package org.pmcsn.utils;


import org.pmcsn.configuration.ConfigurationManager;

import java.util.List;
import java.util.Scanner;

public class PrintUtils {
    public static final String RESET = "\033[0m";
    public static final String YELLOW = "\033[0;33m";
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\033[0;31m";
    public static final String BLUE = "\033[0;34m";

    public static void printTitle() {
        System.out.println(BLUE +
                "  _   _ _      _                             _                 \n" +
                " | | | (_)_ __(_)_ __   __ _   ___ _   _ ___| |_ ___ _ __ ___  \n" +
                " | |_| | | '__| | '_ \\ / _` | / __| | | / __| __/ _ \\ '_ ` _ \\ \n" +
                " |  _  | | |  | | | | | (_| | \\__ \\ |_| \\__ \\ ||  __/ | | | | |\n" +
                " |_| |_|_|_|  |_|_| |_|\\__, | |___/\\__, |___/\\__\\___|_| |_| |_|\n" +
                "                       |___/       |___/                       " + RESET);
    }



    public static String formatList(List<Double> list) {
        if (list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static void printBatchStatisticsResult(String centerName, List<Metric> metrics) {
        ConfigurationManager configurationManager = new ConfigurationManager();
        int batchSize = configurationManager.getInt("general", "batchSize");
        int numBatches = configurationManager.getInt("general", "numBatches");
        System.out.println(BLUE + "\n\n*******************************************************************************************************");
        System.out.println("AUTOCORRELATION VALUES FOR " + centerName + " WITH [B:"+batchSize+"|K:"+numBatches+"]");
        System.out.println("*******************************************************************************************************" + RESET);
        for (Metric metric : metrics) {
            String color = getAcfColor(metric.acfValue);
            System.out.printf("%s: %s%.4f%s%n", metric.name, color, metric.acfValue, RESET);
        }
        System.out.println(BLUE + "*******************************************************************************************************" + RESET);
    }

    private static String getAcfColor(double value) {
        if (value > 0.2) {
            return RED;
        } else {
            return GREEN;
        }
    }

    private static void printMetric(String name, List<Double> values) {
        System.out.printf("%s: %s%n", name, values.toString());
    }

    public static void printFinalResults(List<Verification.VerificationResult> verificationResultList) {
        String alreadyDoneCenterName = "";
        int centerIndex = 1;
        for (Verification.VerificationResult verificationResult : verificationResultList) {
            String centerName = verificationResult.name.toUpperCase();
            if (alreadyDoneCenterName.equalsIgnoreCase(centerName)) {
                centerIndex++;
                centerName = centerName + "_" + centerIndex;
            } else {
                centerIndex = 1;
                centerName = centerName + "_" + centerIndex;
            }
            System.out.println(BLUE + "\n\n*******************************************************************************************************");
            ConfigurationManager configurationManager = new ConfigurationManager();
            System.out.println("FINAL RESULTS FOR " + centerName +
                    " with " + (int) (100.0 * configurationManager.getDouble("general", "levelOfConfidence") + 0.5) +
                    "% confidence");
            System.out.println("*******************************************************************************************************" + RESET);
            printVerificationResult(verificationResult);
            System.out.println(BLUE + "*******************************************************************************************************" + RESET);
            alreadyDoneCenterName = verificationResult.name.toUpperCase();
        }
    }

    private static void printVerificationResult(Verification.VerificationResult result) {
        String within = GREEN + "within";
        String outside = RED + "outside";

        // Compute the colors and within/outside texts
        String responseTimeColor = getColor(result.comparisonResult.responseTimeDiff);
        String responseTimeWithinOutside = result.isWithinInterval(result.comparisonResult.responseTimeDiff, result.confidenceIntervals.getResponseTimeCI()) ? within : outside;

        String queueTimeColor = getColor(result.comparisonResult.queueTimeDiff);
        String queueTimeWithinOutside = result.isWithinInterval(result.comparisonResult.queueTimeDiff, result.confidenceIntervals.getQueueTimeCI()) ? within : outside;

        String serviceTimeColor = getColor(result.comparisonResult.serviceTimeDiff);
        String serviceTimeWithinOutside = result.isWithinInterval(result.comparisonResult.serviceTimeDiff, result.confidenceIntervals.getServiceTimeCI()) ? within : outside;

        String systemPopulationColor = getColor(result.comparisonResult.systemPopulationDiff);
        String systemPopulationWithinOutside = result.isWithinInterval(result.comparisonResult.systemPopulationDiff, result.confidenceIntervals.getSystemPopulationCI()) ? within : outside;

        String queuePopulationColor = getColor(result.comparisonResult.queuePopulationDiff);
        String queuePopulationWithinOutside = result.isWithinInterval(result.comparisonResult.queuePopulationDiff, result.confidenceIntervals.getQueuePopulationCI()) ? within : outside;

        String utilizationColor = getColor(result.comparisonResult.utilizationDiff);
        String utilizationWithinOutside = result.isWithinInterval(result.comparisonResult.utilizationDiff, result.confidenceIntervals.getUtilizationCI()) ? within : outside;

        String lambdaColor = getColor(result.comparisonResult.lambdaDiff);
        String lambdaWithinOutside = result.isWithinInterval(result.comparisonResult.lambdaDiff, result.confidenceIntervals.getLambdaCI()) ? within : outside;

        // Print the results
        System.out.println("E[Ts]: mean " + BLUE + result.meanStatistics.meanResponseTime + RESET + ", diff " + responseTimeColor + result.comparisonResult.responseTimeDiff + RESET + " is " + responseTimeWithinOutside + " the interval ±" + result.confidenceIntervals.getResponseTimeCI() + RESET);
        System.out.println("E[Tq]: mean " + BLUE + result.meanStatistics.meanQueueTime + RESET + ", diff " + queueTimeColor + result.comparisonResult.queueTimeDiff + RESET + " is " + queueTimeWithinOutside + " the interval ±" + result.confidenceIntervals.getQueueTimeCI() + RESET);
        System.out.println("E[s]: mean " + BLUE + result.meanStatistics.meanServiceTime + RESET + ", diff " + serviceTimeColor + result.comparisonResult.serviceTimeDiff + RESET + " is " + serviceTimeWithinOutside + " the interval ±" + result.confidenceIntervals.getServiceTimeCI() + RESET);
        System.out.println("E[Ns]: mean " + BLUE + result.meanStatistics.meanSystemPopulation + RESET + ", diff " + systemPopulationColor + result.comparisonResult.systemPopulationDiff + RESET + " is " + systemPopulationWithinOutside + " the interval ±" + result.confidenceIntervals.getSystemPopulationCI() + RESET);
        System.out.println("E[Nq]: mean " + BLUE + result.meanStatistics.meanQueuePopulation + RESET + ", diff " + queuePopulationColor + result.comparisonResult.queuePopulationDiff + RESET + " is " + queuePopulationWithinOutside + " the interval ±" + result.confidenceIntervals.getQueuePopulationCI() + RESET);
        System.out.println("ρ: mean " + BLUE + result.meanStatistics.meanUtilization + RESET + ", diff " + utilizationColor + result.comparisonResult.utilizationDiff + RESET + " is " + utilizationWithinOutside + " the interval ±" + result.confidenceIntervals.getUtilizationCI() + RESET);
        System.out.println("λ: mean " + BLUE + result.meanStatistics.lambda + RESET + ", diff " + lambdaColor + result.comparisonResult.lambdaDiff + RESET + " is " + lambdaWithinOutside + " the interval ±" + result.confidenceIntervals.getLambdaCI() + RESET);
    }

    private static String getColor(double value) {
        if (value < 0.5) {
            return GREEN;
        } else if (value < 1) {
            return YELLOW;
        } else {
            return RED;
        }
    }

    public static void printMainMenuOptions() {
        System.out.println("\nWelcome to Menara Airport Simulator!");
        System.out.println(BLUE + "Please select an option:" + RESET);
        System.out.println(BLUE + "1" + RESET + ". Start Simulation");
        System.out.println(BLUE + "2" + RESET + ". Exit");

        System.out.print(BLUE + "Enter your choice >>> " + RESET);
    }

    public static void printStartSimulationOptions() {
        System.out.println(BLUE + "\nSelect simulation Type:" + RESET);
        System.out.println(BLUE + "1" + RESET  + ". Basic Simulation");
        System.out.println(BLUE + "2" + RESET + ". Improved Model Simulation");
        System.out.println(BLUE + "3" + RESET + ". Batch Simulation");
        System.out.println(BLUE + "4" + RESET  + ". Basic Simulation with Exponential Distributions");
        System.out.println(BLUE + "5" + RESET + ". Batch Simulation with Exponential Distributions");

        System.out.print(BLUE + "Enter the simulation type number: " + RESET);
    }


    public static void resetMenu() {
        clearScreen();
        printTitle();
    }

    public static void pauseAndClear(Scanner scanner) {
        System.out.println(BLUE + "\nPress Enter to return to the menu..." + RESET);
        scanner.nextLine();
        clearScreen();
    }

    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("Error clearing the console: " + e.getMessage());
        }
    }

    public static void printError(String errorMessage){
        System.out.println(RED + errorMessage + RESET);
    }
    public static void printSuccess(String successMessage){
        System.out.println(GREEN + successMessage + RESET);
    }
    public static void printWarning(String warningMessage){
        System.out.println(YELLOW + warningMessage + RESET);
    }
}
