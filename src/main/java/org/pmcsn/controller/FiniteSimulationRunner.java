package org.pmcsn.controller;


import org.pmcsn.centers.ComitatoCredito_SANTANDER;
import org.pmcsn.centers.RepartoIstruttorie_MAACFinance;
import org.pmcsn.centers.RepartoLiquidazioni_MAACFinance;
import org.pmcsn.centers.SysScoringAutomatico_SANTANDER;
import org.pmcsn.configuration.CenterFactory;
import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;
import org.pmcsn.utils.*;

import java.util.ArrayList;
import java.util.List;

import static org.pmcsn.utils.AnalyticalComputation.computeAnalyticalResults;
import static org.pmcsn.utils.Comparison.compareResults;
import static org.pmcsn.utils.PrintUtils.BRIGHT_GREEN;
import static org.pmcsn.utils.PrintUtils.printFinalResults;
import static org.pmcsn.utils.Verification.verifyConfidenceIntervals;

public class FiniteSimulationRunner {
    private static final ConfigurationManager config = new ConfigurationManager();
    private static final int START = 0;
    private static final double STOP = config.getDouble("general", "finiteSimObservationTime"); // 8 hours
    private static final long SEED = 123456789L;

    private RepartoIstruttorie_MAACFinance repartoIstruttorie;
    private SysScoringAutomatico_SANTANDER scoringAutomatico;
    private ComitatoCredito_SANTANDER comitatoCredito;
    private RepartoLiquidazioni_MAACFinance repartoLiquidazioni;

    private List<Observations> repartoIstruttorieObservations;
    private Observations scoringAutomaticoObservations;
    private Observations comitatoCreditoObservations;
    private Observations repartoLiquidazioniObservations;

    public void runFiniteSimulation(
            boolean approximateServiceAsExponential,
            boolean shouldTrackObservations,
            boolean isDigitalSignature) throws Exception {
        initCenters(approximateServiceAsExponential, isDigitalSignature);
        String simulationType;
        if (approximateServiceAsExponential) {
            simulationType = "FINITE_SIMULATION_EXPONENTIAL";
        } else {
            simulationType = "FINITE_SIMULATION";
        }
        System.out.println("\nRUNNING " + simulationType + "...");

        //Rng setting the seed
        long[] seeds = new long[1024];
        seeds[0] = SEED;
        Rngs rngs = new Rngs();

        int runsNumber = config.getInt("general", "runsNumber");
        if (shouldTrackObservations) {
            initObservations(simulationType, runsNumber);
        }

        for (int i = 0; i < runsNumber; i++) {
            long number = 1;

            rngs.plantSeeds(seeds[i]);

            //Msq initialization
            MsqTime msqTime = new MsqTime();
            msqTime.current = START;
            EventQueue queue;
            if (shouldTrackObservations) {
                queue = new FiniteSimulationEventQueue();
            } else {
                queue = new EventQueue();
            }

            // Initialize LuggageChecks
            repartoIstruttorie.start(rngs, START);
            repartoIstruttorie.setStop(STOP);

            //generating first arrival
            double time = repartoIstruttorie.getArrival();
            MsqEvent firstEvent = new MsqEvent(EventType.ARRIVAL_REPARTO_ISTRUTTORIE, time);
            firstEvent.applicant = Applicant.create(rngs, time);
            queue.add(firstEvent);

            // Initialize and reset other centers
            resetCenters(rngs);

            MsqEvent event;
            // need to use OR because all the conditions should be false
            while (!repartoIstruttorie.isEndOfArrivals() || !queue.isEmpty() || number != 0) {
                // Retrieving next event to be processed
                event = queue.pop();
                if (event.type == EventType.SAVE_STAT) {
                    repartoIstruttorie.updateObservations(repartoIstruttorieObservations, i);
                    scoringAutomatico.updateObservations(scoringAutomaticoObservations, i);
                    comitatoCredito.updateObservations(comitatoCreditoObservations, i);
                    repartoLiquidazioni.updateObservations(repartoLiquidazioniObservations, i);
                    continue;
                }
                msqTime.next = event.time;

                // Updating areas
                updateAreas(msqTime);

                // Advancing the clock
                msqTime.current = msqTime.next;

                // Processing the event based on its type
                processCurrentEvent(event, msqTime, queue);

                number = getTotalNumberOfJobsInSystem();
            }

            // Writing observations for current run
            if (shouldTrackObservations) {
                writeObservations(simulationType);
                resetObservations();
            }

            //System.out.println("EVENT COUNT FOR RUN N°"+i+": " + eventCount);
            // Saving statistics for current run
            saveAllStats();

            // Generating next seed
            rngs.selectStream(config.getInt("general", "seedStreamIndex"));
            seeds[i + 1] = rngs.getSeed();
        }

        System.out.println(simulationType + " HAS JUST FINISHED.");

        if (shouldTrackObservations) {
            WelchPlot.welchPlot("csvFiles/%s/observations".formatted(simulationType));
        }

        // Writing statistics csv with data from all runs
        writeAllStats(simulationType);

        if(approximateServiceAsExponential) {
            modelVerification(simulationType); // Computing and writing verifications stats csv
        }
        System.out.println();
        getMeanResponseTime();
        System.out.println(BRIGHT_GREEN + "Average time spent by one job is: "+ repartoLiquidazioni.getMeanResidenceTime() + " min");
    }

    private void initCenters(boolean approximateServiceAsExponential,  boolean isDigitalSignature) {
        CenterFactory factory = new CenterFactory(false);
        repartoIstruttorie = factory.createRepartoIstruttorie(approximateServiceAsExponential, false);
        scoringAutomatico = factory.createSysScoringAutomatico(approximateServiceAsExponential, false);
        comitatoCredito = factory.createComitatoCredito(approximateServiceAsExponential, false);
        repartoLiquidazioni = factory.createRepartoLiquidazioni(approximateServiceAsExponential, isDigitalSignature, false);
    }

    private void resetCenters(Rngs rngs) {
        scoringAutomatico.reset(rngs);
        comitatoCredito.reset(rngs);
        repartoLiquidazioni.reset(rngs);
    }

    private void processCurrentEvent(MsqEvent event, MsqTime msqTime, EventQueue events) {
        switch (event.type) {
            case ARRIVAL_REPARTO_ISTRUTTORIE:
                repartoIstruttorie.processArrival(event, msqTime, events);
                repartoIstruttorie.generateNextArrival(events);
                break;
            case COMPLETION_REPARTO_ISTRUTTORIE:
                repartoIstruttorie.processCompletion(event, msqTime, events);
                break;
            case ARRIVAL_SCORING_AUTOMATICO:
                scoringAutomatico.processArrival(event, msqTime, events);
                break;
            case COMPLETION_SCORING_AUTOMATICO:
                scoringAutomatico.processCompletion(event, msqTime, events);
                break;
            case ARRIVAL_COMITATO_CREDITO:
                comitatoCredito.processArrival(event, msqTime, events);
                break;
            case COMPLETION_COMITATO_CREDITO:
                comitatoCredito.processCompletion(event, msqTime, events);
                break;
            case ARRIVAL_REPARTO_LIQUIDAZIONI:
                repartoLiquidazioni.processArrival(event, msqTime, events);
                break;
            case COMPLETION_REPARTO_LIQUIDAZIONI:
                repartoLiquidazioni.processCompletion(event, msqTime, events);
                break;
        }
    }

    private void saveAllStats() {
        repartoIstruttorie.saveStats();
        scoringAutomatico.saveStats();
        comitatoCredito.saveStats();
        repartoLiquidazioni.saveStats();
    }

    private void modelVerification(String simulationType) {
        List<AnalyticalComputation.AnalyticalResult> analyticalResultList = computeAnalyticalResults(simulationType);

        // Compare results and verifications and save comparison result
        List<MeanStatistics> meanStatisticsList = aggregateMeanStatistics();

        List<Comparison.ComparisonResult> comparisonResultList = compareResults(simulationType, analyticalResultList, meanStatisticsList);

        List<ConfidenceIntervals> confidenceIntervalsList = aggregateConfidenceIntervals();

        List<Verification.VerificationResult> verificationResultList = verifyConfidenceIntervals(simulationType, meanStatisticsList, comparisonResultList, confidenceIntervalsList);

        printFinalResults(verificationResultList, 0, 0);
    }

    private List<MeanStatistics> aggregateMeanStatistics() {
        List<MeanStatistics> meanStatisticsList = new ArrayList<>();

        meanStatisticsList.add(repartoIstruttorie.getMeanStatistics());
        meanStatisticsList.add(scoringAutomatico.getMeanStatistics());
        meanStatisticsList.add(comitatoCredito.getMeanStatistics());
        meanStatisticsList.add(repartoLiquidazioni.getMeanStatistics());
        return meanStatisticsList;
    }

    private void getMeanResponseTime(){
        System.out.println("Average response time in REPARTO ISTRUTTORIE: "+ repartoIstruttorie.getMeanStatistics().meanResponseTime);
        System.out.println("Average response time in SISTEMA SCORING AUTOMATICO: "+ scoringAutomatico.getMeanStatistics().meanResponseTime);
        System.out.println("Average response time in COMITATO CREDITO: " + comitatoCredito.getMeanStatistics().meanResponseTime);
        System.out.println("Average response time in REPARTO LIQUIDAZIONI: "+repartoLiquidazioni.getMeanStatistics().meanResponseTime);
    }

    private List<ConfidenceIntervals> aggregateConfidenceIntervals() {
        List<ConfidenceIntervals> confidenceIntervalsList = new ArrayList<>();
        confidenceIntervalsList.add(createConfidenceIntervals(repartoIstruttorie.getStatistics()));
        confidenceIntervalsList.add(createConfidenceIntervals(scoringAutomatico.getStatistics()));
        confidenceIntervalsList.add(createConfidenceIntervals(comitatoCredito.getStatistics()));
        confidenceIntervalsList.add(createConfidenceIntervals(repartoLiquidazioni.getStatistics()));

        return confidenceIntervalsList;
    }

    private ConfidenceIntervals createConfidenceIntervals(BasicStatistics stats) {
        return new ConfidenceIntervals(
                stats.meanResponseTimeList, stats.meanQueueTimeList, stats.meanServiceTimeList,
                stats.meanSystemPopulationList, stats.meanQueuePopulationList, stats.meanUtilizationList, stats.lambdaList
        );
    }

    private void writeAllStats(String simulationType) {
        System.out.println("Writing csv files with stats for all the centers.");
        repartoIstruttorie.writeStats(simulationType);
        scoringAutomatico.writeStats(simulationType);
        comitatoCredito.writeStats(simulationType);
        repartoLiquidazioni.writeStats(simulationType);
    }

    private long getTotalNumberOfJobsInSystem() {
        return repartoIstruttorie.getNumberOfJobsInNode() +
                scoringAutomatico.getNumberOfJobsInNode() +
                comitatoCredito.getNumberOfJobsInNode() +
                repartoLiquidazioni.getNumberOfJobsInNode();
    }

    private void updateAreas(MsqTime msqTime) {
        // Updating the areas
        repartoIstruttorie.setArea(msqTime);
        scoringAutomatico.setArea(msqTime);
        comitatoCredito.setArea(msqTime);
        repartoLiquidazioni.setArea(msqTime);
    }


    private void initObservations(String simulationType, int runsNumber) {
        FileUtils.deleteDirectory("csvFiles/%s/observations".formatted(simulationType));
        repartoIstruttorieObservations = new ArrayList<>();
        for (int i = 0; i < repartoIstruttorie.getServersNumber(); i++) {
            repartoIstruttorieObservations.add(new Observations("%s_%d".formatted(repartoIstruttorie.getCenterName(), i + 1), runsNumber));
        }
        scoringAutomaticoObservations = new Observations(scoringAutomatico.getCenterName(), runsNumber);
        comitatoCreditoObservations = new Observations(comitatoCredito.getCenterName(), runsNumber);
        repartoLiquidazioniObservations = new Observations(repartoLiquidazioni.getCenterName(), runsNumber);
    }

    private void resetObservations() {
        repartoIstruttorieObservations.forEach(Observations::reset);
        scoringAutomaticoObservations.reset();
        comitatoCreditoObservations.reset();
        repartoLiquidazioniObservations.reset();
    }

    private void writeObservations(String simulationType) {
        WelchPlot.writeObservations(simulationType, repartoIstruttorieObservations);
        WelchPlot.writeObservations(simulationType, scoringAutomaticoObservations);
        WelchPlot.writeObservations(simulationType, comitatoCreditoObservations);
        WelchPlot.writeObservations(simulationType, repartoLiquidazioniObservations);
    }
}
