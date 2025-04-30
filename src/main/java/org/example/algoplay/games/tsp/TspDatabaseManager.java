package org.example.algoplay.games.tsp;

import org.example.algoplay.models.User;
import org.example.algoplay.services.UserSessionService;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

/**
 * Structured TSP Database Manager: Stores sessions, solver runs, and paths,
 * while pulling user identity from an external database via UserSessionService.
 */
public class TspDatabaseManager {

    private final String jdbcUrl = "jdbc:postgresql://localhost:5432/AlgoPlay";
    private final String username = "postgres";
    private final String password = "G0tb1tf3v3rh1t"; // Replace securely

    public boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            return conn != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveStructuredData(Map<String, TspPerformanceTracker.PerformanceData> results) {
        if (results == null || results.isEmpty()) return false;

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            conn.setAutoCommit(false);

            // Get current user from external login session
            User user = UserSessionService.getInstance().getCurrentUser();
            Integer userId = (user != null) ? user.getUserId() : null;
            int cityCount = results.values().iterator().next().getCityCount();
            UUID sessionUUID = UUID.randomUUID();

            // Insert new session
            String insertSession = "INSERT INTO tsp_sessions (user_id, session_uuid, city_count) VALUES (?, ?, ?) RETURNING id";
            int sessionId;

            try (PreparedStatement stmt = conn.prepareStatement(insertSession)) {
                if (userId != null) stmt.setInt(1, userId);
                else stmt.setNull(1, Types.INTEGER);

                stmt.setObject(2, sessionUUID);
                stmt.setInt(3, cityCount);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    sessionId = rs.getInt("id");
                } else {
                    throw new SQLException("Failed to insert session.");
                }
            }

            // Replace old insertRun and insertPath SQL with two sets
            String insertAlgoRun = "INSERT INTO algorithm_executions (session_id, algorithm_name, path_length, execution_time_ms) VALUES (?, ?, ?, ?) RETURNING id";
            String insertUserRun = "INSERT INTO user_attempts (session_id, user_id, path_length, execution_time_ms) VALUES (?, ?, ?, ?) RETURNING id";

            String insertAlgoPath = "INSERT INTO algorithm_paths (execution_id, city_order) VALUES (?, ?)";
            String insertUserPath = "INSERT INTO user_paths (attempt_id, city_order) VALUES (?, ?)";

            for (TspPerformanceTracker.PerformanceData data : results.values()) {
                String solverName = data.getSolutionName();

                boolean isUser = "A_Real_Person".equalsIgnoreCase(solverName);

                int runId;

                if (isUser) {
                    // Insert into user_attempts
                    try (PreparedStatement runStmt = conn.prepareStatement(insertUserRun)) {
                        runStmt.setInt(1, sessionId);
                        if (userId != null) runStmt.setInt(2, userId);
                        else runStmt.setNull(2, Types.INTEGER);
                        runStmt.setInt(3, data.getPathLength());
                        runStmt.setLong(4, data.getExecutionTimeMs());

                        ResultSet rs = runStmt.executeQuery();
                        if (rs.next()) {
                            runId = rs.getInt("id");
                        } else {
                            throw new SQLException("Failed to insert user attempt.");
                        }
                    }

                    // Insert into user_paths
                    try (PreparedStatement pathStmt = conn.prepareStatement(insertUserPath)) {
                        pathStmt.setInt(1, runId);
                        pathStmt.setString(2, data.getPath().toString());
                        pathStmt.executeUpdate();
                    }

                } else {
                    // Insert into algorithm_executions
                    try (PreparedStatement runStmt = conn.prepareStatement(insertAlgoRun)) {
                        runStmt.setInt(1, sessionId);
                        runStmt.setString(2, solverName);
                        runStmt.setInt(3, data.getPathLength());
                        runStmt.setLong(4, data.getExecutionTimeMs());

                        ResultSet rs = runStmt.executeQuery();
                        if (rs.next()) {
                            runId = rs.getInt("id");
                        } else {
                            throw new SQLException("Failed to insert algorithm execution.");
                        }
                    }

                    // Insert into algorithm_paths
                    try (PreparedStatement pathStmt = conn.prepareStatement(insertAlgoPath)) {
                        pathStmt.setInt(1, runId);
                        pathStmt.setString(2, data.getPath().toString());
                        pathStmt.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
