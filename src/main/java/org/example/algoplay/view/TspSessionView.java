package org.example.algoplay.view;

import org.example.algoplay.controllers.games.tsp.TspSessionController;
import org.example.algoplay.games.tsp.TspSessionResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaFX view for displaying TSP session results
 */
public class TspSessionView extends BorderPane {

    private final TspSessionController controller;

    @FXML
    private ComboBox<String> sessionComboBox;

    @FXML
    private Button refreshButton;

    @FXML
    private Button exportCsvButton;

    @FXML
    private TableView<TspSessionResult> tableView;

    @FXML
    private TableColumn<TspSessionResult, String> uuidCol;

    @FXML
    private TableColumn<TspSessionResult, String> createdCol;

    @FXML
    private TableColumn<TspSessionResult, String> cityCountCol;

    @FXML
    private TableColumn<TspSessionResult, String> sourceCol;

    @FXML
    private TableColumn<TspSessionResult, String> pathLengthCol;

    @FXML
    private TableColumn<TspSessionResult, String> execTimeCol;

    @FXML
    private TableColumn<TspSessionResult, String> cityOrderCol;

    @FXML
    private BarChart<String, Number> executionTimeChart;

    @FXML
    private BarChart<String, Number> pathLengthChart;

    public TspSessionView(TspSessionController controller) {
        this.controller = controller;
        loadFxml();
        setupTableColumns();
        setupEventHandlers();
        loadData();
    }

    private void loadFxml() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tsp/TspSessionView.fxml"));
        loader.setController(this);

        try {
            BorderPane root = loader.load();
            // Copy the children and properties from the loaded root to this
            this.setTop(root.getTop());
            this.setCenter(root.getCenter());
            this.setBottom(root.getBottom());
            this.setLeft(root.getLeft());
            this.setRight(root.getRight());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for TspSessionView", e);
        }
    }

    private void setupTableColumns() {
        // Configure cell value factories for table columns
        uuidCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSessionUuid()));

        createdCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() == null) {
                return new SimpleStringProperty("");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
        });

        cityCountCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getCityCount())));

        sourceCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSource()));

        pathLengthCol.setCellValueFactory(cellData ->
                cellData.getValue().getPathLength() != null ?
                        new SimpleStringProperty(String.format("%.2f", cellData.getValue().getPathLength())) :
                        new SimpleStringProperty(""));

        execTimeCol.setCellValueFactory(cellData ->
                cellData.getValue().getExecutionTimeMs() != null ?
                        new SimpleStringProperty(String.valueOf(cellData.getValue().getExecutionTimeMs())) :
                        new SimpleStringProperty(""));

        cityOrderCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCityOrderAsString()));
    }

    @FXML
    private void initialize() {
        // Set ComboBox items
        sessionComboBox.setItems(controller.getSessionUuids());

        // Set TableView items
        tableView.setItems(controller.getSessionResults());
    }

    private void setupEventHandlers() {
        // Handle session selection
        sessionComboBox.setOnAction(e -> {
            String selectedUuid = sessionComboBox.getSelectionModel().getSelectedItem();
            if (selectedUuid != null) {
                controller.loadSessionResultsByUuid(selectedUuid);
                updateCharts();
            }
        });

        // Handle refresh button
        refreshButton.setOnAction(e -> loadData());

        // Handle export CSV button
        exportCsvButton.setOnAction(e -> exportToCsv());
    }

    private void loadData() {
        controller.loadAllSessionResults();
        updateCharts();
    }

    private void updateCharts() {
        // Clear existing series
        executionTimeChart.getData().clear();
        pathLengthChart.getData().clear();

        // Prepare data for charts
        XYChart.Series<String, Number> executionTimeSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> pathLengthSeries = new XYChart.Series<>();

        controller.getSessionResults().forEach(result -> {
            String source = result.getSource();

            if (result.getExecutionTimeMs() != null) {
                executionTimeSeries.getData().add(
                        new XYChart.Data<>(source, result.getExecutionTimeMs())
                );
            }

            if (result.getPathLength() != null) {
                pathLengthSeries.getData().add(
                        new XYChart.Data<>(source, result.getPathLength())
                );
            }
        });

        executionTimeChart.getData().add(executionTimeSeries);
        pathLengthChart.getData().add(pathLengthSeries);
    }

    /**
     * Exports the current session results to a CSV file
     */
    private void exportToCsv() {
        List<TspSessionResult> results = controller.getSessionResults();

        if (results.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Export Failed",
                    "No data to export", "Please load session data before exporting.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save TSP Session Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        // Suggest filename based on session UUID if a single session is displayed
        if (results.size() > 0) {
            // Check if all results have the same session UUID
            List<String> uniqueUuids = results.stream()
                    .map(TspSessionResult::getSessionUuid)
                    .distinct()
                    .collect(Collectors.toList());

            if (uniqueUuids.size() == 1) {
                String uuid = uniqueUuids.get(0);
                fileChooser.setInitialFileName("tsp_session_" + uuid + ".csv");
            } else {
                fileChooser.setInitialFileName("tsp_sessions.csv");
            }
        } else {
            fileChooser.setInitialFileName("tsp_sessions.csv");
        }

        Window window = this.getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                writer.write("Session UUID,Created At,Cities,Source,Username,Path Length,Execution Time (ms),City Order\n");

                // Write data rows
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (TspSessionResult result : results) {
                    StringBuilder row = new StringBuilder();

                    // Session UUID
                    row.append(escapeCsvField(result.getSessionUuid())).append(",");

                    // Created At
                    if (result.getCreatedAt() != null) {
                        row.append(escapeCsvField(result.getCreatedAt().format(formatter)));
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
                    row.append(escapeCsvField(result.getCityOrderAsString()));

                    row.append("\n");
                    writer.write(row.toString());
                }

                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Data Exported",
                        "Session data has been exported to:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed",
                        "Could not write to file", e.getMessage());
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

    /**
     * Shows an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}