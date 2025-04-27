package org.example.algoplay.games.tsp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Handles database operations for TSP solutions
 * Uses default connection settings (no UI configuration)
 */
public class TspDatabaseManager {

    // PostgreSQL connection properties (hardcoded defaults)
    private String jdbcUrl = "jdbc:postgresql://localhost:5432/tsp_database";
    private String username = "postgres";
    private String password = "G0tb1tf3v3rh1t";

    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to get a database connection
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    /**
     * Initialize database - creates tables if they don't exist
     * @return true if successful, false otherwise
     */
    public boolean initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Create the solutions table
            String sql = "CREATE TABLE IF NOT EXISTS tsp_solutions (" +
                    "id SERIAL PRIMARY KEY," +
                    "session_id VARCHAR(50) NOT NULL," +
                    "solution_name VARCHAR(50) NOT NULL," +
                    "path_length INTEGER NOT NULL," +
                    "execution_time_ms BIGINT NOT NULL," +
                    "city_count INTEGER NOT NULL," +
                    "timestamp TIMESTAMP NOT NULL)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.executeUpdate();
            }

            // Create the paths table
            sql = "CREATE TABLE IF NOT EXISTS tsp_paths (" +
                    "id SERIAL PRIMARY KEY," +
                    "session_id VARCHAR(50) NOT NULL," +
                    "solution_name VARCHAR(50) NOT NULL," +
                    "path_json TEXT NOT NULL)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if database tables exist
     */
    private boolean tablesExist(Connection conn) throws SQLException {
        boolean solutionsTableExists = false;
        boolean pathsTableExists = false;

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'tsp_solutions')")) {
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    solutionsTableExists = rs.getBoolean(1);
                }
            }
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'tsp_paths')")) {
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pathsTableExists = rs.getBoolean(1);
                }
            }
        }

        return solutionsTableExists && pathsTableExists;
    }

    /**
     * Save performance data to database
     * @param performanceData Map of solution names to performance data
     * @return true if successful, false otherwise
     */
    public boolean savePerformanceData(Map<String, TspPerformanceTracker.PerformanceData> performanceData) {
        try (Connection conn = getConnection()) {
            // Test if tables exist, if not create them
            if (!tablesExist(conn)) {
                initializeDatabase();
            }

            // Start a transaction
            conn.setAutoCommit(false);

            // Generate a session ID for this set of results
            String sessionId = UUID.randomUUID().toString();

            // Prepare the SQL statement for solutions
            String sql = "INSERT INTO tsp_solutions (session_id, solution_name, path_length, " +
                    "execution_time_ms, city_count, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Insert each solution
                for (TspPerformanceTracker.PerformanceData data : performanceData.values()) {
                    pstmt.setString(1, sessionId);
                    pstmt.setString(2, data.getSolutionName());
                    pstmt.setInt(3, data.getPathLength());
                    pstmt.setLong(4, data.getExecutionTimeMs());
                    pstmt.setInt(5, data.getCityCount());
                    pstmt.setTimestamp(6, java.sql.Timestamp.valueOf(data.getTimestamp()));
                    pstmt.executeUpdate();
                }

                // Prepare the SQL statement for paths
                sql = "INSERT INTO tsp_paths (session_id, solution_name, path_json) VALUES (?, ?, ?)";

                try (PreparedStatement pathStmt = conn.prepareStatement(sql)) {
                    // Insert each path
                    for (TspPerformanceTracker.PerformanceData data : performanceData.values()) {
                        pathStmt.setString(1, sessionId);
                        pathStmt.setString(2, data.getSolutionName());
                        pathStmt.setString(3, data.getPath().toString());
                        pathStmt.executeUpdate();
                    }

                    // Commit the transaction
                    conn.commit();
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}