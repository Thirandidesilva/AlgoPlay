package org.example.algoplay.services;

import org.example.algoplay.services.DatabaseService;
import org.example.algoplay.controllers.games.KTController.Point;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    // Reference to the central database service
    private final DatabaseService dbService;
    // Reference to the user session service
    private final UserSessionService userSessionService;

    // SQL statements for table creation and data manipulation
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS knight_tour_solutions (" +
                    "id SERIAL PRIMARY KEY, " +
                    "game_id INTEGER DEFAULT 5 REFERENCES games(game_id) ON DELETE CASCADE, " +
                    "user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE, " +
                    "algorithm VARCHAR(50) NOT NULL, " +
                    "start_position VARCHAR(10) NOT NULL, " +
                    "solution_path TEXT NOT NULL, " +
                    "execution_time BIGINT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    /**
     * Constructor - gets the database service instance and creates table if it doesn't exist
     */
    public DatabaseManager() {
        // Get the singleton instance of the central database service
        this.dbService = DatabaseService.getInstance();

        // Get the singleton instance of the user session service
        this.userSessionService = UserSessionService.getInstance();

        try {
            // Create the table if it doesn't exist
            dbService.executeUpdate(CREATE_TABLE_SQL);
            System.out.println("Knight Tour database table initialized successfully!");
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves a knight tour solution to the database
     */
    public void saveSolution(String algorithm, String startPosition,
                             String solutionPath, long executionTime) {
        // Get current user ID from session
        int userId = userSessionService.getCurrentUserId();

        // Check if user is logged in
        if (userId == -1) {
            System.err.println("Cannot save solution: No user logged in");
            return;
        }

        String sql = "INSERT INTO knight_tour_solutions (game_id, user_id, algorithm, start_position, solution_path, execution_time) " +
                "VALUES (5, ?, ?, ?, ?, ?)";

        boolean success = dbService.executeUpdate(sql, userId, algorithm, startPosition, solutionPath, executionTime);

        if (success) {
            System.out.println("Solution saved successfully");
        } else {
            System.err.println("Failed to save solution");
        }
    }

    /**
     * Retrieves all solutions from the database
     */
    public List<SolutionRecord> getAllSolutions() {
        List<SolutionRecord> solutions = new ArrayList<>();
        String sql = "SELECT ks.*, u.username FROM knight_tour_solutions ks " +
                "JOIN users u ON ks.user_id = u.user_id " +
                "ORDER BY ks.created_at DESC";

        try (ResultSet rs = dbService.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    SolutionRecord record = new SolutionRecord(
                            rs.getInt("id"),
                            rs.getInt("game_id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("algorithm"),
                            rs.getString("start_position"),
                            rs.getString("solution_path"),
                            rs.getLong("execution_time"),
                            rs.getTimestamp("created_at")
                    );
                    solutions.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving solutions: " + e.getMessage());
            e.printStackTrace();
        }

        return solutions;
    }

    /**
     * Get average execution time grouped by algorithm
     */
    public Map<String, Double> getAverageExecutionTimeByAlgorithm() {
        Map<String, Double> averageTimes = new HashMap<>();
        String sql = "SELECT algorithm, AVG(execution_time) as avg_time FROM knight_tour_solutions GROUP BY algorithm";

        try (ResultSet rs = dbService.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    averageTimes.put(rs.getString("algorithm"), rs.getDouble("avg_time"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving average times: " + e.getMessage());
            e.printStackTrace();
        }

        return averageTimes;
    }

    /**
     * Get count of solutions by algorithm
     */
    public Map<String, Integer> getSolutionCountByAlgorithm() {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT algorithm, COUNT(*) as count FROM knight_tour_solutions GROUP BY algorithm";

        try (ResultSet rs = dbService.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    counts.put(rs.getString("algorithm"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving solution counts: " + e.getMessage());
            e.printStackTrace();
        }

        return counts;
    }

    /**
     * Retrieves solutions for a specific user
     */
    public List<SolutionRecord> getSolutionsForCurrentUser() {
        List<SolutionRecord> solutions = new ArrayList<>();

        // Get current user ID from session
        int userId = userSessionService.getCurrentUserId();

        // Check if user is logged in
        if (userId == -1) {
            System.err.println("Cannot retrieve solutions: No user logged in");
            return solutions;
        }

        String sql = "SELECT ks.*, u.username FROM knight_tour_solutions ks " +
                "JOIN users u ON ks.user_id = u.user_id " +
                "WHERE ks.user_id = ? ORDER BY ks.created_at DESC";

        try (ResultSet rs = dbService.executeQuery(sql, userId)) {
            if (rs != null) {
                while (rs.next()) {
                    SolutionRecord record = new SolutionRecord(
                            rs.getInt("id"),
                            rs.getInt("game_id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("algorithm"),
                            rs.getString("start_position"),
                            rs.getString("solution_path"),
                            rs.getLong("execution_time"),
                            rs.getTimestamp("created_at")
                    );
                    solutions.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user solutions: " + e.getMessage());
            e.printStackTrace();
        }

        return solutions;
    }

    /**
     * Converts a solution path string to a list of points
     */
    public List<Point> convertPathStringToPoints(String pathString) {
        List<Point> points = new ArrayList<>();
        String[] coords = pathString.split(";");

        for (String coord : coords) {
            if (!coord.isEmpty()) {
                String[] xy = coord.split(",");
                if (xy.length == 2) {
                    try {
                        int x = Integer.parseInt(xy[0]);
                        int y = Integer.parseInt(xy[1]);
                        points.add(new Point(x, y));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing coordinates: " + coord);
                    }
                }
            }
        }

        return points;
    }

    /**
     * Record class to hold solution data
     */
    public static class SolutionRecord {
        private final int id;
        private final int gameId;
        private final int userId;
        private final String username;
        private final String algorithm;
        private final String startPosition;
        private final String solutionPath;
        private final long executionTime;
        private final Timestamp createdAt;

        public SolutionRecord(int id, int gameId, int userId, String username, String algorithm,
                              String startPosition, String solutionPath, long executionTime, Timestamp createdAt) {
            this.id = id;
            this.gameId = gameId;
            this.userId = userId;
            this.username = username;
            this.algorithm = algorithm;
            this.startPosition = startPosition;
            this.solutionPath = solutionPath;
            this.executionTime = executionTime;
            this.createdAt = createdAt;
        }

        // Getters
        public int getId() { return id; }
        public int getGameId() { return gameId; }
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getAlgorithm() { return algorithm; }
        public String getStartPosition() { return startPosition; }
        public String getSolutionPath() { return solutionPath; }
        public long getExecutionTime() { return executionTime; }
        public Timestamp getCreatedAt() { return createdAt; }

        @Override
        public String toString() {
            return "Solution by " + username + " using " + algorithm +
                    " (Start: " + startPosition + ", Time: " + executionTime + "ms)";
        }
    }
}