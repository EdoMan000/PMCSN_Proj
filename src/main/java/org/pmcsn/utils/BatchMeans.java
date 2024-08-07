package org.pmcsn.utils;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.controller.BatchSimulationRunner;
import org.pmcsn.model.BatchStatistics;

import java.util.List;

import static org.pmcsn.utils.PrintUtils.printError;
import static org.pmcsn.utils.PrintUtils.printSuccess;

public class BatchMeans {
    public static void main(String[] args) throws Exception {
        BatchSimulationRunner batchRunner = new BatchSimulationRunner();
        List<BatchStatistics> batchStatisticsList = batchRunner.runBatchSimulation(true);

        boolean allOk = true;

        // Iterate over each BatchStatistics object
        for (BatchStatistics batchStatistics : batchStatisticsList) {
            // List of all metric lists for current BatchStatistics with their labels
            List<Metric> allMetrics = List.of(
                    new Metric("Mean Response Time", batchStatistics.meanResponseTimeList),
                    new Metric("Mean Service Time", batchStatistics.meanServiceTimeList),
                    new Metric("Mean Queue Time", batchStatistics.meanQueueTimeList),
                    new Metric("Lambda", batchStatistics.lambdaList),
                    new Metric("Mean System Population", batchStatistics.meanSystemPopulationList),
                    new Metric("Mean Utilization", batchStatistics.meanUtilizationList),
                    new Metric("Mean Queue Population", batchStatistics.meanQueuePopulationList)
            );

            // Calculate and print ACF for each metric list
            for (Metric metric : allMetrics) {
                double acfValue = Math.abs(acf(metric.values));
                System.out.printf("Metric: %s, abs(ACF): %.4f%n", metric.name, acfValue);
                if (acfValue > 0.2) {
                    allOk = false;
                }
            }

            if (allOk) {
                printSuccess("B and K are OK!.");
            } else {
                printError("B and K are NOT OK!.");
            }

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

// Helper class to hold metric name and values
class Metric {
    String name;
    List<Double> values;

    Metric(String name, List<Double> values) {
        this.name = name;
        this.values = values;
    }
}
