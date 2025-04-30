package org.example.algoplay.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.algoplay.controllers.games.KTController;
import org.example.algoplay.services.DatabaseManager;
import org.example.algoplay.services.UserSessionService;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KTDataViewController {
    @FXML private TableView<DatabaseManager.SolutionRecord> solutionsTable;
    @FXML private TableColumn<DatabaseManager.SolutionRecord, Integer> idColumn;
    @FXML private TableColumn<DatabaseManager.SolutionRecord, String> usernameColumn;
    @FXML private TableColumn<DatabaseManager.SolutionRecord, String> algorithmColumn;
    @FXML private TableColumn<DatabaseManager.SolutionRecord, String> startPositionColumn;
    @FXML private TableColumn<DatabaseManager.SolutionRecord, Long> executionTimeColumn;
    @FXML private TableColumn<DatabaseManager.SolutionRecord, String> dateColumn;

    @FXML private BarChart<String, Number> executionTimeChart;
    @FXML private PieChart algorithmDistributionChart;

    @FXML private ComboBox<String> filterComboBox;
    @FXML private GridPane solutionPreviewGrid;
    @FXML private Label solutionStatsLabel;
    @FXML private Button backButton;

    private final int BOARD_SIZE = 5;
    private DatabaseManager dbManager;
    private UserSessionService userSessionService;
    private ObservableList<DatabaseManager.SolutionRecord> allSolutions;
    private ObservableList<DatabaseManager.SolutionRecord> mySolutions;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        try {
            // Initialize services
            dbManager = new DatabaseManager();
            userSessionService = UserSessionService.getInstance();

            // Configure table columns
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("algorithm"));
            startPositionColumn.setCellValueFactory(new PropertyValueFactory<>("startPosition"));
            executionTimeColumn.setCellValueFactory(new PropertyValueFactory<>("executionTime"));
            dateColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(
                            () -> dateFormat.format(cellData.getValue().getCreatedAt()),
                            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCreatedAt())
                    )
            );

            // Set up filter options
            filterComboBox.getItems().addAll("All Solutions", "My Solutions");
            filterComboBox.setValue("All Solutions");
            filterComboBox.setOnAction(e -> updateSolutionsView());

            // Add selection listener to the table
            solutionsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            displaySolutionPreview(newSelection);
                        }
                    }
            );

            // Load solutions and update UI
            loadSolutionsData();
            updateSolutionsView();
            updateCharts();

        } catch (Exception e) {
            showAlert("Error", "Failed to initialize data view: " + e.getMessage());
            System.err.println("Data view initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSolutionsData() {
        try {
            // Load all solutions
            List<DatabaseManager.SolutionRecord> solutions = dbManager.getAllSolutions();
            allSolutions = FXCollections.observableArrayList(solutions);

            // Load current user's solutions
            if (userSessionService.isLoggedIn()) {
                List<DatabaseManager.SolutionRecord> userSolutions = dbManager.getSolutionsForCurrentUser();
                mySolutions = FXCollections.observableArrayList(userSolutions);
            } else {
                mySolutions = FXCollections.observableArrayList();
            }
        } catch (Exception e) { //Database Excepttion handling
            showAlert("Database Error", "Failed to load solutions: " + e.getMessage());
            System.err.println("Failed to load solutions: " + e.getMessage());
            e.printStackTrace();

            // Initialize empty lists in case of error
            allSolutions = FXCollections.observableArrayList();
            mySolutions = FXCollections.observableArrayList();
        }
    }

    private void updateSolutionsView() {
        String filter = filterComboBox.getValue();

        if ("My Solutions".equals(filter)) {
            if (!userSessionService.isLoggedIn()) {
                showAlert("Not Logged In", "Please log in to view your solutions.");
                filterComboBox.setValue("All Solutions");
                solutionsTable.setItems(allSolutions);
            } else {
                solutionsTable.setItems(mySolutions);
            }
        } else {
            solutionsTable.setItems(allSolutions);
        }

        // Clear solution preview
        clearSolutionPreview();
    }

    private void updateCharts() {
        try {
            // Update execution time chart
            executionTimeChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Average Execution Time (ms)");

            Map<String, Double> avgTimes = dbManager.getAverageExecutionTimeByAlgorithm();
            for (Map.Entry<String, Double> entry : avgTimes.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            executionTimeChart.getData().add(series);

            // Update algorithm distribution chart
            algorithmDistributionChart.getData().clear();
            Map<String, Integer> counts = dbManager.getSolutionCountByAlgorithm();
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                algorithmDistributionChart.getData().add(
                        new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue())
                );
            }

        } catch (Exception e) {
            System.err.println("Error updating charts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displaySolutionPreview(DatabaseManager.SolutionRecord solution) {
        // Clear previous content
        clearSolutionPreview();

        try {
            // Convert solution path to points
            List<KTController.Point> pathPoints = dbManager.convertPathStringToPoints(solution.getSolutionPath());

            // Create board grid
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    Rectangle square = new Rectangle(40, 40);
                    square.setFill((row + col) % 2 == 0 ? Color.SLATEGRAY : Color.GHOSTWHITE);

                    StackPane stackPane = new StackPane();
                    stackPane.getChildren().add(square);

                    solutionPreviewGrid.add(stackPane, col, row);
                }
            }

            // Draw path
            for (int i = 0; i < pathPoints.size(); i++) {
                KTController.Point p = pathPoints.get(i);

                Rectangle pathSquare = new Rectangle(40, 40);
                if (i == 0) {
                    // Start position is blue
                    pathSquare.setFill(Color.CORNFLOWERBLUE);
                } else if (i == pathPoints.size() - 1) {
                    // End position is red
                    pathSquare.setFill(Color.LIGHTCORAL);
                } else {
                    // Middle positions are salmon
                    pathSquare.setFill(Color.LIGHTSALMON);
                }

                Text moveNumber = new Text(Integer.toString(i));
                moveNumber.setStyle("-fx-font-size: 10;");

                StackPane stackPane = new StackPane();
                stackPane.getChildren().addAll(pathSquare, moveNumber);

                solutionPreviewGrid.add(stackPane, p.y, p.x);
            }

            // Update solution stats label
            String stats = String.format(
                    "Algorithm: %s | Start: %s | Execution time: %d ms | Path length: %d",
                    solution.getAlgorithm(),
                    solution.getStartPosition(),
                    solution.getExecutionTime(),
                    pathPoints.size()
            );
            solutionStatsLabel.setText(stats);

        } catch (Exception e) {
            System.err.println("Error displaying solution preview: " + e.getMessage());
            e.printStackTrace();
            solutionStatsLabel.setText("Error displaying solution");
        }
    }

    private void clearSolutionPreview() {
        solutionPreviewGrid.getChildren().clear();
        solutionStatsLabel.setText("");
    }

    @FXML
    private void refreshData() {
        loadSolutionsData();
        updateSolutionsView();
        updateCharts();
    }

    @FXML
    private void exportToCSV() {
        try {
            // Get current displayed solutions
            ObservableList<DatabaseManager.SolutionRecord> solutions = solutionsTable.getItems();

            if (solutions.isEmpty()) {
                showAlert("No Data", "There are no solutions to export.");
                return;
            }

            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save CSV File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("knights_tour_solutions_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");

            // Show save dialog
            File file = fileChooser.showSaveDialog(backButton.getScene().getWindow());

            if (file != null) {
                // Write to CSV file
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    // Write header
                    writer.println("ID,Username,Algorithm,Start Position,Execution Time (ms),Solution Path,Date");

                    // Write data rows
                    for (DatabaseManager.SolutionRecord solution : solutions) {
                        writer.println(
                                solution.getId() + "," +
                                        csvEscape(solution.getUsername()) + "," +
                                        csvEscape(solution.getAlgorithm()) + "," +
                                        csvEscape(solution.getStartPosition()) + "," +
                                        solution.getExecutionTime() + "," +
                                        csvEscape(solution.getSolutionPath()) + "," +
                                        csvEscape(dateFormat.format(solution.getCreatedAt()))
                        );
                    }

                    showAlert("Export Successful", "Solutions exported to " + file.getName());
                }
            }
        } catch (Exception e) {
            showAlert("Export Error", "Failed to export solutions: " + e.getMessage());
            System.err.println("Failed to export solutions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to escape CSV values
    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma or newline
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (needsQuoting) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @FXML
    private void backToGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/KTView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/kt.css").toExternalForm());

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Knight's Tour");

        } catch (IOException e) {
            System.err.println("Error returning to Knight's Tour: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}