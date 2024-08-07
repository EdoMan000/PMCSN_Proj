package org.pmcsn;

import java.util.Scanner;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.controller.BatchSimulationRunner;
import org.pmcsn.utils.FileUtils;

import static org.pmcsn.utils.PrintUtils.*;

public class HiringSystemSimulator {

    public static void main(String[] args) throws Exception {
        FileUtils.deleteDirectory("csvFiles");
        Scanner scanner = new Scanner(System.in);
        BatchSimulationRunner batchRunner = new BatchSimulationRunner();

        while (true) {
            mainMenu(scanner, batchRunner);
        }
    }
    private static void mainMenu(Scanner scanner, BatchSimulationRunner batchRunner) throws Exception {
        resetMenu();
        printMainMenuOptions();
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        switch (choice) {
            case 1:
                startSimulation(scanner, batchRunner);
                break;
            case 2:
                printError("Exiting Simulator. Goodbye!");
                System.exit(0);
                break;
            default:
                printError("Invalid choice '" + choice + "'. Please try again.");
                pauseAndClear(scanner);
        }
    }

    private static void startSimulation(Scanner scanner, BatchSimulationRunner batchRunner) throws Exception {
        resetMenu();
        printStartSimulationOptions();
        int simulationType = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        ConfigurationManager configurationManager = new ConfigurationManager();
        //boolean shouldTrackObservations = configurationManager.getBoolean("general", "shouldTrackObservations");
        switch (simulationType) {
            case 1:
                //basicRunner.runBasicSimulation(false, shouldTrackObservations);
                break;
            case 2:
                //improvedRunner.runImprovedModelSimulation();
                break;
            case 3:
                batchRunner.runBatchSimulation(false);
                break;
            case 4:
                //basicRunner.runBasicSimulation(true, shouldTrackObservations);
                break;
            case 5:
                batchRunner.runBatchSimulation(true);
                break;
            default:
                printError("Invalid simulation type '" + simulationType + "'.");
        }
        pauseAndClear(scanner);
    }
}
