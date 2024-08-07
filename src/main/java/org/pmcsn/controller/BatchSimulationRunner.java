package org.pmcsn.controller;


import org.pmcsn.centers.Dummy_MultiServer;
import org.pmcsn.centers.Dummy_SingleServer;
import org.pmcsn.configuration.CenterFactory;
import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;
import org.pmcsn.utils.AnalyticalComputation;
import org.pmcsn.utils.Comparison;
import org.pmcsn.utils.Verification;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.pmcsn.utils.AnalyticalComputation.computeAnalyticalResults;
import static org.pmcsn.utils.Comparison.compareResults;
import static org.pmcsn.utils.PrintUtils.printFinalResults;
import static org.pmcsn.utils.Verification.verifyConfidenceIntervals;

public class BatchSimulationRunner {
    private static final Logger logger = Logger.getLogger(BatchSimulationRunner.class.getName());
    /*  STATISTICS OF INTEREST :
     *  * Response times
     *  * Population
     */

    // Constants
    private static final int START = 0;
    private static final int STOP = Integer.MAX_VALUE;
    private static final long SEED = 123456789L;
    private Dummy_SingleServer dummySingleServer;
    private Dummy_MultiServer dummyMultiServer;

    // We need to compute autocorrelation on the series
    // Number of jobs in single batch (B)
    private final int batchSize;
    // Number of batches (K >= 40)
    private final int batchesNumber;
    private final int warmupThreshold;
    private boolean isWarmingUp = true;


    public BatchSimulationRunner() {
        ConfigurationManager config = new ConfigurationManager();
        batchSize = config.getInt("general", "batchSize");
        batchesNumber = config.getInt("general", "numBatches");
        warmupThreshold = (int) ((batchSize*batchesNumber)*0.2);
    }

    public List<BatchStatistics> runBatchSimulation(boolean approximateServiceAsExponential) throws Exception {
        initCenters(approximateServiceAsExponential);

        String simulationType;
        if (approximateServiceAsExponential) {
            simulationType = "BATCH_SIMULATION_EXPONENTIAL";
        } else {
            simulationType = "BATCH_SIMULATION";
        }
        System.out.println("\nRUNNING " + simulationType + "...");

        // Rng setting the seed
        Rngs rngs = new Rngs();
        rngs.plantSeeds(SEED);

        // Initialize MsqTime
        MsqTime msqTime = new MsqTime();
        msqTime.current = START;
        EventQueue events = new EventQueue();

        // Initialize LuggageChecks
        dummyMultiServer.start(rngs, START);

        // Generate the first arrival
        double time = dummyMultiServer.getArrival();
        events.add(new MsqEvent(EventType.ARRIVAL_FIRST_CENTER, time));

        resetCenters(rngs);

        // the terminating condition is that all the centers have processed all the jobs
        while(!isDone()) {
            // Retrieving next event to be processed
            MsqEvent event = events.pop();
            msqTime.next = event.time;

            // Updating areas
            updateAreas(msqTime);

            // Advancing the clock
            msqTime.current = msqTime.next;

            // Processing the event based on its type
            processCurrentEvent(event, msqTime, events);


            // Checking if still in warmup period
            if (getMinimumNumberOfJobsServedByCenters() >= warmupThreshold && isWarmingUp) {
                System.out.println("WARMUP COMPLETED... Starting to collect statistics for centers from now on.");
                isWarmingUp = false;
                stopWarmup(msqTime);
            }
        }

        // The batch simulation has now ended. Time to collect the statistics
        System.out.println(simulationType + " HAS JUST FINISHED.");
        System.out.printf("Events queue size %d%n", events.size());

        // Writing statistics csv with data from all batches
        writeAllStats(simulationType);

        // Computing and writing verifications stats csv
        if (approximateServiceAsExponential) {
            modelVerification(simulationType);
        }

        return getBatchStatistics();
    }

    private void stopWarmup(MsqTime time) {
        dummyMultiServer.stopWarmup(time);
        dummySingleServer.stopWarmup(time);
    }

    private List<BatchStatistics> getBatchStatistics() {
        List<BatchStatistics> batchStatistics = new ArrayList<>();
        batchStatistics.add(dummyMultiServer.getBatchStatistics());
        batchStatistics.add(dummySingleServer.getBatchStatistics());
        return batchStatistics;
    }


    private void initCenters(boolean approximateServiceAsExponential) {
        CenterFactory factory = new CenterFactory();
        dummySingleServer = factory.createDummySingleServer(approximateServiceAsExponential);
        dummyMultiServer = factory.createDummyMultiServer(approximateServiceAsExponential);
    }

    private void processCurrentEvent(MsqEvent event, MsqTime msqTime, EventQueue events) {
        switch (event.type) {
            case ARRIVAL_FIRST_CENTER:
                dummyMultiServer.processArrival(event, msqTime, events);
                dummyMultiServer.generateNextArrival(events);
                break;
            case FIRST_CENTER_DONE:
                dummyMultiServer.processCompletion(event, msqTime, events);
                break;
            case ARRIVAL_SECOND_CENTER:
                dummySingleServer.processArrival(event, msqTime, events);
                break;
            case SECOND_CENTER_DONE:
                dummySingleServer.processCompletion(event, msqTime, events);
                break;
        }
    }

    private void modelVerification(String simulationType) {
        List<AnalyticalComputation.AnalyticalResult> analyticalResultList = computeAnalyticalResults(simulationType);

        // Compare results and verifications and save comparison result
        List<MeanStatistics> batchMeanStatisticsList = aggregateBatchMeanStatistics();

        List<Comparison.ComparisonResult> comparisonResultList = compareResults(simulationType, analyticalResultList, batchMeanStatisticsList);

        List<ConfidenceIntervals> confidenceIntervalsList = aggregateConfidenceIntervals();

        List<Verification.VerificationResult> verificationResultList = verifyConfidenceIntervals(simulationType, batchMeanStatisticsList, comparisonResultList, confidenceIntervalsList);

        printFinalResults(verificationResultList);
    }

    private List<MeanStatistics> aggregateBatchMeanStatistics() {
        List<MeanStatistics> batchMeanStatisticsList = new ArrayList<>();

        batchMeanStatisticsList.add(dummyMultiServer.getBatchMeanStatistics());
        batchMeanStatisticsList.add(dummySingleServer.getBatchMeanStatistics());

        return batchMeanStatisticsList;
    }

    private List<ConfidenceIntervals> aggregateConfidenceIntervals() {
        List<ConfidenceIntervals> confidenceIntervalsList = new ArrayList<>();

        confidenceIntervalsList.add(createConfidenceIntervals(dummyMultiServer.getBatchStatistics()));
        confidenceIntervalsList.add(createConfidenceIntervals(dummySingleServer.getBatchStatistics()));

        return confidenceIntervalsList;
    }

    private ConfidenceIntervals createConfidenceIntervals(BatchStatistics stats) {
        return new ConfidenceIntervals(
                stats.meanResponseTimeList, stats.meanQueueTimeList, stats.meanServiceTimeList,
                stats.meanSystemPopulationList, stats.meanQueuePopulationList, stats.meanUtilizationList, stats.lambdaList
        );
    }


    private void writeAllStats(String simulationType) {
        System.out.println("Writing csv files with stats for all the centers.");

        dummyMultiServer.writeBatchStats(simulationType);
        dummySingleServer.writeBatchStats(simulationType);

    }

    private void updateAreas(MsqTime msqTime) {
        dummyMultiServer.setArea(msqTime);
        dummySingleServer.setArea(msqTime);
    }

    private long getMinimumNumberOfJobsServedByCenters() {
        // qua ho messo solo il secondo perchè è quello che processa meno jobs e raggiuge il warm up più tardi
        return dummySingleServer.getTotalNumberOfJobsServed();
    }

    private boolean isDone() {

        return dummyMultiServer.isDone() && dummySingleServer.isDone();
    }

    private void resetCenters(Rngs rngs) {
        dummySingleServer.reset(rngs);
    }
}
