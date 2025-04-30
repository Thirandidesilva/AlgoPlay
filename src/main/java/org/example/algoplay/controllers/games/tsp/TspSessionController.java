package org.example.algoplay.controllers.games.tsp;

import org.example.algoplay.games.tsp.TspDatabaseManager;
import org.example.algoplay.games.tsp.TspSessionResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Controller for TSP session data, integrating with existing TspDatabaseManager
 */
public class TspSessionController {

    private final TspDatabaseManager dbManager;
    private final ObservableList<TspSessionResult> sessionResults;
    private final ObservableList<String> sessionUuids;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private Stage primaryStage;

    public TspSessionController() {
        this.dbManager = new TspDatabaseManager();
        this.sessionResults = FXCollections.observableArrayList();
        this.sessionUuids = FXCollections.observableArrayList();

        this.jdbcUrl = "jdbc:postgresql://localhost:5432/AlgoPlay";
        this.username = "postgres";
        this.password = "G0tb1tf3v3rh1t"; // Use secure credential management in real applications
    }

    /**
     * Sets the primary stage for use with dialogs
     * @param stage The primary application stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void loadAllSessionResults() {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            String query = """
                SELECT 
                    s.id AS session_id,
                    s.session_uuid,
                    s.city_count,
                    s.started_at,
                    'User' AS source,
                    u.user_id,
                    u.username,
                    ua.id AS run_id,
                    up.city_order,
                    ua.path_length,
                    ua.execution_time_ms
                FROM tsp_sessions s
                LEFT JOIN user_attempts ua ON ua.session_id = s.id
                LEFT JOIN user_paths up ON up.attempt_id = ua.id
                LEFT JOIN users u ON s.user_id = u.user_id

                UNION

                SELECT 
                    s.id AS session_id,
                    s.session_uuid,
                    s.city_count,
                    s.started_at,
                    ae.algorithm_name AS source,
                    NULL AS user_id,
                    NULL AS username,
                    ae.id AS run_id,
                    ap.city_order,
                    ae.path_length,
                    ae.execution_time_ms
                FROM tsp_sessions s
                LEFT JOIN algorithm_executions ae ON ae.session_id = s.id
                LEFT JOIN algorithm_paths ap ON ap.execution_id = ae.id

                ORDER BY session_id, source, run_id
            """;

            List<TspSessionResult> results = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToSessionResult(rs));
                }
            }

            sessionResults.clear();
            sessionResults.addAll(results);
            updateSessionUuidsList();

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load session results", e.getMessage());
        }
    }

    public void loadSessionResultsByUuid(String sessionUuid) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            String query = """
                SELECT 
                    s.id AS session_id,
                    s.session_uuid,
                    s.city_count,
                    s.started_at,
                    'User' AS source,
                    u.user_id,
                    u.username,
                    ua.id AS run_id,
                    up.city_order,
                    ua.path_length,
                    ua.execution_time_ms
                FROM tsp_sessions s
                LEFT JOIN user_attempts ua ON ua.session_id = s.id
                LEFT JOIN user_paths up ON up.attempt_id = ua.id
                LEFT JOIN users u ON s.user_id = u.user_id
                WHERE s.session_uuid = ?
                
                UNION
                
                SELECT 
                    s.id AS session_id,
                    s.session_uuid,
                    s.city_count,
                    s.started_at,
                    ae.algorithm_name AS source,
                    NULL AS user_id,
                    NULL AS username,
                    ae.id AS run_id,
                    ap.city_order,
                    ae.path_length,
                    ae.execution_time_ms
                FROM tsp_sessions s
                LEFT JOIN algorithm_executions ae ON ae.session_id = s.id
                LEFT JOIN algorithm_paths ap ON ap.execution_id = ae.id
                WHERE s.session_uuid = ?

                ORDER BY source, run_id
            """;

            List<TspSessionResult> results = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setObject(1, java.util.UUID.fromString(sessionUuid));
                stmt.setObject(2, java.util.UUID.fromString(sessionUuid));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapResultSetToSessionResult(rs));
                    }
                }
            }

            sessionResults.clear();
            sessionResults.addAll(results);

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load session results", e.getMessage());
        }
    }

    /**
     * Exports the current session results to a CSV file
     */
    public void exportToCsv() {
        if (sessionResults.isEmpty()) {
            showInfoAlert("Export Failed", "No data to export", "Please load session data before exporting.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save TSP Session Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        // Suggest filename based on session UUID if a single session is displayed
        if (sessionResults.size() > 0 && sessionResults.stream().map(TspSessionResult::getSessionUuid).distinct().count() == 1) {
            String uuid = sessionResults.get(0).getSessionUuid();
            fileChooser.setInitialFileName("tsp_session_" + uuid + ".csv");
        } else {
            fileChooser.setInitialFileName("tsp_sessions.csv");
        }

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                writer.write("Session UUID,Created At,Cities,Source,Username,Path Length,Execution Time (ms),City Order\n");

                // Write data rows
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (TspSessionResult result : sessionResults) {
                    StringBuilder row = new StringBuilder();

                    // Session UUID
                    row.append(result.getSessionUuid()).append(",");

                    // Created At
                    if (result.getCreatedAt() != null) {
                        row.append(result.getCreatedAt().format(formatter));
                    }
                    row.append(",");

                    // Cities
                    row.append(result.getCityCount()).append(",");

                    // Source
                    row.append(escapeCsvField(result.getSource())).append(",");

                    // Username
                    row.append(escapeCsvField(result.getUsername())).append(",");

                    // Path Length
                    if (result.getPathLength() != null) {
                        row.append(result.getPathLength());
                    }
                    row.append(",");

                    // Execution Time
                    if (result.getExecutionTimeMs() != null) {
                        row.append(result.getExecutionTimeMs());
                    }
                    row.append(",");

                    // City Order
                    if (result.getCityOrder() != null) {
                        row.append(escapeCsvField(result.getCityOrder().toString()));
                    }

                    row.append("\n");
                    writer.write(row.toString());
                }

                showInfoAlert("Export Successful", "Data Exported",
                        "Session data has been exported to:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                showErrorAlert("Export Failed", "Could not write to file", e.getMessage());
            }
        }
    }

    /**
     * Escapes a field for CSV format by quoting fields with commas
     * and escaping quotes
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private TspSessionResult mapResultSetToSessionResult(ResultSet rs) throws SQLException {
        TspSessionResult result = new TspSessionResult();

        result.setSessionId(rs.getLong("session_id"));
        result.setSessionUuid(rs.getString("session_uuid"));
        result.setCityCount(rs.getInt("city_count"));

        Timestamp createdAt = rs.getTimestamp("started_at");
        if (createdAt != null) {
            result.setCreatedAt(createdAt.toLocalDateTime());
        }

        result.setSource(rs.getString("source"));

        Long userId = rs.getLong("user_id");
        if (!rs.wasNull()) {
            result.setUserId(userId);
        }

        String username = rs.getString("username");
        if (username != null) {
            result.setUsername(username);
        }

        Long runId = rs.getLong("run_id");
        if (!rs.wasNull()) {
            result.setRunId(runId);
        }

        String cityOrder = rs.getString("city_order");
        if (cityOrder != null) {
            result.setCityOrderFromString(cityOrder);
        }

        Double pathLength = rs.getDouble("path_length");
        if (!rs.wasNull()) {
            result.setPathLength(pathLength);
        }

        Long executionTime = rs.getLong("execution_time_ms");
        if (!rs.wasNull()) {
            result.setExecutionTimeMs(executionTime);
        }

        return result;
    }

    private void updateSessionUuidsList() {
        List<String> uniqueUuids = sessionResults.stream()
                .map(TspSessionResult::getSessionUuid)
                .distinct()
                .collect(Collectors.toList());

        sessionUuids.clear();
        sessionUuids.addAll(uniqueUuids);
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public String getFormattedDateTime(TspSessionResult result) {
        if (result.getCreatedAt() == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return result.getCreatedAt().format(formatter);
    }

    public boolean testDatabaseConnection() {
        return dbManager.testConnection();
    }

    public ObservableList<TspSessionResult> getSessionResults() {
        return sessionResults;
    }

    public ObservableList<String> getSessionUuids() {
        return sessionUuids;
    }
}