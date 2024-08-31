package org.pmcsn.utils;

import org.pmcsn.controller.FiniteImprovedSimulationRunner;
import org.pmcsn.controller.FiniteSimulationRunner;
import org.pmcsn.libraries.Rngs;
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

public class PlotUtils {
    private PlotUtils() {}

    public static void writeObservations(String path, List<Observations> observationsList) {
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

    public static void writeObservations(String path, Observations observations) {
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

    public static void main(String[] args) throws Exception {
        FileUtils.deleteDirectory("csvFiles/FINITE_SIMULATION");
        FileUtils.deleteDirectory("csvFiles/IMPROVED_FINITE_SIMULATION");
        Rngs rngs = new Rngs();
        rngs.selectStream(255);
        long[] seeds = new long[1];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = (long) (rngs.random() * (Long.MAX_VALUE));
        }
        for (long seed : seeds) {
            FiniteSimulationRunner runner = new FiniteSimulationRunner(seed);
            runner.runFiniteSimulation(false, true, false);
            FiniteImprovedSimulationRunner iRunner = new FiniteImprovedSimulationRunner(seed);
            iRunner.runImprovedModelSimulation(false, true, false);
            PlotUtils.welchPlot("csvFiles/IMPROVED_FINITE_SIMULATION/%d/observations".formatted(seed));
            PlotUtils.welchPlot("csvFiles/FINITE_SIMULATION/%d/observations".formatted(seed));
        }
    }

    public static void welchPlot(String parent) throws IOException {
        List<Path> files = listAllFiles(Path.of(parent));
        for (Path file : files) {
            List<List<Double>> matrix = new ArrayList<>();
            for (String line : Files.readAllLines(file)) {
                if (line.trim().split(",").length >= 30) {
                    matrix.add(Arrays.stream(line.trim().split(","))
                            .mapToDouble(Double::parseDouble)
                            .boxed()
                            .collect(Collectors.toList()));
                }
            }
            List<Double> plot = finiteSimulationPlot(matrix);
            String plotPath = file.toString().replace(".data", "_plot.csv");
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
}