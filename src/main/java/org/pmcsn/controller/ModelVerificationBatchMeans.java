package org.pmcsn.controller;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.model.BatchStatistics;
import org.pmcsn.model.BatchMetric;

import java.util.List;

import static org.pmcsn.utils.PrintUtils.printBatchStatisticsResult;

public class ModelVerificationBatchMeans {

    public static void runModelVerificationWithBatchMeansMethod() throws Exception {
        ConfigurationManager config = new ConfigurationManager();
        int batchSize = config.getInt("general", "batchSize");
        int batchesNumber = config.getInt("general", "numBatches");
        int warmupThreshold = (int) ((batchSize*batchesNumber)*config.getDouble("general", "warmupPercentage"));
        BatchSimulationRunner batchRunner = new BatchSimulationRunner(batchSize, batchesNumber, warmupThreshold);
        List<BatchStatistics> batchStatisticsList = batchRunner.runBatchSimulation(true, false);

        // Iterate over each BatchStatistics object
        for (BatchStatistics batchStatistics : batchStatisticsList) {
            // List of all metric lists for current BatchStatistics with their labels
            List<BatchMetric> allBatchMetrics = List.of(
                    new BatchMetric("E[Ts]", batchStatistics.meanResponseTimeList),
                    new BatchMetric("E[Tq]", batchStatistics.meanQueueTimeList),
                    new BatchMetric("E[s]", batchStatistics.meanServiceTimeList),
                    new BatchMetric("E[Ns]", batchStatistics.meanSystemPopulationList),
                    new BatchMetric("E[Nq]", batchStatistics.meanQueuePopulationList),
                    new BatchMetric("ρ", batchStatistics.meanUtilizationList),
                    new BatchMetric("λ", batchStatistics.lambdaList)
            );

            // Calculate ACF for each metric list
            for (BatchMetric batchMetric : allBatchMetrics) {
                double acfValue = Math.abs(acf(batchMetric.values));
                batchMetric.setAcfValue(acfValue);
            }

            // Pass the metrics and the allOk status to the print function
            printBatchStatisticsResult(batchStatistics.getCenterName(), allBatchMetrics);
        }
    }

    public static void runModelVerificationWithBatchMeansMethodImproved() throws Exception {
        ConfigurationManager config = new ConfigurationManager();
        int batchSize = config.getInt("general", "batchSizeImproved");
        int batchesNumber = config.getInt("general", "numBatchesImproved");
        int warmupThreshold = (int) ((batchSize*batchesNumber)*config.getDouble("general", "warmupPercentageImproved"));

        BatchImprovedSimulationRunner batchRunner = new BatchImprovedSimulationRunner(batchSize, batchesNumber, warmupThreshold);
        List<BatchStatistics> batchStatisticsList = batchRunner.runBatchSimulation(true, false);

        // Iterate over each BatchStatistics object
        for (BatchStatistics batchStatistics : batchStatisticsList) {
            // List of all metric lists for current BatchStatistics with their labels
            List<BatchMetric> allBatchMetrics = List.of(
                    new BatchMetric("E[Ts]", batchStatistics.meanResponseTimeList),
                    new BatchMetric("E[Tq]", batchStatistics.meanQueueTimeList),
                    new BatchMetric("E[s]", batchStatistics.meanServiceTimeList),
                    new BatchMetric("E[Ns]", batchStatistics.meanSystemPopulationList),
                    new BatchMetric("E[Nq]", batchStatistics.meanQueuePopulationList),
                    new BatchMetric("ρ", batchStatistics.meanUtilizationList),
                    new BatchMetric("λ", batchStatistics.lambdaList)
            );

            // Calculate ACF for each metric list
            for (BatchMetric batchMetric : allBatchMetrics) {
                double acfValue = Math.abs(acf(batchMetric.values));
                batchMetric.setAcfValue(acfValue);
            }

            // Pass the metrics and the allOk status to the print function
            printBatchStatisticsResult(batchStatistics.getCenterName(), allBatchMetrics);
        }
    }

    public static double acf(List<Double> data) {
        int k = data.size();
        double mean = 0.0;

        // Calculate the mean of the batch means
        for (double value : data) {
            mean += value;
        }
        mean /= k;

        double numerator = 0.0;
        double denominator = 0.0;

        // Compute the numerator and denominator for the lag-1 autocorrelation
        for (int j = 0; j < k - 1; j++) {
            numerator += (data.get(j) - mean) * (data.get(j + 1) - mean);
        }
        for (int j = 0; j < k; j++) {
            denominator += Math.pow(data.get(j) - mean, 2);
        }
        return numerator / denominator;
    }
}

