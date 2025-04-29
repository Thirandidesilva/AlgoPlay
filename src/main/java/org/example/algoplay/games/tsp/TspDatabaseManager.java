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

    private final String jdbcUrl = "jdbc:postgresql://localhost:5432/tsp_database";
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

            // Insert each solver run and corresponding path
            String insertRun = "INSERT INTO tsp_runs (session_id, solver_name, path_length, execution_time_ms) VALUES (?, ?, ?, ?) RETURNING id";
            String insertPath = "INSERT INTO tsp_paths (run_id, city_order) VALUES (?, ?)";

            for (TspPerformanceTracker.PerformanceData data : results.values()) {
                int runId;
                try (PreparedStatement runStmt = conn.prepareStatement(insertRun)) {
                    runStmt.setInt(1, sessionId);
                    runStmt.setString(2, data.getSolutionName());
                    runStmt.setInt(3, data.getPathLength());
                    runStmt.setLong(4, data.getExecutionTimeMs());

                    ResultSet rs = runStmt.executeQuery();
                    if (rs.next()) {
                        runId = rs.getInt("id");
                    } else {
                        throw new SQLException("Failed to insert run.");
                    }
                }

                try (PreparedStatement pathStmt = conn.prepareStatement(insertPath)) {
                    pathStmt.setInt(1, runId);
                    pathStmt.setString(2, data.getPath().toString());
                    pathStmt.executeUpdate();
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
