package org.example.algoplay.controllers.games;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.algoplay.games.toh.TowerOfHanoiGame;
import org.example.algoplay.models.TowerOfHanoiRound;
import org.example.algoplay.models.User;
import org.example.algoplay.services.GameStatisticsService;

import java.util.ArrayList;
import java.util.List;

public class TohController {

    @FXML
    private Spinner<Integer> diskCountSpinner;

    @FXML
    private CheckBox fourPegModeCheckbox;

    @FXML
    private Button solveRecursiveBtn;

    @FXML
    private Button solveIterativeBtn;

    @FXML
    private Button solveFourPegBtn;

    @FXML
    private Button backButton;

    @FXML
    private TextArea movesTextArea;

    @FXML
    private Label movesCountLabel;

    @FXML
    private Label optimalMovesLabel;

    @FXML
    private Label recursiveTimeLabel;

    @FXML
    private Label iterativeTimeLabel;

    @FXML
    private Label fourPegTimeLabel;

    @FXML
    private BarChart<String, Number> algorithmComparisonChart;

    @FXML
    private TableView<TowerOfHanoiRound> highScoresTable;

    @FXML
    private HBox visualizationContainer;

    private TowerOfHanoiGame game;
    private User currentUser;
    private GameStatisticsService statsService;

    private ObservableList<StackPane> pegs = FXCollections.observableArrayList();
    private List<String> userMoves = new ArrayList<>();

    public void initialize() {
        // Initialize the spinner for disk count (3-10)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 10, 3);
        diskCountSpinner.setValueFactory(valueFactory);

        // Initialize the game with default values
        resetGame();

        // Get statistics service
        statsService = GameStatisticsService.getInstance();

        // Setup event handlers
        setupEventHandlers();

        // Load high scores
        loadHighScores();

        // Configure the chart
        setupAlgorithmComparisonChart();

        // Setup visualization container
        setupVisualization();

        // Disable four peg button initially
        solveFourPegBtn.setDisable(true);

        // Enable/disable four peg button based on checkbox
        fourPegModeCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            solveFourPegBtn.setDisable(!newVal);
            resetGame();
        });

        // Return to main game menu
        backButton.setOnAction(event -> returnToMainMenu());
    }

    private void resetGame() {
        int diskCount = diskCountSpinner.getValue();
        boolean isFourPegMode = fourPegModeCheckbox.isSelected();

        game = new TowerOfHanoiGame(diskCount, isFourPegMode);

        // Reset UI elements
        movesTextArea.clear();
        movesCountLabel.setText("0");
        optimalMovesLabel.setText(String.valueOf(game.getOptimalMovesCount()));
        recursiveTimeLabel.setText("--");
        iterativeTimeLabel.setText("--");
        fourPegTimeLabel.setText("--");

        // Reset user moves
        userMoves.clear();

        // Reset visualization
        updateVisualization();
    }

    private void setupEventHandlers() {
        // Reset game when disk count changes
        diskCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> resetGame());

        // Solve buttons
        solveRecursiveBtn.setOnAction(event -> solveWithRecursive());
        solveIterativeBtn.setOnAction(event -> solveWithIterative());
        solveFourPegBtn.setOnAction(event -> solveWithFourPeg());
    }

    private void solveWithRecursive() {
        String moves = game.solveWithRecursiveAlgorithm();
        movesTextArea.setText(moves);
        updateStatistics();
        updateVisualization();
    }

    private void solveWithIterative() {
        String moves = game.solveWithIterativeAlgorithm();
        movesTextArea.setText(moves);
        updateStatistics();
        updateVisualization();
    }

    private void solveWithFourPeg() {
        if (!fourPegModeCheckbox.isSelected()) {
            return;
        }

        String moves = game.solveWithFourPegAlgorithm();
        movesTextArea.setText(moves);
        updateStatistics();
        updateVisualization();
    }

    private void updateStatistics() {
        // Update moves count
        int movesCount = game.getMoveHistory().size();
        movesCountLabel.setText(String.valueOf(movesCount));

        // Update algorithm times
        recursiveTimeLabel.setText(game.getRecursiveTime() + " ms");
        iterativeTimeLabel.setText(game.getIterativeTime() + " ms");

        if (fourPegModeCheckbox.isSelected()) {
            fourPegTimeLabel.setText(game.getFourPegTime() + " ms");
        }

        // Save statistics if user is logged in
        if (currentUser != null) {
            saveGameRound();
        }

        // Update algorithm comparison chart
        updateAlgorithmComparisonChart();
    }

    private void saveGameRound() {
        int numDisks = game.getNumDisks();
        int movesCount = game.getMoveHistory().size();
        String movesSequence = String.join("\n", game.getMoveHistory());
        int optimalMoves = game.getOptimalMovesCount();
        boolean isCorrect = movesCount == optimalMoves;

        TowerOfHanoiRound round = new TowerOfHanoiRound(
                currentUser.getUserId(),
                numDisks,
                movesCount,
                movesSequence,
                optimalMoves,
                isCorrect
        );

        round.setAlgorithmTimes(
                game.getRecursiveTime(),
                game.getIterativeTime(),
                game.getFourPegTime()
        );

        round.save();

        // Refresh high scores
        loadHighScores();
    }

    private void loadHighScores() {
        List<TowerOfHanoiRound> highScores = TowerOfHanoiRound.getHighScores(5);
        // Update high scores table
        // (This would require setting up table columns and items)
    }

    private void setupAlgorithmComparisonChart() {
        // Initialize chart series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Algorithm Performance (Avg. Time in ms)");

        algorithmComparisonChart.getData().add(series);
    }

    private void updateAlgorithmComparisonChart() {
        // Get algorithm comparison data
        List<TowerOfHanoiRound.AlgorithmPerformance> performances =
                TowerOfHanoiRound.getAlgorithmComparison();

        // Clear existing data
        algorithmComparisonChart.getData().clear();

        // Create new series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Algorithm Performance (Avg. Time in ms)");

        for (TowerOfHanoiRound.AlgorithmPerformance perf : performances) {
            String type = perf.getAlgorithmType();
            double time = perf.getAvgExecutionTime();

            // Format algorithm type name for display
            String displayName = type.substring(0, 1).toUpperCase() + type.substring(1);
            if (type.equals("four_peg")) {
                displayName = "Four Peg";
            }

            series.getData().add(new XYChart.Data<>(displayName, time));
        }

        algorithmComparisonChart.getData().add(series);
    }

    private void setupVisualization() {
        // Clear existing pegs
        visualizationContainer.getChildren().clear();
        pegs.clear();

        // Create pegs (3 or 4)
        int pegCount = fourPegModeCheckbox.isSelected() ? 4 : 3;
        for (int i = 0; i < pegCount; i++) {
            StackPane peg = createPeg(i);
            visualizationContainer.getChildren().add(peg);
            pegs.add(peg);
        }

        updateVisualization();
    }

    private StackPane createPeg(int pegIndex) {
        StackPane pegContainer = new StackPane();
        pegContainer.getStyleClass().add("peg-container");

        // Add peg base
        VBox pegBase = new VBox();
        pegBase.getStyleClass().add("peg-base");
        pegContainer.getChildren().add(pegBase);

        // Add label
        Label pegLabel = new Label(String.valueOf((char)('A' + pegIndex)));
        pegLabel.getStyleClass().add("peg-label");
        pegContainer.getChildren().add(pegLabel);

        return pegContainer;
    }

    private void updateVisualization() {
        // This would update the visualization based on the current game state
        // The actual implementation would depend on how the game tracks its state
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void returnToMainMenu() {
        try {
            // Load the main menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            Parent mainMenuRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();

            // Create new scene with main menu content
            Scene mainMenuScene = new Scene(mainMenuRoot);
            mainMenuScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            // Set the scene on the stage
            stage.setScene(mainMenuScene);
            stage.setTitle("AlgoPlay");

        } catch (Exception e) {
            System.err.println("Error returning to main menu");
            e.printStackTrace();
        }
    }
}