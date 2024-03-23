package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.io.FileInputStream;

public class App {
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            System.err.println("Could not set up logging configuration: " + e1.getMessage());
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            System.out.println("Wordle created and connected.");
        } else {
            System.out.println("Not able to connect. Sorry!");
            return;
        }
        if (wordleDatabaseConnection.createWordleTables()) {
            System.out.println("Wordle structures in place.");
        } else {
            System.out.println("Not able to launch. Sorry!");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.matches("^[a-z]{4}$")) {
                    wordleDatabaseConnection.addValidWord(i, line);
                    logger.log(Level.INFO, "Valid word added: {0}", line);
                } else {
                    logger.log(Level.SEVERE, "Invalid word from file ignored: {0}", line);
                }
                i++;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Not able to load words from file.", e);
        }

        try (Scanner scanner = new Scanner(System.in)) {
            String guess;
            do {
                System.out.print("Enter a 4 letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
                if (!guess.matches("^[a-z]{4}$") && !guess.equals("q")) {
                    logger.log(Level.WARNING, "Invalid guess made: {0}", guess);
                    System.out.println("Input is not a valid guess. Please enter a 4-letter word consisting only of lowercase letters a-z.");
                    continue;
                }

                if (!guess.equals("q")) {
                    System.out.println("You've guessed '" + guess + "'.");
                    if (wordleDatabaseConnection.isValidWord(guess)) {
                        System.out.println("Success! It is in the list.\n");
                    } else {
                        System.out.println("Sorry. This word is NOT in the list.\n");
                    }
                }
            } while (!guess.equals("q"));
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.SEVERE, "Scanner exception occurred.", e);
        }
    }
}
