package org.pmcsn.utils;


import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.model.BatchMetric;

import java.util.List;
import java.util.Scanner;

public class PrintUtils {
    //public static final String RESET = "\033[0m";

    public static final String RESET = "\033[0;97m";

    public static final String BRIGHT_YELLOW = "\033[0;93m";
    public static final String BRIGHT_GREEN = "\033[0;92m";
    public static final String BRIGHT_RED = "\033[0;91m";
    public static final String BRIGHT_BLUE = "\033[0;94m";
    public static final String BRIGHT_CYAN = "\033[0;96m";
    public static final String BRIGHT_MAGENTA = "\033[0;95m";
    public static final String BRIGHT_WHITE = "\033[0;97m";
    public static final String BRIGHT_BLACK = "\033[0;90m";

    public static void printTitle() {
        System.out.println(BRIGHT_RED +
                        "⠀⠀⠀⠀⠀⠀⠀⠀⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                        "⠀⠀⠀⠀⠀⠀⠀⠀⣿⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                        "⠀⠀⠀⠀⠀⠀⣦⠀⠘⢾⣦⠀⠀⠀⠀⠀⠀⠀⠀⠀  ⣠⣤⣶⢶⣦⡤⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀     ⢠⣤⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀    ⠀⣤⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                        "⠀⠀⠀⠀⡀⠀⢿⢧⠀⠈⢿⣃⡀⠀⠀⠀⠀⠀⠀ ⢸⡿⠁⠀⠀⠀⠁⠀⠀⠀ ⢀⣀⣀⣀⣀⡀⠀⢀⣀⣀⣀⣀⡀⠀ ⠀⢸⣻⣄⣀⣀⠀⠀⢀⣀⣀⣀⣀⡀⠀⢀⣀⣀⣀⣀⡀⠀⠀⠀⠀⢀⣀⣀⣀⣿⡃⠀⠀⠀ ⣀⣀⣀⡀⠀ ⠀ ⢀⣀⣀⣀⡀\n" +
                        "⠀⣠⣶⡿⣷⠀⠈⢿⣳⡀⠈⣿⣻⢷⣦⡀⠀⠀⠈⠿⣷⣤⣀⠀⠀⠀ ⠀⣾⠿⠉⠉⠙⣯⠇⠀⢸⡿⠉⠉⠙⢿⣇⠀⢸⣻⠚⠙⠋⠀⣴⡿⠋⠉⠉⣿⠇⠀⢸⡿⠉⠉⠙⢻⣷⠀⠀⣼⡟⠋⠉⠙⣾⠇⠀ ⢰⡿⠋⠉⠙⢿⣆⠀ ⢸⡿⠉⠉⠁\n" +
                        "⣰⡿⣯⡷⣿⢦⠀⠈⢿⢧⣰⢿⣻⣾⡽⣷⠀⠀⠀⠀⠀⠙⠙⠿⣶⣄⠀⢰⣿⠀⠀⠀⠀⣿⠆⠀ ⢸⡷⠀⠀⠀⢸⡿⠀⠸⣿⠀⠀⠀ ⢰⣿⠁⠀⠀⠀⣿⡆⠀ ⢸⣷⠀⠀⠀⢸⣟⠀⢰⣿⠁⠀⠀⠀ ⣿⡆⠀ ⣾⢧⣠⣄⣄⣼⣻⠀ ⢸⣳⠀⠀⠀\n" +
                        "⠸⣟⣷⢿⣯⢿⣧⡀⠨⣿⢯⣟⡿⣾⡽⡟⠀⠀⠀⠀⠀⠀⠀⠀⢸⣯⠀⠸⣯⡀⠀⠀⠀⣿⡃⠀ ⢸⢿⠀⠀⠀⢸⡿⠀⠸⣿⡀⠀⠀ ⠸⣯⡄⠀⠀⠀⣿⡆⠀ ⢸⣾⠀⠀⠀⢸⣟⠀⠸⡷⡄⠀⠀ ⠀⣿⡆ ⠀⢻⣏⠉⠈⠉⠁⠉⠀ ⢸⣻⠀⠀⠀\n" +
                        "⠀⠈⠋⠿⢾⣻⣽⣳⡼⣯⡿⣯⠿⠓⠋⠀⠀⠀⠸⢶⣶⢶⡶⠯⠋⠀⠀⠛⢿⢶⡶⠞⣷⠃⠀ ⢸⠿⠀⠀⠀⢸⢿⠀⠀⠻⢷⡶⡖⠀⠙⠽⣶⢶⠖⢯⡇⠀ ⢸⢷⠀⠀⠀⢸⣻⠀⠀⠙⠿⣶⢶⠖⡿⡆⠀  ⠈⠛⢷⢶⡶⡶⠆ ⠸⣟⠀⠀⠀\n" + RESET
        );
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

    public static void printBatchStatisticsResult(String centerName, List<BatchMetric> batchMetrics) {
        ConfigurationManager configurationManager = new ConfigurationManager();
        int batchSize = configurationManager.getInt("general", "batchSize");
        int numBatches = configurationManager.getInt("general", "numBatches");
        System.out.println(BRIGHT_RED + "\n\n*******************************************************************************************************");
        System.out.println("AUTOCORRELATION VALUES FOR " + centerName + " WITH [B:"+batchSize+"|K:"+numBatches+"]");
        System.out.println("*******************************************************************************************************" + RESET);
        for (BatchMetric batchMetric : batchMetrics) {
            String color = getAcfColor(batchMetric.acfValue);
            System.out.printf("%s: %s%.4f%s%n", batchMetric.name, color, batchMetric.acfValue, RESET);
        }
        System.out.println(BRIGHT_RED + "*******************************************************************************************************" + RESET);
    }

    private static String getAcfColor(double value) {
        if (value > 0.2) {
            return BRIGHT_RED;
        } else {
            return BRIGHT_GREEN;
        }
    }

    private static void printMetric(String name, List<Double> values) {
        System.out.printf("%s: %s%n", name, values.toString());
    }

    public static void printFinalResults(List<Verification.VerificationResult> verificationResultList) {
        for (Verification.VerificationResult verificationResult : verificationResultList) {
            String centerName = verificationResult.name.toUpperCase();
            System.out.println(BRIGHT_RED + "\n\n*******************************************************************************************************");
            ConfigurationManager configurationManager = new ConfigurationManager();
            System.out.println("FINAL RESULTS FOR " + centerName +
                    " with " + (int) (100.0 * configurationManager.getDouble("general", "levelOfConfidence") + 0.5) +
                    "% confidence");
            System.out.println("*******************************************************************************************************" + RESET);
            printVerificationResult(verificationResult);
            System.out.println(BRIGHT_RED + "*******************************************************************************************************" + RESET);
        }
    }

    private static void printVerificationResult(Verification.VerificationResult result) {
        String within = BRIGHT_GREEN + "within";
        String outside = BRIGHT_RED + "outside";

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
        System.out.println(BRIGHT_RED + "E[Ts]: mean " + RESET + result.meanStatistics.meanResponseTime + RESET + ", diff " + responseTimeColor + result.comparisonResult.responseTimeDiff + RESET + " is " + responseTimeWithinOutside + " the interval ±" + result.confidenceIntervals.getResponseTimeCI() + RESET);
        System.out.println(BRIGHT_RED + "E[Tq]: mean " + RESET + result.meanStatistics.meanQueueTime + RESET + ", diff " + queueTimeColor + result.comparisonResult.queueTimeDiff + RESET + " is " + queueTimeWithinOutside + " the interval ±" + result.confidenceIntervals.getQueueTimeCI() + RESET);
        System.out.println(BRIGHT_RED + "E[s]: mean " + RESET + result.meanStatistics.meanServiceTime + RESET + ", diff " + serviceTimeColor + result.comparisonResult.serviceTimeDiff + RESET + " is " + serviceTimeWithinOutside + " the interval ±" + result.confidenceIntervals.getServiceTimeCI() + RESET);
        System.out.println(BRIGHT_RED + "E[Ns]: mean " + RESET + result.meanStatistics.meanSystemPopulation + RESET + ", diff " + systemPopulationColor + result.comparisonResult.systemPopulationDiff + RESET + " is " + systemPopulationWithinOutside + " the interval ±" + result.confidenceIntervals.getSystemPopulationCI() + RESET);
        System.out.println(BRIGHT_RED + "E[Nq]: mean " + RESET + result.meanStatistics.meanQueuePopulation + RESET + ", diff " + queuePopulationColor + result.comparisonResult.queuePopulationDiff + RESET + " is " + queuePopulationWithinOutside + " the interval ±" + result.confidenceIntervals.getQueuePopulationCI() + RESET);
        System.out.println(BRIGHT_RED + "ρ: mean " + RESET + result.meanStatistics.meanUtilization + RESET + ", diff " + utilizationColor + result.comparisonResult.utilizationDiff + RESET + " is " + utilizationWithinOutside + " the interval ±" + result.confidenceIntervals.getUtilizationCI() + RESET);
        System.out.println(BRIGHT_RED + "λ: mean " + RESET + result.meanStatistics.lambda + RESET + ", diff " + lambdaColor + result.comparisonResult.lambdaDiff + RESET + " is " + lambdaWithinOutside + " the interval ±" + result.confidenceIntervals.getLambdaCI() + RESET);
    }

    private static String getColor(double value) {
        if (value < 0.5) {
            return BRIGHT_GREEN;
        } else if (value < 1) {
            return BRIGHT_YELLOW;
        } else {
            return BRIGHT_RED;
        }
    }

    public static void printMainMenuOptions() {
        System.out.println("\nWelcome to Menara Airport Simulator!");
        System.out.println(BRIGHT_RED + "Please select an option:" + RESET);
        System.out.println(BRIGHT_RED + "1" + RESET + ". Start Simulation");
        System.out.println(BRIGHT_RED + "2" + RESET + ". Exit");

        System.out.print(BRIGHT_RED + "Enter your choice >>> " + RESET);
    }

    public static void printStartSimulationOptions() {
        System.out.println(BRIGHT_RED + "\nSelect simulation Type:" + RESET);
        System.out.println(BRIGHT_RED + "1" + RESET  + ". Basic Simulation");
        System.out.println(BRIGHT_RED + "3" + RESET + ". Basic Simulation VERIFICATION");
        System.out.println(BRIGHT_RED + "2" + RESET + ". Improved Model Simulation");
        System.out.println(BRIGHT_RED + "3" + RESET + ". Improved Model Simulation VERIFICATION");

        System.out.print(BRIGHT_RED + "Enter the simulation type number: " + RESET);
    }


    public static void resetMenu() {
        clearScreen();
        printTitle();
    }

    public static void pauseAndClear(Scanner scanner) {
        System.out.println(BRIGHT_RED + "\nPress Enter to return to the menu..." + RESET);
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
        System.out.println(BRIGHT_MAGENTA + errorMessage + RESET);
    }
    public static void printSuccess(String successMessage){
        System.out.println(BRIGHT_GREEN + successMessage + RESET);
    }
    public static void printDebug(String warningMessage){
        System.out.println(BRIGHT_RED + warningMessage + RESET);
    }
    public static void printWarning(String warningMessage){
        System.out.println(BRIGHT_YELLOW + warningMessage + RESET);
    }
}
