package org.pmcsn.utils;

import org.pmcsn.controller.BatchSimulationRunner;
import org.pmcsn.model.BatchStatistics;

import java.util.List;

import static org.pmcsn.utils.PrintUtils.printBatchStatisticsResult;

public class BatchMeans {
    public static void main(String[] args) throws Exception {
        BatchSimulationRunner batchRunner = new BatchSimulationRunner();
        List<BatchStatistics> batchStatisticsList = batchRunner.runBatchSimulation(true);

        // Iterate over each BatchStatistics object
        for (BatchStatistics batchStatistics : batchStatisticsList) {
            // List of all metric lists for current BatchStatistics with their labels
            List<Metric> allMetrics = List.of(
                    new Metric("E[Ts]", batchStatistics.meanResponseTimeList),
                    new Metric("E[Tq]", batchStatistics.meanQueueTimeList),
                    new Metric("E[s]", batchStatistics.meanServiceTimeList),
                    new Metric("E[Ns]", batchStatistics.meanSystemPopulationList),
                    new Metric("E[Nq]", batchStatistics.meanQueuePopulationList),
                    new Metric("ρ", batchStatistics.meanUtilizationList),
                    new Metric("λ", batchStatistics.lambdaList)
            );

            // Calculate ACF for each metric list
            for (Metric metric : allMetrics) {
                double acfValue = Math.abs(acf(metric.values));
                metric.setAcfValue(acfValue);
            }

            // Pass the metrics and the allOk status to the print function
            printBatchStatisticsResult(batchStatistics.getCenterName(), allMetrics);
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

// Helper class to hold metric name, values, and ACF value
class Metric {
    String name;
    List<Double> values;
    double acfValue;

    Metric(String name, List<Double> values) {
        this.name = name;
        this.values = values;
    }

    public void setAcfValue(double acfValue) {
        this.acfValue = acfValue;
    }
}