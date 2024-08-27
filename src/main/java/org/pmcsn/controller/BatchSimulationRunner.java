package org.pmcsn.controller;

import org.pmcsn.centers.*;
import org.pmcsn.configuration.CenterFactory;
import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;
import org.pmcsn.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.pmcsn.utils.AnalyticalComputation.computeAnalyticalResults;
import static org.pmcsn.utils.Comparison.compareResults;
import static org.pmcsn.utils.PrintUtils.*;
import static org.pmcsn.utils.Verification.verifyConfidenceIntervals;

public class BatchSimulationRunner {
    // Constants
    private static final int START = 0;
    private final long seed;

    // Centri
    private RepartoIstruttorie_MAACFinance repartoIstruttorie;
    private SysScoringAutomatico_SANTANDER scoringAutomatico;
    private ComitatoCredito_SANTANDER comitatoCredito;
    private RepartoLiquidazioni_MAACFinance repartoLiquidazioni;

    private List<Observations> psObservations;
    private List<Observations> rimObservations;
    private Observations saObservations;
    private Observations ccObservations;
    private Observations rilObservations;

    // We need to compute autocorrelation on the series
    // Number of jobs in single batch (B)
    private final int batchSize;
    // Number of batches (K >= 40)
    private final int numBatches;
    private final int warmupThreshold;
    private boolean isWarmingUp = true;
    private final int intervalLength;

    public BatchSimulationRunner(int batchSize, int numBatches, int warmupThreshold) {
        this(batchSize, numBatches, warmupThreshold, 123456789L);
    }

    public BatchSimulationRunner(int batchSize, int numBatches, int warmupThreshold, long seed) {
        this.batchSize = batchSize;
        this.numBatches = numBatches;
        this.warmupThreshold = warmupThreshold;
        this.seed = seed;
        final ConfigurationManager conf = new ConfigurationManager();
        intervalLength = conf.getInt("general", "bmIntervalLength");
    }

    public List<BatchStatistics> runBatchSimulation(boolean approximateServiceAsExponential,  boolean withDigitalSignature) throws Exception {
        initCenters(approximateServiceAsExponential, withDigitalSignature);

        String simulationType = getSimulationType(approximateServiceAsExponential, withDigitalSignature);
        printDebug("\nRUNNING " + simulationType + "...");

        // Rng setting the seed
        Rngs rngs = new Rngs();
        rngs.plantSeeds(seed);

        String observationsPath = "csvFiles/%s/%d/observations".formatted(simulationType, seed);
        initObservations(observationsPath);

        // Initialize MsqTime
        MsqTime msqTime = new MsqTime();
        msqTime.current = START;
        EventQueue events = new FiniteSimulationEventQueue(intervalLength);

        repartoIstruttorie.start(rngs, START);
        repartoIstruttorie.setStop(Double.POSITIVE_INFINITY);

        // Generate the first arrival
        double time = repartoIstruttorie.getArrival();
        MsqEvent firstEvent = new MsqEvent(EventType.ARRIVAL_REPARTO_ISTRUTTORIE, time);
        firstEvent.applicant = Applicant.create(rngs, time);
        events.add(firstEvent);

        resetCenters(rngs);

        boolean stopWarmup = false;

        // the terminating condition is that all the centers have processed all the jobs
        while(!isDone()) {
            // Retrieving next event to be processed
            MsqEvent event = events.pop();
            if (event.type == EventType.SAVE_STAT) {
                if (!isWarmingUp) {
                    repartoIstruttorie.updateObservations(rimObservations);
                    scoringAutomatico.updateObservations(saObservations);
                    comitatoCredito.updateObservations(ccObservations);
                    repartoLiquidazioni.updateObservations(rilObservations);
                }
                continue;
            }
            msqTime.next = event.time;

            // Updating areas
            updateAreas(msqTime);

            MsqTime currentTime = new MsqTime();
            currentTime.current = msqTime.current;
            // Advancing the clock
            msqTime.current = msqTime.next;

            if (stopWarmup) {
                stopWarmup(currentTime);
                stopWarmup = false;
            }

            // Processing the event based on its type
            processCurrentEvent(event, msqTime, events);

            // Checking if still in warmup period
            if (isWarmingUp && getMinimumNumberOfJobsServedByCenters() >= warmupThreshold ) {
                printSuccess("WARMUP COMPLETED... Starting to collect statistics for centers from now on.");
                isWarmingUp = false;
                stopWarmup = true;
            }
        }

        System.out.printf("pControlloAutomatico = %f%n", scoringAutomatico.getAcceptedJobs() / scoringAutomatico.getTotalNumberOfJobs());
        System.out.printf("pFeedback = %f%n", comitatoCredito.feedbackCreated / comitatoCredito.getTotalNumberOfJobs());
        System.out.printf("pComitato = %f%n", comitatoCredito.getAcceptedJobs() / comitatoCredito.getTotalNumberOfJobs());

        // The batch simulation has now ended. Time to collect the statistics
        printSuccess(simulationType + " HAS JUST FINISHED.");
        printDebug("Events queue size is " + events.size());

        writeObservations(observationsPath);
        PlotUtils.welchPlot(observationsPath);

        // Writing statistics csv with data from all batches
        writeAllStats(simulationType, seed);

        // Computing and writing verifications stats csv
        if (approximateServiceAsExponential) {
            modelVerification(simulationType);
        }

        return getBatchStatistics();
    }

    private void initObservations(String path) {
        FileUtils.deleteDirectory(path);
        rimObservations = new ArrayList<>();
        for (int i = 0; i < repartoIstruttorie.getServersNumber(); i++) {
            rimObservations.add(new Observations("%s_%d".formatted(repartoIstruttorie.getCenterName(), i + 1)));
        }
        saObservations = new Observations(scoringAutomatico.getCenterName());
        ccObservations = new Observations(comitatoCredito.getCenterName());
        rilObservations = new Observations(repartoLiquidazioni.getCenterName());
    }

    private void writeObservations(String path) {
        PlotUtils.writeObservations(path, rimObservations);
        PlotUtils.writeObservations(path, saObservations);
        PlotUtils.writeObservations(path, ccObservations);
        PlotUtils.writeObservations(path, rilObservations);
    }

    private String getSimulationType(boolean approximateServiceAsExponential, boolean withDigitalSignature) {
        String s;
        if (approximateServiceAsExponential) {
            s = "BATCH_SIMULATION_EXPONENTIAL";
        } else {
            s = "BATCH_SIMULATION";
        }
        if (withDigitalSignature) {
            return s + "_DIGITAL_SIGNATURE";
        }
        return s;
    }

    private void stopWarmup(MsqTime time) {
        repartoIstruttorie.stopWarmup(time);
        scoringAutomatico.stopWarmup(time);
        comitatoCredito.stopWarmup(time);
        repartoLiquidazioni.stopWarmup(time);
    }

    private List<BatchStatistics> getBatchStatistics() {
        List<BatchStatistics> batchStatistics = new ArrayList<>();
        batchStatistics.add(repartoIstruttorie.getBatchStatistics());
        batchStatistics.add(scoringAutomatico.getBatchStatistics());
        batchStatistics.add(comitatoCredito.getBatchStatistics());
        batchStatistics.add(repartoLiquidazioni.getBatchStatistics());
        return batchStatistics;
    }


    private void initCenters(boolean approximateServiceAsExponential,  boolean isDigitalSignature) {
        CenterFactory factory = new CenterFactory(false);
        repartoIstruttorie = factory.createRepartoIstruttorie(approximateServiceAsExponential, true);
        scoringAutomatico = factory.createSysScoringAutomatico(approximateServiceAsExponential, true);
        comitatoCredito = factory.createComitatoCredito(approximateServiceAsExponential, true);
        repartoLiquidazioni = factory.createRepartoLiquidazioni(approximateServiceAsExponential, isDigitalSignature, true);
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

    private void modelVerification(String simulationType) {
        List<AnalyticalComputation.AnalyticalResult> analyticalResultList = computeAnalyticalResults(simulationType);

        // Compare results and verifications and save comparison result
        List<MeanStatistics> batchMeanStatisticsList = aggregateBatchMeanStatistics();

        List<Comparison.ComparisonResult> comparisonResultList = compareResults(simulationType, analyticalResultList, batchMeanStatisticsList);

        List<ConfidenceIntervals> confidenceIntervalsList = aggregateConfidenceIntervals();

        List<Verification.VerificationResult> verificationResultList = verifyConfidenceIntervals(simulationType, batchMeanStatisticsList, comparisonResultList, confidenceIntervalsList);

        printFinalResults(verificationResultList, batchSize, numBatches);
    }

    private List<MeanStatistics> aggregateBatchMeanStatistics() {
        List<MeanStatistics> batchMeanStatisticsList = new ArrayList<>();
        batchMeanStatisticsList.add(repartoIstruttorie.getBatchMeanStatistics());
        batchMeanStatisticsList.add(scoringAutomatico.getBatchMeanStatistics());
        batchMeanStatisticsList.add(comitatoCredito.getBatchMeanStatistics());
        batchMeanStatisticsList.add(repartoLiquidazioni.getBatchMeanStatistics());
        return batchMeanStatisticsList;
    }

    private List<ConfidenceIntervals> aggregateConfidenceIntervals() {
        List<ConfidenceIntervals> confidenceIntervalsList = new ArrayList<>();
        confidenceIntervalsList.add(createConfidenceIntervals(repartoIstruttorie.getBatchStatistics()));
        confidenceIntervalsList.add(createConfidenceIntervals(scoringAutomatico.getBatchStatistics()));
        confidenceIntervalsList.add(createConfidenceIntervals(comitatoCredito.getBatchStatistics()));
        confidenceIntervalsList.add(createConfidenceIntervals(repartoLiquidazioni.getBatchStatistics()));
        return confidenceIntervalsList;
    }

    private ConfidenceIntervals createConfidenceIntervals(BatchStatistics stats) {
        return new ConfidenceIntervals(
                stats.meanResponseTimeList, stats.meanQueueTimeList, stats.meanServiceTimeList,
                stats.meanSystemPopulationList, stats.meanQueuePopulationList, stats.meanUtilizationList, stats.lambdaList
        );
    }

    private void writeAllStats(String simulationType, long seed) {
        printDebug("Writing csv files with stats for all the centers.");
        repartoIstruttorie.writeBatchStats(simulationType, seed);
        scoringAutomatico.writeBatchStats(simulationType, seed);
        comitatoCredito.writeBatchStats(simulationType, seed);
        repartoLiquidazioni.writeBatchStats(simulationType, seed);
    }

    private void updateAreas(MsqTime msqTime) {
        repartoIstruttorie.setArea(msqTime);
        scoringAutomatico.setArea(msqTime);
        comitatoCredito.setArea(msqTime);
        repartoLiquidazioni.setArea(msqTime);
    }

    private long getMinimumNumberOfJobsServedByCenters() {
        return Stream.of(
                repartoLiquidazioni.getTotalNumberOfJobsServed(),
                scoringAutomatico.getTotalNumberOfJobsServed(),
                comitatoCredito.getTotalNumberOfJobsServed(),
                repartoIstruttorie.getTotalNumberOfJobsServed())
                .min(Long::compare).orElseThrow();
    }

    private boolean isDone() {
        return repartoIstruttorie.isDone()
                && scoringAutomatico.isDone()
                && comitatoCredito.isDone()
                && repartoLiquidazioni.isDone();
    }

    private void resetCenters(Rngs rngs) {
        scoringAutomatico.reset(rngs);
        comitatoCredito.reset(rngs);
        repartoLiquidazioni.reset(rngs);
    }
}
