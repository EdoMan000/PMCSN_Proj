package org.pmcsn;

import java.util.List;
import java.util.Scanner;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.controller.*;
import org.pmcsn.model.BatchStatistics;
import org.pmcsn.utils.FileUtils;

import static org.pmcsn.utils.PrintUtils.*;

public class MAACFinanceSimulator {

    public static void main(String[] args) throws Exception {
        //FileUtils.deleteDirectory("csvFiles");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            mainMenu(scanner);
        }
    }
    private static void mainMenu(Scanner scanner) throws Exception {
        resetMenu();
        printMainMenuOptions();
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        switch (choice) {
            case 1:
                startSimulation(scanner);
                break;
            case 2:
                printError("Exiting MAAC Finance Simulator. Goodbye!");
                System.exit(0);
                break;
            default:
                printError("Invalid choice '" + choice + "'. Please try again.");
                pauseAndClear(scanner);
        }
    }

    private static void startSimulation(Scanner scanner) throws Exception {
        resetMenu();
        printStartSimulationOptions();


        int simulationType = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        ConfigurationManager configurationManager = new ConfigurationManager();
        boolean shouldTrackObservations = configurationManager.getBoolean("general", "shouldTrackObservations");
        FiniteSimulationRunner basicRunner = new FiniteSimulationRunner();
        BatchSimulationRunner batchRunner = new BatchSimulationRunner(configurationManager.getInt("general", "batchSize"), configurationManager.getInt("general", "numBatches"));
        BatchImprovedSimulationRunner batchImprovedRunner = new BatchImprovedSimulationRunner(configurationManager.getInt("general", "batchSize"), configurationManager.getInt("general", "numBatches"));
        FiniteImprovedSimulationRunner improvedRunner = new FiniteImprovedSimulationRunner();

        switch (simulationType) {
            case 1:
                basicRunner.runFiniteSimulation(false, shouldTrackObservations, false);
                break;
            case 2:
                batchRunner.runBatchSimulation(true, false);
                break;
            case 3:
                improvedRunner.runImprovedModelSimulation(false, shouldTrackObservations, false);
                break;
            case 4:
                batchImprovedRunner.runBatchSimulation(true, false);
                break;
            case 5:
                improvedRunner.runImprovedModelSimulation(false, shouldTrackObservations, true);
                break;
            default:
                printError("Invalid simulation type '" + simulationType + "'.");
        }
        pauseAndClear(scanner);
    }
}
