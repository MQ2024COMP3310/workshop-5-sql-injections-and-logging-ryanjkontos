package workshop05code;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SQLiteConnectionManager {
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            System.err.println("Could not set up logging configuration: " + e1.getMessage());
        }
    }

    private static final Logger logger = Logger.getLogger(SQLiteConnectionManager.class.getName());

    private String databaseURL = "";

    public SQLiteConnectionManager(String filename) {
        databaseURL = "jdbc:sqlite:sqlite/" + filename;
    }

    public void createNewDatabase(String fileName) {
        try (Connection conn = DriverManager.getConnection(databaseURL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.log(Level.INFO, "The driver name is {0}", new Object[]{meta.getDriverName()});
                logger.info("A new database has been created.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Could not create new database.", e);
        }
    }

    public boolean checkIfConnectionDefined() {
        if (databaseURL.isEmpty()) {
            return false;
        } else {
            try (Connection conn = DriverManager.getConnection(databaseURL)) {
                return                conn != null;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Connection check failed.", e);
                return false;
            }
        }
    }

    public boolean createWordleTables() {
        if (databaseURL.isEmpty()) {
            return false;
        } else {
            try (Connection conn = DriverManager.getConnection(databaseURL);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS wordlist;");
                stmt.execute("CREATE TABLE wordlist (\n"
                        + " id integer PRIMARY KEY,\n"
                        + " word text NOT NULL\n"
                        + ");");
                stmt.execute("DROP TABLE IF EXISTS validWords;");
                stmt.execute("CREATE TABLE validWords (\n"
                        + " id integer PRIMARY KEY,\n"
                        + " word text NOT NULL\n"
                        + ");");
                return true;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to create table structures.", e);
                return false;
            }
        }
    }

    public void addValidWord(int id, String word) {
        String pattern = "^[a-z]{4}$";

        if (!word.matches(pattern)) {
            logger.log(Level.SEVERE, "Attempted to add an invalid word to the database: {0}", word);
            return;
        }

        String sql = "INSERT INTO validWords(id,word) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(databaseURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, word);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Exception during addValidWord.", e);
        }
    }

    public boolean isValidWord(String guess) {
        String pattern = "^[a-z]{4}$";

        if (!guess.matches(pattern)) {
            logger.log(Level.WARNING, "Invalid guess: {0}", guess);
            return false;
        }

        String sql = "SELECT count(id) as total FROM validWords WHERE word = ?";
        try (Connection conn = DriverManager.getConnection(databaseURL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, guess);

            ResultSet resultRows = stmt.executeQuery();
            if (resultRows.next()) {
                int result = resultRows.getInt("total");
                return result >= 1;
            }

            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Exception during isValidWord.", e);
            return false;
        }
    }
}

