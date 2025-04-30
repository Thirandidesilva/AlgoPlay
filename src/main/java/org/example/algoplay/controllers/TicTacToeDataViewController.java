package org.example.algoplay.controllers;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.algoplay.services.DatabaseServiceHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class TicTacToeDataViewController extends Application implements Initializable {

    @FXML
    private TableView<TicTacToeGameData> dataTable;

    @FXML
    private TableColumn<TicTacToeGameData, String> usernameColumn;

    @FXML
    private TableColumn<TicTacToeGameData, String> playerNameColumn;

    @FXML
    private TableColumn<TicTacToeGameData, String> gameColumn;

    @FXML
    private TableColumn<TicTacToeGameData, String> difficultyColumn;

    @FXML
    private TableColumn<TicTacToeGameData, String> resultColumn;

    @FXML
    private TableColumn<TicTacToeGameData, Integer> playerMovesColumn;

    @FXML
    private TableColumn<TicTacToeGameData, Integer> aiMovesColumn;

    @FXML
    private TableColumn<TicTacToeGameData, String> algorithmColumn;

    @FXML
    private TableColumn<TicTacToeGameData, Long> avgExecTimeColumn;

    @FXML
    private TableColumn<TicTacToeGameData, Long> maxExecTimeColumn;

    @FXML
    private TableColumn<TicTacToeGameData, Timestamp> createdAtColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Button refreshButton;

    @FXML
    private Button exportButton;

    // Use DatabaseServiceHelper instead of direct DatabaseService
    private DatabaseServiceHelper dbServiceHelper;
    private ObservableList<TicTacToeGameData> gameDataList;

    /**
     * JavaFX Application start method - launches the UI
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/algoplay/views/tic_tac_toe_data_view.fxml"));
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root);

            // Configure the stage
            primaryStage.setTitle("Tic-Tac-Toe Data View");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the database service helper - using the singleton pattern
        dbServiceHelper = DatabaseServiceHelper.getInstance();

        // Initialize observable list
        gameDataList = FXCollections.observableArrayList();

        // Configure table columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        playerNameColumn.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        gameColumn.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        playerMovesColumn.setCellValueFactory(new PropertyValueFactory<>("playerMoves"));
        aiMovesColumn.setCellValueFactory(new PropertyValueFactory<>("aiMoves"));
        algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("algorithm"));
        avgExecTimeColumn.setCellValueFactory(new PropertyValueFactory<>("avgExecutionTime"));
        maxExecTimeColumn.setCellValueFactory(new PropertyValueFactory<>("maxExecutionTime"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Set the items to the table
        dataTable.setItems(gameDataList);

        // Load initial data
        loadData();
    }

    @FXML
    private void refreshData() {
        loadData();
    }

    @FXML
    private void exportToCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(dataTable.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.write("Username,Player Name,Game,Difficulty,Result,Player Moves,AI Moves," +
                        "Algorithm,Avg Execution Time (ms),Max Execution Time (ms),Created At\n");

                // Write data rows
                for (TicTacToeGameData data : gameDataList) {
                    writer.write(String.format(
                            "%s,%s,%s,%s,%s,%d,%d,%s,%d,%d,%s\n",
                            data.getUsername(),
                            data.getPlayerName(),
                            data.getGameName(),
                            data.getDifficulty(),
                            data.getResult(),
                            data.getPlayerMoves(),
                            data.getAiMoves(),
                            data.getAlgorithm(),
                            data.getAvgExecutionTime(),
                            data.getMaxExecutionTime(),
                            data.getCreatedAt()
                    ));
                }

                statusLabel.setText("Data exported successfully to " + file.getName());
            } catch (IOException e) {
                statusLabel.setText("Error exporting data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadData() {
        gameDataList.clear();
        statusLabel.setText("Loading data...");

        try {
            // First, get username mapping
            Map<Integer, String> usernameMap = new HashMap<>();
            // Use dbServiceHelper instead of dbService
            ResultSet userRs = dbServiceHelper.executeQuery("SELECT user_id, username FROM users");
            while (userRs != null && userRs.next()) {
                usernameMap.put(userRs.getInt("user_id"), userRs.getString("username"));
            }

            // Get game name for Tic-Tac-Toe
            String gameName = "Tic-Tac-Toe";
            // Use dbServiceHelper instead of dbService
            ResultSet gameRs = dbServiceHelper.executeQuery("SELECT game_name FROM games WHERE game_id = 1");
            if (gameRs != null && gameRs.next()) {
                gameName = gameRs.getString("game_name");
            }

            // Get Tic-Tac-Toe game results
            String sql = "SELECT r.result_id, r.user_id, r.player_name, r.difficulty, " +
                    "r.result, r.player_moves, r.ai_moves, r.created_at " +
                    "FROM ttt_game_results r " +
                    "ORDER BY r.created_at DESC";

            // Use dbServiceHelper instead of dbService
            ResultSet rs = dbServiceHelper.executeQuery(sql);

            if (rs == null) {
                statusLabel.setText("No data returned from database");
                return;
            }

            // Temporary map to store results before adding performance data
            Map<Integer, TicTacToeGameData> resultsMap = new HashMap<>();
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                int resultId = rs.getInt("result_id");
                int userId = rs.getInt("user_id");
                String username = usernameMap.getOrDefault(userId, "Unknown");
                String playerName = rs.getString("player_name");
                String difficulty = rs.getString("difficulty");
                String result = rs.getString("result");
                int playerMoves = rs.getInt("player_moves");
                int aiMoves = rs.getInt("ai_moves");
                Timestamp createdAt = rs.getTimestamp("created_at");

                TicTacToeGameData gameData = new TicTacToeGameData(
                        resultId, username, playerName, gameName, difficulty, result,
                        playerMoves, aiMoves, createdAt
                );

                resultsMap.put(resultId, gameData);
            }

            if (!hasData) {
                statusLabel.setText("No Tic-Tac-Toe games found");
                return;
            }

            // Get algorithm performance data for each game
            String perfSql = "SELECT p.result_id, p.algorithm_name, p.execution_time, p.move_number " +
                    "FROM ttt_algorithm_performance p " +
                    "ORDER BY p.result_id, p.move_number";

            // Use dbServiceHelper instead of dbService
            ResultSet perfRs = dbServiceHelper.executeQuery(perfSql);

            if (perfRs != null) {
                Map<Integer, List<AlgorithmPerformance>> performanceMap = new HashMap<>();

                while (perfRs.next()) {
                    int resultId = perfRs.getInt("result_id");
                    String algoName = perfRs.getString("algorithm_name");
                    long execTime = perfRs.getLong("execution_time");
                    int moveNumber = perfRs.getInt("move_number");

                    if (!performanceMap.containsKey(resultId)) {
                        performanceMap.put(resultId, new ArrayList<>());
                    }

                    performanceMap.get(resultId).add(new AlgorithmPerformance(
                            algoName, execTime, moveNumber
                    ));
                }

                // Calculate and set algorithm metrics for each game
                for (Map.Entry<Integer, List<AlgorithmPerformance>> entry : performanceMap.entrySet()) {
                    int resultId = entry.getKey();
                    List<AlgorithmPerformance> performances = entry.getValue();

                    if (resultsMap.containsKey(resultId)) {
                        TicTacToeGameData gameData = resultsMap.get(resultId);

                        // Group by algorithm name to handle multiple algorithms
                        Map<String, List<Long>> algoTimes = new HashMap<>();

                        for (AlgorithmPerformance perf : performances) {
                            String algoName = perf.getAlgorithmName();
                            if (!algoTimes.containsKey(algoName)) {
                                algoTimes.put(algoName, new ArrayList<>());
                            }
                            algoTimes.get(algoName).add(perf.getExecutionTime());
                        }

                        // Find most used algorithm
                        String mostUsedAlgo = "";
                        int maxCount = 0;

                        for (Map.Entry<String, List<Long>> algoEntry : algoTimes.entrySet()) {
                            if (algoEntry.getValue().size() > maxCount) {
                                maxCount = algoEntry.getValue().size();
                                mostUsedAlgo = algoEntry.getKey();
                            }
                        }

                        if (!mostUsedAlgo.isEmpty()) {
                            List<Long> times = algoTimes.get(mostUsedAlgo);
                            long sum = 0;
                            long max = 0;

                            for (Long time : times) {
                                sum += time;
                                if (time > max) {
                                    max = time;
                                }
                            }

                            long avg = times.isEmpty() ? 0 : sum / times.size();

                            gameData.setAlgorithm(mostUsedAlgo);
                            gameData.setAvgExecutionTime(avg);
                            gameData.setMaxExecutionTime(max);
                        }
                    }
                }
            }

            // Add processed game data to the observable list
            gameDataList.addAll(resultsMap.values());

            statusLabel.setText("Data refreshed successfully: " + gameDataList.size() + " games loaded");
        } catch (SQLException e) {
            statusLabel.setText("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Data model class for Tic-Tac-Toe game data
    public static class TicTacToeGameData {
        private final int resultId;
        private final String username;
        private final String playerName;
        private final String gameName;
        private final String difficulty;
        private final String result;
        private final int playerMoves;
        private final int aiMoves;
        private String algorithm;
        private long avgExecutionTime;
        private long maxExecutionTime;
        private final Timestamp createdAt;

        public TicTacToeGameData(
                int resultId,
                String username,
                String playerName,
                String gameName,
                String difficulty,
                String result,
                int playerMoves,
                int aiMoves,
                Timestamp createdAt
        ) {
            this.resultId = resultId;
            this.username = username;
            this.playerName = playerName;
            this.gameName = gameName;
            this.difficulty = difficulty;
            this.result = result;
            this.playerMoves = playerMoves;
            this.aiMoves = aiMoves;
            this.algorithm = "";
            this.avgExecutionTime = 0;
            this.maxExecutionTime = 0;
            this.createdAt = createdAt;
        }

        // Getters
        public int getResultId() { return resultId; }
        public String getUsername() { return username; }
        public String getPlayerName() { return playerName; }
        public String getGameName() { return gameName; }
        public String getDifficulty() { return difficulty; }
        public String getResult() { return result; }
        public int getPlayerMoves() { return playerMoves; }
        public int getAiMoves() { return aiMoves; }
        public String getAlgorithm() { return algorithm; }
        public long getAvgExecutionTime() { return avgExecutionTime; }
        public long getMaxExecutionTime() { return maxExecutionTime; }
        public Timestamp getCreatedAt() { return createdAt; }

        // Setters for calculated fields
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public void setAvgExecutionTime(long avgExecutionTime) { this.avgExecutionTime = avgExecutionTime; }
        public void setMaxExecutionTime(long maxExecutionTime) { this.maxExecutionTime = maxExecutionTime; }
    }

    // Helper class for storing algorithm performance data
    private static class AlgorithmPerformance {
        private final String algorithmName;
        private final long executionTime;
        private final int moveNumber;

        public AlgorithmPerformance(String algorithmName, long executionTime, int moveNumber) {
            this.algorithmName = algorithmName;
            this.executionTime = executionTime;
            this.moveNumber = moveNumber;
        }

        public String getAlgorithmName() { return algorithmName; }
        public long getExecutionTime() { return executionTime; }
        public int getMoveNumber() { return moveNumber; }
    }
}