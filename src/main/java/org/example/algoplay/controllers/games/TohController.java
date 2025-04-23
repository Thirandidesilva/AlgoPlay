package org.example.algoplay.controllers.games;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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
import org.example.algoplay.services.UserSessionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

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
        // Initialize the spinner for disk count (5-10)
        int randomDiskCount = (int)(Math.random() * 6) + 5; // 5 to 10

        // Initialize the spinner for disk count (5-10)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 10, randomDiskCount);
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
            setupVisualization();
        });

        diskCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            resetGame();
        });

        // Return to main game menu
        backButton.setOnAction(event -> returnToMainMenu());

        // Get the current user from the session service
        currentUser = UserSessionService.getInstance().getCurrentUser();
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

        // Update the visualization container
        setupVisualization();
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
        recursiveTimeLabel.setText(game.getRecursiveTime() + " ms");
        updateStatistics();
        updateVisualization();
    }

    private void solveWithIterative() {
        String moves = game.solveWithIterativeAlgorithm();
        movesTextArea.setText(moves);
        iterativeTimeLabel.setText(game.getRecursiveTime() + " ms");
        updateStatistics();
        updateVisualization();
    }

    private void solveWithFourPeg() {
        if (!fourPegModeCheckbox.isSelected()) {
            return;
        }

        String moves = game.solveWithFourPegAlgorithm();
        movesTextArea.setText(moves);
        fourPegTimeLabel.setText(game.getRecursiveTime() + " ms");
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
        UserSessionService userSession = UserSessionService.getInstance();
        if (userSession.isLoggedIn()) {
            System.out.println("User is logged in. Attempting to save game round...");
            saveGameRound();
        } else {
            System.out.println("No user logged in. Game round will not be saved.");
        }

        // Update algorithm comparison chart
        updateAlgorithmComparisonChart();
    }

    private void saveGameRound() {
        // Make sure we have a current user
        User user = UserSessionService.getInstance().getCurrentUser();
        if (user == null) {
            System.err.println("Cannot save game round: No user logged in");
            return;
        }

        int userId = user.getUserId();
        // Fixed game ID as requested
        int gameId = 2;

        int numDisks = game.getNumDisks();
        int movesCount = game.getMoveHistory().size();
        String movesSequence = game.getMoveHistory().stream()
                .collect(Collectors.joining("\\n"));
        int optimalMoves = game.getOptimalMovesCount();
        boolean isCorrect = movesCount == optimalMoves;

        TowerOfHanoiRound round = new TowerOfHanoiRound(
                userId,  // Use the user ID directly
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

        boolean savedSuccessfully = round.save();
        if (savedSuccessfully) {
            System.out.println("Game round saved successfully for user ID: " + userId);
        } else {
            System.err.println("Failed to save game round for user ID: " + userId);
        }

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

        // Make sure alignment is set to bottom-center for proper stacking
        pegContainer.setAlignment(Pos.BOTTOM_CENTER);

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
        // Clear all pegs of disks first
        for (StackPane pegContainer : pegs) {
            // Keep only the peg base and label
            if (pegContainer.getChildren().size() > 2) {
                pegContainer.getChildren().subList(2, pegContainer.getChildren().size()).clear();
            }
        }

        // Add disks based on the current game state
        for (int pegIndex = 0; pegIndex < pegs.size(); pegIndex++) {
            StackPane pegContainer = pegs.get(pegIndex);
            Stack<Integer> pegState = game.getPegs().get(pegIndex);

            // Clone the stack to avoid modifying the game state
            @SuppressWarnings("unchecked")
            Stack<Integer> tempStack = (Stack<Integer>) pegState.clone();
            List<Integer> diskSizes = new ArrayList<>();

            // Pop disks from temporary stack to get them in order
            while (!tempStack.isEmpty()) {
                diskSizes.add(tempStack.pop());
            }

            // Add disks from bottom to top
            for (int i = diskSizes.size() - 1; i >= 0; i--) {
                int diskSize = diskSizes.get(i);
                StackPane disk = createDisk(diskSize);

                // Position disk vertically based on its position in the stack
                int stackPosition = diskSizes.size() - 1 - i;

                // Important change: Set a fixed vertical offset for each disk in the stack
                // The multiplier (25) determines the vertical spacing between disks
                disk.setTranslateY(-10 * stackPosition - 10); // Increased offset for better visibility

                // Add disk to peg
                pegContainer.getChildren().add(disk);
            }
        }
    }

    private StackPane createDisk(int size) {
        StackPane disk = new StackPane();
        disk.getStyleClass().addAll("disk", "disk-" + size);

        // Make disk draggable
        setupDragAndDrop(disk, size);

        return disk;
    }

        private void setupDragAndDrop(StackPane disk, int diskSize) {
            disk.setOnMousePressed(event -> {
                // Check if this is a valid disk to move (must be top disk)
                boolean isValidMove = false;

                // Find which peg contains this disk
                for (int pegIndex = 0; pegIndex < pegs.size(); pegIndex++) {
                    StackPane pegContainer = pegs.get(pegIndex);
                    if (pegContainer.getChildren().contains(disk)) {
                        // Check if this is the top disk
                        Stack<Integer> pegState = game.getPegs().get(pegIndex);
                        if (!pegState.isEmpty() && pegState.peek() == diskSize) {
                            isValidMove = true;

                            // Store source peg for later
                            disk.getProperties().put("sourcePeg", pegIndex);

                            // Add visual feedback
                            disk.getStyleClass().add("disk-moving");
                        }
                        break;
                    }
                }

                if (isValidMove) {
                    // Store initial position for dragging
                    disk.getProperties().put("dragStartX", event.getSceneX());
                    disk.getProperties().put("dragStartY", event.getSceneY());
                    disk.getProperties().put("initTranslateX", disk.getTranslateX());
                    disk.getProperties().put("initTranslateY", disk.getTranslateY());

                    // Bring to front
                    disk.toFront();

                    event.consume();
                }
            });

            disk.setOnMouseDragged(event -> {
                if (disk.getProperties().containsKey("dragStartX")) {
                    double dragStartX = (double) disk.getProperties().get("dragStartX");
                    double dragStartY = (double) disk.getProperties().get("dragStartY");
                    double initTranslateX = (double) disk.getProperties().get("initTranslateX");
                    double initTranslateY = (double) disk.getProperties().get("initTranslateY");

                    // Calculate new position
                    double newTranslateX = initTranslateX + event.getSceneX() - dragStartX;
                    double newTranslateY = initTranslateY + event.getSceneY() - dragStartY;

                    // Update position
                    disk.setTranslateX(newTranslateX);
                    disk.setTranslateY(newTranslateY);

                    event.consume();
                }
            });

            disk.setOnMouseReleased(event -> {
                if (disk.getProperties().containsKey("dragStartX")) {
                    // Find which peg is closest to the release point
                    StackPane targetPeg = findClosestPeg(event.getSceneX());
                    int sourcePegIndex = (int) disk.getProperties().get("sourcePeg");
                    int targetPegIndex = pegs.indexOf(targetPeg);

                    // Try to make the move
                    boolean moveMade = makeUserMove(sourcePegIndex, targetPegIndex);

                    // Remove visual feedback
                    disk.getStyleClass().remove("disk-moving");

                    // Clean up properties
                    disk.getProperties().remove("dragStartX");
                    disk.getProperties().remove("dragStartY");
                    disk.getProperties().remove("initTranslateX");
                    disk.getProperties().remove("initTranslateY");
                    disk.getProperties().remove("sourcePeg");

                    // Update visualization regardless of move success
                    updateVisualization();

                    event.consume();
                }
            });
        }

        private StackPane findClosestPeg(double sceneX) {
            StackPane closest = pegs.get(0);
            double minDistance = Double.MAX_VALUE;

            for (StackPane peg : pegs) {
                // Get peg center x coordinate in scene coordinates
                double pegCenterX = peg.localToScene(peg.getBoundsInLocal()).getCenterX();
                double distance = Math.abs(pegCenterX - sceneX);

                if (distance < minDistance) {
                    minDistance = distance;
                    closest = peg;
                }
            }

            return closest;
        }

        private boolean makeUserMove(int sourcePegIndex, int targetPegIndex) {
            if (sourcePegIndex == targetPegIndex) {
                return false; // No move if same peg
            }

            Stack<Integer> sourcePeg = game.getPegs().get(sourcePegIndex);
            Stack<Integer> targetPeg = game.getPegs().get(targetPegIndex);

            // Check if move is valid
            if (sourcePeg.isEmpty()) {
                return false; // Source peg is empty
            }

            int diskSize = sourcePeg.peek();
            if (!targetPeg.isEmpty() && targetPeg.peek() < diskSize) {
                return false; // Can't place larger disk on smaller disk
            }

            // Make the move
            int disk = sourcePeg.pop();
            targetPeg.push(disk);

            // Record the move
            String move = String.format("Move Disk %d from %c to %c",
                    disk, (char)('A' + sourcePegIndex), (char)('A' + targetPegIndex));
            game.getMoveHistory().add(move);
            movesTextArea.appendText(move + "\n");
            movesCountLabel.setText(String.valueOf(game.getMoveHistory().size()));

            // Check if game is solved
            checkForWin();

            return true;
        }

    private void checkForWin() {
        // Game is won when all disks are on the last peg
        boolean isFourPegMode = fourPegModeCheckbox.isSelected();
        int targetPegIndex = isFourPegMode ? 3 : 2;
        Stack<Integer> targetPeg = game.getPegs().get(targetPegIndex);

        if (targetPeg.size() == game.getNumDisks()) {
            // Game won!
            showWinDialog();

            // Save score if user is logged in
            if (currentUser != null) {
                saveGameRound();
            }
        }
    }

        private void showWinDialog() {
            int moves = game.getMoveHistory().size();
            int optimal = game.getOptimalMovesCount();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Congratulations!");
            alert.setHeaderText("Tower of Hanoi Solved!");
            alert.setContentText(String.format("You solved the puzzle in %d moves.\nOptimal solution: %d moves.",
                    moves, optimal));
            alert.showAndWait();
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
