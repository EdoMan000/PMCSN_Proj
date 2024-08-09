package org.pmcsn.controller;

import org.pmcsn.centers.*;
import org.pmcsn.configuration.CenterFactory;
import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;
import org.pmcsn.utils.AnalyticalComputation;
import org.pmcsn.utils.Comparison;
import org.pmcsn.utils.Verification;
import org.pmcsn.utils.WelchPlot;

import java.util.ArrayList;
import java.util.List;

import static org.pmcsn.utils.AnalyticalComputation.computeAnalyticalResults;
import static org.pmcsn.utils.Comparison.compareResults;
import static org.pmcsn.utils.PrintUtils.printFinalResults;
import static org.pmcsn.utils.Verification.verifyConfidenceIntervals;



public class FiniteImprovedSimulationRunner {

    /*  STATISTICS OF INTEREST :
     *  * Response times
     *  * Population
     */

    // Constants
    private static final ConfigurationManager config = new ConfigurationManager();
    private static final int START = 0;
    private static double STOP = config.getDouble("general", "finiteSimObservationTime"); // 8 hours
    private static final long SEED = 123456789L;

    private PreScoring_MAACFinance preScoring;
    private RepartoIstruttorie_MAACFinance repartoIstruttorie;
    private SysScoringAutomatico_SANTANDER scoringAutomatico;
    private ComitatoCredito_SANTANDER comitatoCredito;
    private RepartoLiquidazioni_MAACFinance repartoLiquidazioni;
    private final List<Observations> repartoIstruttorieObservations = new ArrayList<>();

    //TODO
    private final Observations preScoringObservations = null;
    private final Observations scoringAutomaticoObservations = null;
    private final Observations comitatoCreditoObservations = null;
    private final Observations repartoLiquidazioniObservations = null;


    public void runImprovedModelSimulation(boolean approximateServiceAsExponential, boolean shouldTrackObservations, boolean isImprovedModel) throws Exception {
        initCenters(approximateServiceAsExponential, isImprovedModel);
        String simulationType;
        if(approximateServiceAsExponential){
            simulationType = "IMPROVED_FINITE_SIMULATION_EXPONENTIAL";
        }else{
            simulationType = "IMPROVED_FINITE_SIMULATION";
        }
        System.out.println("\nRUNNING " + simulationType + "...");

        //Rng setting the seed
        long[] seeds = new long[1024];
        seeds[0] = SEED;
        Rngs rngs = new Rngs();

        if (shouldTrackObservations) {
            initObservations();
        }

        int runsNumber = config.getInt("general", "runsNumber");
        for (int i = 0; i < runsNumber; i++) {
            double sarrival = START;
            long number = 1;

            rngs.plantSeeds(seeds[i]);

            //Msq initialization
            MsqTime msqTime = new MsqTime();
            msqTime.current = START;
            EventQueue queue = new EventQueue();

            // Initialize LuggageChecks
            preScoring.start(rngs, sarrival);
            preScoring.setStop(STOP);

            //generating first arrival
            double time = preScoring.getArrival();
            MsqEvent firstEvent = new MsqEvent(EventType.ARRIVAL_PRE_SCORING, time);
            firstEvent.applicant = new Applicant();
            queue.add(firstEvent);

            // Initialize and reset other centers
            resetCenters(rngs);

            MsqEvent event;
            int skip = 3;
            int eventCount = 0;

            // need to use OR because all the conditions should be false
            while (!preScoring.isEndOfArrivals() || !queue.isEmpty() || number != 0) {

                // Retrieving next event to be processed
                event = queue.pop();
                msqTime.next = event.time;

                // Updating areas
                updateAreas(msqTime);

                // Advancing the clock
                msqTime.current = msqTime.next;

                // Processing the event based on its type
                processCurrentEvent(shouldTrackObservations, event, msqTime, queue, eventCount, skip, i);

                eventCount++;

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
            WelchPlot.welchPlot("csvFiles/BASIC_SIMULATION_EXPONENTIAL/observations");
        }

        // Writing statistics csv with data from all runs
        writeAllStats(simulationType);

        if(approximateServiceAsExponential) {
            modelVerification(simulationType); // Computing and writing verifications stats csv
        }

        //printJobsServedByNodes(luggageChecks, checkInDesks, boardingPassScanners, securityChecks, passportChecks, stampsCheck, boarding, false);
    }

    private void initCenters(boolean approximateServiceAsExponential,  boolean isImprovedModel) {
        CenterFactory factory = new CenterFactory();
        preScoring = factory.createPreScoring(approximateServiceAsExponential, isImprovedModel, false);
        repartoIstruttorie = factory.createRepartoIstruttorieImproved(approximateServiceAsExponential, isImprovedModel, false);
        scoringAutomatico = factory.createSysScoringAutomatico(approximateServiceAsExponential, isImprovedModel, false);
        comitatoCredito = factory.createComitatoCredito(approximateServiceAsExponential, isImprovedModel, false);
        repartoLiquidazioni = factory.createRepartoLiquidazioni(approximateServiceAsExponential, isImprovedModel, false);
    }

    private void resetCenters(Rngs rngs) {
        repartoIstruttorie.reset(rngs);
        scoringAutomatico.reset(rngs);
        comitatoCredito.reset(rngs);
        repartoLiquidazioni.reset(rngs);
    }

    private void processCurrentEvent(boolean shouldTrackObservations, MsqEvent event, MsqTime msqTime, EventQueue events, int eventCount, int skip, int i) {
        switch (event.type) {
            case ARRIVAL_PRE_SCORING:
                preScoring.processArrival(event, msqTime, events);
                preScoring.generateNextArrival(events);
                break;
            case COMPLETION_PRE_SCORING:
                preScoring.processCompletion(event, msqTime, events);
                break;
            case ARRIVAL_REPARTO_ISTRUTTORIE:
                repartoIstruttorie.processArrival(event, msqTime, events);
                break;
            case COMPLETION_REPARTO_ISTRUTTORIE:
                repartoIstruttorie.processCompletion(event, msqTime, events);
                if (shouldTrackObservations && eventCount % skip == 0)
                    repartoIstruttorie.updateObservations(repartoIstruttorieObservations, i);
                break;
            case ARRIVAL_SCORING_AUTOMATICO:
                scoringAutomatico.processArrival(event, msqTime, events);
                break;
            case COMPLETION_SCORING_AUTOMATICO:
                scoringAutomatico.processCompletion(event, msqTime, events);
                //if (shouldTrackObservations && eventCount % skip == 0)
                //TODO
                //scoringAutomatico.updateObservations(scoringAutomaticoObservations, i);
                break;
            case ARRIVAL_COMITATO_CREDITO:
                comitatoCredito.processArrival(event, msqTime, events);
                break;
            case COMPLETION_COMITATO_CREDITO:
                comitatoCredito.processCompletion(event, msqTime, events);
                //if (shouldTrackObservations && eventCount % skip == 0)
                //TODO
                //comitatoCredito.updateObservations(comitatoCreditoObservations, i);
                break;
            case ARRIVAL_REPARTO_LIQUIDAZIONI:
                repartoLiquidazioni.processArrival(event, msqTime, events);
                break;
            case COMPLETION_REPARTO_LIQUIDAZIONI:
                repartoLiquidazioni.processCompletion(event, msqTime, events);
                //if (shouldTrackObservations && eventCount % skip == 0)
                //TODO
                //repartoLiquidazioni.updateObservations(repartoLiquidazioniObservations, i);
                break;
        }
    }

    private void saveAllStats() {
        preScoring.saveStats();
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

        printFinalResults(verificationResultList);
    }

    private List<MeanStatistics> aggregateMeanStatistics() {
        List<MeanStatistics> meanStatisticsList = new ArrayList<>();

        meanStatisticsList.add(preScoring.getMeanStatistics());
        meanStatisticsList.add(repartoIstruttorie.getMeanStatistics());
        meanStatisticsList.add(scoringAutomatico.getMeanStatistics());
        meanStatisticsList.add(comitatoCredito.getMeanStatistics());
        meanStatisticsList.add(repartoLiquidazioni.getMeanStatistics());
        return meanStatisticsList;
    }

    private List<ConfidenceIntervals> aggregateConfidenceIntervals() {
        List<ConfidenceIntervals> confidenceIntervalsList = new ArrayList<>();
        confidenceIntervalsList.add(createConfidenceIntervals(preScoring.getStatistics()));
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

        preScoring.writeStats(simulationType);
        repartoIstruttorie.writeStats(simulationType);
        scoringAutomatico.writeStats(simulationType);
        comitatoCredito.writeStats(simulationType);
        repartoLiquidazioni.writeStats(simulationType);
    }

    private long getTotalNumberOfJobsInSystem() {
        return preScoring.getNumberOfJobsInNode() +
                repartoIstruttorie.getNumberOfJobsInNode() +
                scoringAutomatico.getNumberOfJobsInNode() +
                comitatoCredito.getNumberOfJobsInNode() +
                repartoLiquidazioni.getNumberOfJobsInNode();
    }

    private void updateAreas(MsqTime msqTime) {
        // Updating the areas
        preScoring.setArea(msqTime);
        repartoIstruttorie.setArea(msqTime);
        scoringAutomatico.setArea(msqTime);
        comitatoCredito.setArea(msqTime);
        repartoLiquidazioni.setArea(msqTime);
    }


    // TODO: capire se è corretto
    private void initObservations() {
        int runsNumber = config.getInt("general", "runsNumber");

        for (int j = 0; j < config.getInt("checkInDeskOthers", "serversNumber"); j++) {
            repartoIstruttorieObservations.add(new Observations("%s_%d".formatted(config.getString("repartoIstruttorieMAAC", "centerName"), j + 1), runsNumber));
        }

        //scoringAutomaticoObservations.add(new Observations("%s".formatted(config.getString("sysScoringAutomaticoSANTANDER", "centerName")), runsNumber));

        //comitatoCreditoObservations.add(new Observations("%s".formatted(config.getString("comitatoCreditoSANTANDER", "centerName")), runsNumber));

        //repartoLiquidazioniObservations.add(new Observations("%s".formatted(config.getString("repartoLiquidazioniMAAC", "centerName")), runsNumber));

    }

    //TODO
    private void resetObservations() {
        repartoIstruttorieObservations.forEach(Observations::reset);
        //scoringAutomaticoObservations.forEach(Observations::reset);
        //comitatoCreditoObservations.forEach(Observations::reset);
        //repartoLiquidazioniObservations.forEach(Observations::reset);
    }

    //TODO
    private void writeObservations(String simulationType) {
        // Computing warm up period boundaries
        WelchPlot.writeObservations(simulationType, repartoIstruttorieObservations);
        //WelchPlot.writeObservations(simulationType, scoringAutomaticoObservations);
        //WelchPlot.writeObservations(simulationType, comitatoCreditoObservations);
        //WelchPlot.writeObservations(simulationType, repartoLiquidazioniObservations);
    }
}