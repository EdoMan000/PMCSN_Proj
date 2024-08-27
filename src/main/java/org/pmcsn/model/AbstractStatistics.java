package org.pmcsn.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractStatistics {
    private final Logger logger = Logger.getLogger(AbstractStatistics.class.getName());

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
        // mean queue population (E[Nq])
        double meanQueuePopulation = area.getQueueArea() / (lastCompletionTime - currentBatchStartTime);
        add(Index.QueuePopulation, meanQueuePopulationList, meanQueuePopulation);
        // mean wait time (E[Tq])
        double meanQueueTime = area.getQueueArea() / numberOfJobsServed;
        add(Index.QueueTime, meanQueueTimeList, meanQueueTime);
        double meanServiceTime;
        double utilization;
        if (isMultiServer) {
            // mean service time (E[s])
            meanServiceTime = Arrays.stream(sum)
                    .filter(s -> s.served > 0)
                    .mapToDouble(s -> s.service / s.served)
                    .average().orElseThrow();
            // mean utilization (ρ)
            utilization = (lambda * meanServiceTime) / sum.length;
        } else {
            // mean service time (E[s])
            meanServiceTime = sum[0].service / sum[0].served;
            // mean utilization (ρ)
            utilization = area.getServiceArea() / (lastCompletionTime - currentBatchStartTime);
        }
        add(Index.Utilization, meanUtilizationList, utilization);
        add(Index.ServiceTime, meanServiceTimeList, meanServiceTime);
    }

    abstract void add(Index index, List<Double> list, double value);

    public void writeStats(String simulationType, long seed) {
        File parent = Path.of("csvFiles", simulationType, String.valueOf(seed), "results").toFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                logger.severe("Failed to create directory: " + parent.getPath());
                System.exit(1);
            }
        }
        File file = new File(parent, centerName + ".csv");
        try (FileWriter fileWriter = new FileWriter(file)) {
            String DELIMITER = "\n";
            String COMMA = ",";
            int run;
            String name = simulationType.contains("BATCH") ? "#Batch" : "#Run";
            fileWriter.append(name).append(", E[Ts], E[Tq], E[s], E[Ns], E[Nq], ρ, λ").append(DELIMITER);
            for (run = 0; run < meanResponseTimeList.size(); run++) {
                writeRunValuesRow(fileWriter, run, COMMA, DELIMITER);
            }
            fileWriter.flush();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    private void writeRunValuesRow(FileWriter fileWriter, int run, String COMMA, String DELIMITER) throws IOException {
        fileWriter.append(String.valueOf(run + 1)).append(COMMA)
                .append(String.valueOf(meanResponseTimeList.get(run))).append(COMMA)
                .append(String.valueOf(meanQueueTimeList.get(run))).append(COMMA)
                .append(String.valueOf(meanServiceTimeList.get(run))).append(COMMA)
                .append(String.valueOf(meanSystemPopulationList.get(run))).append(COMMA)
                .append(String.valueOf(meanQueuePopulationList.get(run))).append(COMMA)
                .append(String.valueOf(meanUtilizationList.get(run))).append(COMMA)
                .append(String.valueOf(lambdaList.get(run))).append(DELIMITER);
    }

}
