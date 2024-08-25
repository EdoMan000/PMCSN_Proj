package org.pmcsn.utils;

import org.pmcsn.model.Observations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WelchPlot {
    private WelchPlot() {}

    public static void writeObservations(String simulationType, List<Observations> observationsList) {
        String path = "csvFiles/%s/observations/".formatted(simulationType);
        FileUtils.createDirectoryIfNotExists(path);
        File parent = new File(path);
        List<Double> row = new ArrayList<>();
        int minSize = observationsList.stream().mapToInt(x -> x.getPoints().size()).min().orElseThrow();
        for (int i = 0; i < minSize; i++) {
            double s = 0;
            for (Observations o : observationsList) {
                s += o.getPoints().get(i);
            }
            row.add(s / observationsList.size());
        }
        String centerName = observationsList.getFirst().getCenterName();
        String[] s = centerName.split("_");
        centerName = String.join("_", Arrays.copyOfRange(s, 0, s.length - 1));
        File file = new File(parent, "%s.data".formatted(centerName));
        writeRow(file, row);
    }

    public static void writeObservations(String simulationType, Observations observations) {
        String path = "csvFiles/%s/observations/".formatted(simulationType);
        FileUtils.createDirectoryIfNotExists(path);
        File parent = new File(path);
        File file = new File(parent, "%s.data".formatted(observations.getCenterName()));
        writeRow(file, observations);
    }

    private static void writeRow(File file, Observations observations) {
        writeRow(file, observations.getPoints());
    }

    private static void writeRow(File file, List<Double> points) {
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            StringBuilder row = new StringBuilder();
            points.forEach(p -> row.append(p.toString()).append(","));
            fileWriter.write(row.append("\n").toString());
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        WelchPlot.welchPlot("csvFiles/%s/observations".formatted("FINITE_SIMULATION"));
    }

    public static void welchPlot(String parent) throws IOException {
        List<Path> files = listAllFiles(Path.of(parent));
        for (Path file : files) {
            List<List<Double>> matrix = new ArrayList<>();
            for (String line : Files.readAllLines(file)) {
                matrix.add(Arrays.stream(line.trim().split(","))
                        .mapToDouble(Double::parseDouble)
                        .boxed()
                        .collect(Collectors.toList()));
            }
            List<Double> plot = finiteSimulationPlot(matrix);
            String plotPath = file.toString().replace(".data", "_time_plot.csv");
            savePlot(plotPath, plot);
            plot = welchPlot2(matrix);
            plotPath = file.toString().replace(".data", "_welch_plot.csv");
            savePlot(plotPath, plot);
        }
    }

    private static List<Double> finiteSimulationPlot(List<List<Double>> matrix) {
        int minSize = matrix.stream().mapToInt(List::size).min().orElseThrow();
        List<Double> averages = new ArrayList<>();
        for (int i = 0; i < minSize; i++) {
            double sum = 0.0;
            for (int j = 0; j < matrix.size(); j++) {
                sum += matrix.get(j).get(i);
            }
            averages.add(sum / matrix.size());
        }
        return averages;
    }

    private static void savePlot(String plotPath, List<Double> plot) {
        try (FileWriter w = new FileWriter(plotPath)) {
            StringBuilder s = new StringBuilder();
            s.append("E[Ts]\n");
            plot.forEach(x -> s.append(x).append("\n"));
            w.write(s.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Path> listAllFiles(Path parent) throws IOException {
        List<Path> paths = new ArrayList<>();
        paths.add(parent);
        List<Path> files = new ArrayList<>();
        while (!paths.isEmpty()) {
            var path = paths.removeLast();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
            {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        if (!Files.isHidden(entry)) {
                            paths.add(entry);
                        }
                    } else if (entry.toString().endsWith(".data")) {
                        files.add(entry);
                    }
                }
            }
        }
        return files;
    }

    public static List<Double> welchPlot2(List<List<Double>> matrix) {
        int m = matrix.stream().filter(r -> !r.isEmpty()).mapToInt(List::size).min().orElseThrow();
        List<Double> ensembleAverage = new ArrayList<>(m);
        int n = matrix.size();
        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += matrix.get(j).get(i);
            }
            ensembleAverage.add(sum / n);
        }

        int w = Math.min(m/4, 10);
        List<Double> points = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            if (i >= w + 1 && i <= m - w) {
                double y = 0.0;
                for (int s = -w; s < w; ++s) {
                    y += ensembleAverage.get(i + s);
                }
                points.add(y/(2*w+1));
            } else if (i >= 1 && i < w) {
                double y = 0.0;
                for (int s = -(i-1); s < i-1; ++s) {
                    y += ensembleAverage.get(i + s);
                }
                points.add(y/(2*i-1));
            }
        }
        return points;
    }

    public static List<Double> welchPlot(List<List<Double>> matrix) {
        int m = matrix.stream().filter(r -> !r.isEmpty()).mapToInt(List::size).min().orElseThrow();
        List<Double> ensembleAverage = new ArrayList<>(m);

        // Calculate ensemble averages
        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            int count = 0;
            for (List<Double> row : matrix) {
                if (!row.isEmpty()) {
                    sum += row.get(i);
                    count += 1;
                }
            }
            ensembleAverage.add(sum / count);
        }
        // Define window size (w)
        int w = Math.min(m / 4, 5); // Choose w as min(m/4, 5)
        // Calculate moving averages
        List<Double> movingAverage = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            int count = 0;

            if (i < w) {
                for (int s = -i; s <= i; s++) {
                    sum += ensembleAverage.get(i + s);
                    count++;
                }
                movingAverage.add(sum / count);
            } else if (i >= m - w) {
                for (int s = -(m - i - 1); s <= (m - i - 1); s++) {
                    sum += ensembleAverage.get(i + s);
                    count++;
                }
                movingAverage.add(sum / count);
            } else {
                for (int s = -w; s <= w; s++) {
                    sum += ensembleAverage.get(i + s);
                    count++;
                }
                movingAverage.add(sum / (2 * w + 1));
            }
        }
        return movingAverage;
    }
}
