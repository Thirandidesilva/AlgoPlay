package org.example.algoplay.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.algoplay.models.HanoiDataEntry;
import org.example.algoplay.services.DatabaseService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.stage.FileChooser;

public class HanoiDataViewController implements Initializable {

    @FXML private TableView<HanoiDataEntry> dataTable;
    @FXML private TableColumn<HanoiDataEntry, String> usernameColumn;
    @FXML private TableColumn<HanoiDataEntry, String> gameNameColumn;
    @FXML private TableColumn<HanoiDataEntry, Integer> diskCountColumn;
    @FXML private TableColumn<HanoiDataEntry, Integer> moveCountColumn;
    @FXML private TableColumn<HanoiDataEntry, Integer> optimalMovesColumn;
    @FXML private TableColumn<HanoiDataEntry, Boolean> isCorrectColumn;
    @FXML private TableColumn<HanoiDataEntry, Long> recursiveTimeColumn;
    @FXML private TableColumn<HanoiDataEntry, Long> iterativeTimeColumn;
    @FXML private TableColumn<HanoiDataEntry, Long> fourPegTimeColumn;
    @FXML private TableColumn<HanoiDataEntry, String> moveSequenceColumn;
    @FXML private Label statusLabel;
    @FXML private Button backToGameButton;

    private ObservableList<HanoiDataEntry> dataEntries = FXCollections.observableArrayList();
    private DatabaseService dbService = DatabaseService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        gameNameColumn.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        diskCountColumn.setCellValueFactory(new PropertyValueFactory<>("diskCount"));
        moveCountColumn.setCellValueFactory(new PropertyValueFactory<>("moveCount"));
        optimalMovesColumn.setCellValueFactory(new PropertyValueFactory<>("optimalMoves"));
        isCorrectColumn.setCellValueFactory(new PropertyValueFactory<>("isCorrect"));
        recursiveTimeColumn.setCellValueFactory(new PropertyValueFactory<>("recursiveTime"));
        iterativeTimeColumn.setCellValueFactory(new PropertyValueFactory<>("iterativeTime"));
        fourPegTimeColumn.setCellValueFactory(new PropertyValueFactory<>("fourPegTime"));
        moveSequenceColumn.setCellValueFactory(new PropertyValueFactory<>("moveSequence"));

        // Format boolean column to show Yes/No instead of true/false
        isCorrectColumn.setCellFactory(column -> new TableCell<HanoiDataEntry, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });

        // Make the table resizable
        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load data
        refreshData();
    }

    @FXML
    private void refreshData() {
        try {
            loadData();
            statusLabel.setText("Data refreshed successfully");
        } catch (Exception e) {
            statusLabel.setText("Error refreshing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File file = fileChooser.showSaveDialog(dataTable.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.write("Username,Game,Disk Count,Moves,Optimal Moves,Solved Correctly," +
                        "Recursive Time (ms),Iterative Time (ms),Four Peg Time (ms),Move Sequence\n");

                // Write data
                for (HanoiDataEntry entry : dataEntries) {
                    writer.write(String.format("%s,%s,%d,%d,%d,%s,%d,%d,%d,\"%s\"\n",
                            entry.getUsername(),
                            entry.getGameName(),
                            entry.getDiskCount(),
                            entry.getMoveCount(),
                            entry.getOptimalMoves(),
                            entry.getIsCorrect() ? "Yes" : "No",
                            entry.getRecursiveTime(),
                            entry.getIterativeTime(),
                            entry.getFourPegTime(),
                            entry.getMoveSequence().replace("\"", "\"\"") // Escape quotes for CSV
                    ));
                }
                statusLabel.setText("Data exported to " + file.getName());
            } catch (IOException e) {
                statusLabel.setText("Error exporting data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void backToGame() {
        try {
            // Load the Tower of Hanoi FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/toh.fxml"));
            Parent tohRoot = loader.load();

            // Create new scene
            Scene tohScene = new Scene(tohRoot);
            tohScene.getStylesheets().add(getClass().getResource("/css/toh.css").toExternalForm());

            // Get the current stage
            Stage stage = (Stage) dataTable.getScene().getWindow();

            // Set the scene on the stage
            stage.setScene(tohScene);
            stage.setTitle("Tower of Hanoi");

        } catch (Exception e) {
            System.err.println("Error returning to Tower of Hanoi game");
            e.printStackTrace();
        }
    }

    private void loadData() {
        dataEntries.clear();

        try {
            // First, get username mapping
            Map<Integer, String> usernameMap = new HashMap<>();
            ResultSet userRs = dbService.executeQuery("SELECT user_id, username FROM users");
            while (userRs != null && userRs.next()) {
                usernameMap.put(userRs.getInt("user_id"), userRs.getString("username"));
            }

            // Debugging output
            System.out.println("Found " + usernameMap.size() + " users");

            // Query tower_of_hanoi_rounds directly
            String sql = "SELECT t.hanoi_id, t.user_id, t.num_disks, t.moves_count, " +
                    "t.moves_sequence, t.optimal_moves, t.is_correct " +
                    "FROM tower_of_hanoi_rounds t";

            ResultSet rs = dbService.executeQuery(sql);

            // Check if ResultSet is empty
            if (rs == null) {
                statusLabel.setText("No data returned from database query");
                System.out.println("ResultSet is null");
                return;
            }

            // First, get all hanoi games
            Map<Integer, HanoiDataEntry> entriesMap = new HashMap<>();
            boolean hasData = false;

            // Since you mentioned the game_id is 2, let's get that game name
            String gameName = "Tower of Hanoi"; // Default name
            ResultSet gameRs = dbService.executeQuery("SELECT game_name FROM games WHERE game_id = 2");
            if (gameRs != null && gameRs.next()) {
                gameName = gameRs.getString("game_name");
            }
            System.out.println("Game name: " + gameName);

            while (rs.next()) {
                hasData = true;
                int hanoiId = rs.getInt("hanoi_id");
                int userId = rs.getInt("user_id");
                String username = usernameMap.getOrDefault(userId, "Unknown");
                int diskCount = rs.getInt("num_disks");
                int moveCount = rs.getInt("moves_count");
                int optimalMoves = rs.getInt("optimal_moves");
                boolean isCorrect = rs.getBoolean("is_correct");
                String moveSequence = rs.getString("moves_sequence");

                // Create entry with algorithm times set to 0 initially
                HanoiDataEntry entry = new HanoiDataEntry(
                        username, gameName, diskCount, moveCount, optimalMoves,
                        isCorrect, 0L, 0L, 0L, moveSequence
                );

                entriesMap.put(hanoiId, entry);
                System.out.println("Added entry for hanoi_id: " + hanoiId);
            }

            if (!hasData) {
                statusLabel.setText("No Tower of Hanoi rounds found in database");
                System.out.println("No data in ResultSet");
                return;
            }

            // Now get algorithm performance data
            String perfSql = "SELECT hanoi_id, algorithm_type, execution_time " +
                    "FROM hanoi_algorithm_performance";

            ResultSet perfRs = dbService.executeQuery(perfSql);

            if (perfRs != null) {
                while (perfRs.next()) {
                    int hanoiId = perfRs.getInt("hanoi_id");
                    String algoType = perfRs.getString("algorithm_type");
                    long execTime = perfRs.getLong("execution_time");

                    if (entriesMap.containsKey(hanoiId)) {
                        HanoiDataEntry entry = entriesMap.get(hanoiId);

                        // Update the appropriate time based on algorithm type
                        if (algoType.equalsIgnoreCase("recursive")) {
                            entry.recursiveTimeProperty().set(execTime);
                        } else if (algoType.equalsIgnoreCase("iterative")) {
                            entry.iterativeTimeProperty().set(execTime);
                        } else if (algoType.equalsIgnoreCase("four_peg")) {
                            entry.fourPegTimeProperty().set(execTime);
                        }
                        System.out.println("Updated entry for hanoi_id: " + hanoiId + " with " + algoType + " time: " + execTime);
                    }
                }
            }

            // Add all entries to the observable list
            dataEntries.addAll(entriesMap.values());
            dataTable.setItems(dataEntries);

            statusLabel.setText("Loaded " + dataEntries.size() + " data entries");
        } catch (SQLException e) {
            statusLabel.setText("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}