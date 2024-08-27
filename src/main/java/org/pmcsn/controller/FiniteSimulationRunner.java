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
    private final int start = 0;
    private final double stop = config.getDouble("general", "finiteSimObservationTime"); // 8 hours
    private final long seed;
    private final int rngStreamIndex = config.getInt("general", "seedStreamIndex");
    private final int runsNumber = config.getInt("general", "runsNumber");

    private RepartoIstruttorie_MAACFinance repartoIstruttorie;
    private SysScoringAutomatico_SANTANDER scoringAutomatico;
    private ComitatoCredito_SANTANDER comitatoCredito;
    private RepartoLiquidazioni_MAACFinance repartoLiquidazioni;

    private List<Observations> repartoIstruttorieObservations;
    private Observations scoringAutomaticoObservations;
    private Observations comitatoCreditoObservations;
    private Observations repartoLiquidazioniObservations;

    public FiniteSimulationRunner() {
        this(123456789L);
    }

    public FiniteSimulationRunner(long seed) {
        this.seed = seed;
    }

    public void runFiniteSimulation(
            boolean approximateServiceAsExponential,
            boolean shouldTrackObservations,
            boolean withDigitalSignature) throws Exception {
        initCenters(approximateServiceAsExponential, withDigitalSignature);

        String simulationType = getSimulationType(approximateServiceAsExponential, withDigitalSignature);
        System.out.println("\nRUNNING " + simulationType + "...");

        //Rng setting the seed
        long[] seeds = new long[1024];
        seeds[0] = seed;
        Rngs rngs = new Rngs();

        String observationsPath = "csvFiles/%s/%d/observations".formatted(simulationType, seed);
        if (shouldTrackObservations) {
            initObservations(observationsPath);
        }

        for (int i = 0; i < runsNumber; i++) {
            long number = 1;

            rngs.plantSeeds(seeds[i]);

            //Msq initialization
            MsqTime msqTime = new MsqTime();
            msqTime.current = start;
            EventQueue queue;
            if (shouldTrackObservations) {
                queue = new FiniteSimulationEventQueue();
            } else {
                queue = new EventQueue();
            }

            // Initialize LuggageChecks
            repartoIstruttorie.start(rngs, start);
            repartoIstruttorie.setStop(stop);

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
                    repartoIstruttorie.updateObservations(repartoIstruttorieObservations);
                    scoringAutomatico.updateObservations(scoringAutomaticoObservations);
                    comitatoCredito.updateObservations(comitatoCreditoObservations);
                    repartoLiquidazioni.updateObservations(repartoLiquidazioniObservations);
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
                writeObservations(observationsPath);
                resetObservations();
            }

            // Saving statistics for current run
            saveAllStats();

            // Generating next seed
            rngs.selectStream(rngStreamIndex);
            seeds[i + 1] = rngs.getSeed();
        }

        System.out.println(simulationType + " HAS JUST FINISHED");

        if (shouldTrackObservations) {
            PlotUtils.welchPlot(observationsPath);
        }

        // Writing statistics csv with data from all runs
        writeAllStats(simulationType, seed);

        if (approximateServiceAsExponential) {
            modelVerification(simulationType); // Computing and writing verifications stats csv
        }
        System.out.println();
        printMeanResponseTime();
        System.out.println(BRIGHT_GREEN + "Average time spent by one job is: "+ repartoLiquidazioni.getMeanResidenceTime() + " min");
    }

    private String getSimulationType(boolean approximateServiceAsExponential, boolean withDigitalSignature) {
        String s;
        if (approximateServiceAsExponential) {
            s = "FINITE_SIMULATION_EXPONENTIAL";
        } else {
            s = "FINITE_SIMULATION";
        }
        if (withDigitalSignature) {
            return s + "_DIGITAL_SIGNATURE";
        }
        return s;
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

    private void printMeanResponseTime(){
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

    private void writeAllStats(String simulationType, long seed) {
        System.out.println("Writing csv files with stats for all the centers.");
        repartoIstruttorie.writeStats(simulationType, seed);
        scoringAutomatico.writeStats(simulationType, seed);
        comitatoCredito.writeStats(simulationType, seed);
        repartoLiquidazioni.writeStats(simulationType, seed);
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

    private void initObservations(String path) {
        FileUtils.deleteDirectory(path);
        repartoIstruttorieObservations = new ArrayList<>();
        for (int i = 0; i < repartoIstruttorie.getServersNumber(); i++) {
            repartoIstruttorieObservations.add(new Observations("%s_%d".formatted(repartoIstruttorie.getCenterName(), i + 1)));
        }
        scoringAutomaticoObservations = new Observations(scoringAutomatico.getCenterName());
        comitatoCreditoObservations = new Observations(comitatoCredito.getCenterName());
        repartoLiquidazioniObservations = new Observations(repartoLiquidazioni.getCenterName());
    }

    private void resetObservations() {
        repartoIstruttorieObservations.forEach(Observations::reset);
        scoringAutomaticoObservations.reset();
        comitatoCreditoObservations.reset();
        repartoLiquidazioniObservations.reset();
    }

    private void writeObservations(String path) {
        PlotUtils.writeObservations(path, repartoIstruttorieObservations);
        PlotUtils.writeObservations(path, scoringAutomaticoObservations);
        PlotUtils.writeObservations(path, comitatoCreditoObservations);
        PlotUtils.writeObservations(path, repartoLiquidazioniObservations);
    }
}
