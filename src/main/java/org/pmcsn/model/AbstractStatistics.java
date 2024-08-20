package org.pmcsn.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.pmcsn.utils.PrintUtils.formatList;

public abstract class AbstractStatistics {
    public enum Index {
        ServiceTime,
        QueueTime,
        Lambda,
        SystemPopulation,
        Utilization,
        QueuePopulation,
        ResponseTime
    }
    public List<Double> meanServiceTimeList = new ArrayList<>();
    public List<Double> meanQueueTimeList = new ArrayList<>();
    public List<Double> lambdaList = new ArrayList<>();
    public List<Double> meanSystemPopulationList = new ArrayList<>();
    public List<Double> meanUtilizationList = new ArrayList<>();
    public List<Double> meanQueuePopulationList = new ArrayList<>();
    public List<Double> meanResponseTimeList = new ArrayList<>();
    MeanStatistics meanStatistics = null;

    private final String centerName;

    public AbstractStatistics(String centerName) {
        this.centerName = centerName;
    }

    public MeanStatistics getMeanStatistics() {
        if(meanStatistics == null){
            meanStatistics = new MeanStatistics(this);
        }
        return meanStatistics;
    }

    public String getCenterName() {
        return centerName;
    }

    public void saveStats(Area area, MsqSum[] sum, double lastArrivalTime, double lastCompletionTime, boolean isMultiServer) {
        saveStats(area, sum, lastArrivalTime, lastCompletionTime, isMultiServer, 0);
    }

    public void saveStats(Area area, MsqSum[] sum, double lastArrivalTime, double lastCompletionTime, boolean isMultiServer, double currentBatchStartTime) {
        long numberOfJobsServed = Arrays.stream(sum).mapToLong(s -> s.served).sum();
        double lambda = numberOfJobsServed / (lastArrivalTime - currentBatchStartTime);
        add(Index.Lambda, lambdaList, lambda);
        // mean system population (E[Ns])
        double meanSystemPopulation = area.getNodeArea() / (lastCompletionTime - currentBatchStartTime);
        add(Index.SystemPopulation, meanSystemPopulationList, meanSystemPopulation);
        // mean response time (E[Ts])
        double meanResponseTime = area.getNodeArea() / numberOfJobsServed;
        add(Index.ResponseTime, meanResponseTimeList, meanResponseTime);
        double meanQueuePopulation;
        double meanQueueTime;
        double meanServiceTime;
        double utilization;
        if (isMultiServer) {
            double queueArea = area.getNodeArea();
            for (MsqSum s : sum) {
                queueArea -= s.service;
            }
            // mean queue population (E[Nq])
            meanQueuePopulation = queueArea / (lastCompletionTime - currentBatchStartTime);
            meanQueueTime = queueArea / numberOfJobsServed;
            // mean service time (E[s])
            meanServiceTime = Arrays.stream(sum)
                    .filter(s -> s.served > 0)
                    .mapToDouble(s -> s.service / s.served)
                    .average().orElseThrow();
            // mean utilization (ρ)
            utilization = (lambda * meanServiceTime) / sum.length;
        } else {
            // mean queue population (E[Nq])
            meanQueuePopulation = area.getQueueArea() / (lastCompletionTime - currentBatchStartTime);
            // mean wait time (E[Tq])
            meanQueueTime = area.getQueueArea() / numberOfJobsServed;
            // mean service time (E[s])
            meanServiceTime = sum[0].service / sum[0].served;
            // mean utilization (ρ)
            utilization = area.getServiceArea() / (lastCompletionTime - currentBatchStartTime);
        }
        add(Index.QueuePopulation, meanQueuePopulationList, meanQueuePopulation);
        add(Index.QueueTime, meanQueueTimeList, meanQueueTime);
        add(Index.Utilization, meanUtilizationList, utilization);
        add(Index.ServiceTime, meanServiceTimeList, meanServiceTime);
    }

    abstract void add(Index index, List<Double> list, double value);

    public void writeStats(String simulationType) {
        File file = new File("csvFiles/" + simulationType + "/results/");
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File("csvFiles/" + simulationType + "/results/" + centerName + ".csv");
        try (FileWriter fileWriter = new FileWriter(file)) {

            String DELIMITER = "\n";
            String COMMA = ",";
            int run;
            String name = simulationType.contains("BATCH") ? "#Batch" : "#Run";
            fileWriter.append(name).append(", E[Ts], E[Tq], E[s], E[Ns], E[Nq], ρ, λ").append(DELIMITER);

            for (run = 0; run < meanResponseTimeList.size(); run++) {
                writeRunValuesRow(fileWriter, run, COMMA, DELIMITER);
            }

            MeanStatistics meanStatistics = getMeanStatistics();
            //writeMeanStatisticsRow(meanStatistics, fileWriter, COMMA, DELIMITER);

            ConfidenceIntervals confidenceIntervals = new ConfidenceIntervals(
                    meanResponseTimeList, meanQueueTimeList, meanServiceTimeList,
                    meanSystemPopulationList, meanQueuePopulationList, meanUtilizationList, lambdaList
            );

            //writeConfidenceIntervalsRow(confidenceIntervals, fileWriter, COMMA, DELIMITER);

            fileWriter.flush();
        } catch (IOException e) {
            //ignore
        }
    }

    private void writeRunValuesRow(FileWriter fileWriter, int run, String COMMA, String DELIMITER) throws IOException {
        fileWriter.append(String.valueOf(run + 1)).append(COMMA)
                .append(String.valueOf(meanResponseTimeList.get(run))).append(COMMA)
                .append(String.valueOf(meanQueueTimeList.get(run))).append(COMMA)
                .append(String.valueOf(meanServiceTimeList.get(run))).append(COMMA)
                .append(String.valueOf(meanSystemPopulationList.get(run))).append(COMMA)
                .append(String.valueOf(meanQueueTimeList.get(run))).append(COMMA)
                .append(String.valueOf(meanUtilizationList.get(run))).append(COMMA)
                .append(String.valueOf(lambdaList.get(run))).append(DELIMITER);
    }

    private void writeMeanStatisticsRow(MeanStatistics meanStatistics, FileWriter fileWriter, String COMMA, String DELIMITER) throws IOException {
        fileWriter.append("MEAN_VALUES").append(COMMA)
                .append(String.valueOf(meanStatistics.meanResponseTime)).append(COMMA)
                .append(String.valueOf(meanStatistics.meanQueueTime)).append(COMMA)
                .append(String.valueOf(meanStatistics.meanServiceTime)).append(COMMA)
                .append(String.valueOf(meanStatistics.meanSystemPopulation)).append(COMMA)
                .append(String.valueOf(meanStatistics.meanQueuePopulation)).append(COMMA)
                .append(String.valueOf(meanStatistics.meanUtilization)).append(COMMA)
                .append(String.valueOf(meanStatistics.lambda)).append(DELIMITER);
    }

    private void writeConfidenceIntervalsRow(ConfidenceIntervals confidenceIntervals, FileWriter fileWriter, String COMMA, String DELIMITER) throws IOException {
        String PREAMBLE = "± ";
        // Write confidence intervals row
        fileWriter.append("CONFIDENCE_INTERVALS").append(COMMA)
                .append(PREAMBLE).append(String.valueOf(confidenceIntervals.getResponseTimeCI())).append(COMMA)
                .append(PREAMBLE).append(String.valueOf(confidenceIntervals.getQueueTimeCI())).append(COMMA)
                .append(PREAMBLE).append(String.valueOf(confidenceIntervals.getServiceTimeCI())).append(COMMA)
                .append(PREAMBLE).append(String.valueOf(confidenceIntervals.getSystemPopulationCI())).append(COMMA)
                .append(PREAMBLE).append(String.valueOf(confidenceIntervals.getQueuePopulationCI())).append(COMMA)
                .append(PREAMBLE).append(String.valueOf(confidenceIntervals.getUtilizationCI())).append(COMMA)
                .append(PREAMBLE).append(String.valueOf(confidenceIntervals.getLambdaCI())).append(DELIMITER);
    }

    public void printLists() {
        System.out.println("Mean Response Time List: " + formatList(meanResponseTimeList));
        System.out.println("Mean Service Time List: " + formatList(meanServiceTimeList));
        System.out.println("Mean Queue Time List: " + formatList(meanQueueTimeList));
        System.out.println("Lambda List: " + formatList(lambdaList));
        System.out.println("Mean System Population List: " + formatList(meanSystemPopulationList));
        System.out.println("Mean Utilization List: " + formatList(meanUtilizationList));
        System.out.println("Mean Queue Population List: " + formatList(meanQueuePopulationList));
    }

}
