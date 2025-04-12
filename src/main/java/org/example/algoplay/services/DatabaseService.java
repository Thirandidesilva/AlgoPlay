package org.example.algoplay.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:algoplay.db";

    public void initializeDatabase() {
        try {
            // Create connection
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Database connection established");

            // Create tables if they don't exist
            createTables();

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        Statement statement = connection.createStatement();

        // Create Players table
        statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL)");

        // Create GameResults table
        statement.execute("CREATE TABLE IF NOT EXISTS game_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_id INTEGER," +
                "game_type TEXT NOT NULL," +
                "is_correct BOOLEAN," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (player_id) REFERENCES players(id))");

        // Create AlgorithmPerformance table
        statement.execute("CREATE TABLE IF NOT EXISTS algorithm_performance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "game_type TEXT NOT NULL," +
                "algorithm_name TEXT NOT NULL," +
                "execution_time_ms INTEGER," +
                "game_round INTEGER," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        // Game-specific tables
        createTicTacToeTable(statement);
        createTravelingSalesmanTable(statement);
        createTowerOfHanoiTable(statement);
        createEightQueensTable(statement);
        createKnightsTourTable(statement);

        statement.close();
    }

    private void createTicTacToeTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS tictactoe_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_id INTEGER," +
                "game_result TEXT," + // win, lose, draw
                "algorithm_used TEXT," +
                "response TEXT," +
                "FOREIGN KEY (player_id) REFERENCES players(id))");
    }

    private void createTravelingSalesmanTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS tsp_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_id INTEGER," +
                "home_city TEXT," +
                "selected_cities TEXT," +
                "shortest_route TEXT," +
                "algorithm_used TEXT," +
                "FOREIGN KEY (player_id) REFERENCES players(id))");
    }

    private void createTowerOfHanoiTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS tower_of_hanoi_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_id INTEGER," +
                "disk_count INTEGER," +
                "moves_count INTEGER," +
                "move_sequence TEXT," +
                "algorithm_used TEXT," +
                "FOREIGN KEY (player_id) REFERENCES players(id))");
    }

    private void createEightQueensTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS eight_queens_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_id INTEGER," +
                "solution_id INTEGER," +
                "solution TEXT," +
                "is_recognized BOOLEAN DEFAULT false," +
                "FOREIGN KEY (player_id) REFERENCES players(id))");
    }

    private void createKnightsTourTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS knights_tour_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_id INTEGER," +
                "start_position TEXT," +
                "tour_sequence TEXT," +
                "algorithm_used TEXT," +
                "FOREIGN KEY (player_id) REFERENCES players(id))");
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}