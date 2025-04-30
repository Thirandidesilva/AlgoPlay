package org.example.algoplay.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.algoplay.games.ttt.HeuristicAI;
import org.example.algoplay.games.ttt.RecursiveMoveSelector;
import org.example.algoplay.models.Difficulty;
import org.example.algoplay.models.TicTacToeBoard;
import org.example.algoplay.models.User;
import org.example.algoplay.services.DatabaseService;
import org.example.algoplay.services.UserSessionService;
import org.example.algoplay.utils.TimeTracker;
import org.example.algoplay.services.DatabaseServiceHelper;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class TicTacToeController implements Initializable {

    @FXML private GridPane gameGrid;
    @FXML private Label statusLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label playerNameLabel;
    @FXML private Label moveCountLabel;
    @FXML private Button resetButton;
    @FXML private VBox setupPane;
    @FXML private HBox gameInfoPane;
    @FXML private TextField playerNameField;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private Button startGameButton;

    private final int SIZE = 5;
    private Button[][] buttons;
    private TicTacToeBoard board;
    private boolean playerTurn = true;
    private final HeuristicAI heuristicAI = new HeuristicAI();
    private final RecursiveMoveSelector recursiveAI = new RecursiveMoveSelector();

    private int playerMoves = 0;
    private int aiMoves = 0;
    private String playerName = "Guest"; // Default player name
    private boolean isHeuristicAI = true;
    private Difficulty currentDifficulty = Difficulty.MEDIUM;

    // DATABASE VARIABLES
    private int userId = -1;
    private final int gameId = 1; // Fixed gameId as requested
    private int roundId;
    private int moveNumber = 1;
    private int resultId = -1; // Added to track ttt_game_results
    private final TimeTracker timeTracker = new TimeTracker();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add difficulty options to dropdown
        difficultyComboBox.getItems().addAll("EASY", "MEDIUM", "HARD");
        difficultyComboBox.setValue("MEDIUM"); // Default selection

        // Check if user is already logged in via UserSessionService
        if (UserSessionService.getInstance().isLoggedIn()) {
            playerName = UserSessionService.getInstance().getCurrentUser().getUsername();
            userId = UserSessionService.getInstance().getCurrentUserId();
            playerNameField.setText(playerName);
        }

        // Initialize board but keep it hidden until Start Game is clicked
        board = new TicTacToeBoard(SIZE);
        buttons = new Button[SIZE][SIZE];
        createGameGrid();
    }

    private void createGameGrid() {
        // Clear existing grid
        gameGrid.getChildren().clear();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Button btn = new Button("");
                btn.setMinSize(60, 60);
                btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                GridPane.setFillWidth(btn, true);
                GridPane.setFillHeight(btn, true);

                int row = i, col = j;
                btn.setOnAction(e -> handleCellClick(row, col, btn));

                buttons[i][j] = btn;
                gameGrid.add(btn, j, i);
            }
        }
    }

    @FXML
    private void startGame() {
        // Get player name from the text field
        String enteredName = playerNameField.getText().trim();
        if (!enteredName.isEmpty()) {
            playerName = enteredName;
        } else {
            playerName = "Guest";
        }

        // Set player name label
        playerNameLabel.setText("Player: " + playerName);

        // Get selected difficulty
        String selectedDifficulty = difficultyComboBox.getValue();
        setDifficulty(selectedDifficulty);

        // Update user information in database
        ensureValidUser();

        // Show game elements and hide setup elements
        setupPane.setVisible(false);
        gameInfoPane.setVisible(true);
        statusLabel.setVisible(true);
        gameGrid.setVisible(true);
        moveCountLabel.setVisible(true);

        // Reset game state
        resetGameState();
    }

    private void resetGameState() {
        board.clearBoard();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setStyle("");
                buttons[i][j].setDisable(false);
            }
        }

        playerTurn = true;
        playerMoves = 0;
        aiMoves = 0;
        moveNumber = 1;
        resultId = -1; // Reset the resultId for new game
        updateMoveCountLabel();
        statusLabel.setText("Your turn");
    }

    private void ensureValidUser() {
        if (userId <= 0) {
            // First check if user exists
            ResultSet rs = null;
            try {
                rs = DatabaseService.getInstance().executeQuery(
                        "SELECT user_id FROM users WHERE username = ?", playerName);

                if (rs != null && rs.next()) {
                    userId = rs.getInt("user_id");
                    System.out.println("Found existing user with ID: " + userId);
                } else {
                    // Create new user if not exists
                    userId = saveUser(playerName);
                    System.out.println("Created new user with ID: " + userId);

                    // Update UserSessionService if user is created
                    if (userId > 0) {
                        User user = new User(userId, playerName);
                        UserSessionService.getInstance().setCurrentUser(user);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error checking user: " + e.getMessage());
                e.printStackTrace();
                // Create user as fallback
                userId = saveUser(playerName);

                // Update UserSessionService if user is created
                if (userId > 0) {
                    User user = new User(userId, playerName);
                    UserSessionService.getInstance().setCurrentUser(user);
                }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        System.err.println("Error closing ResultSet: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Set the difficulty level for this game
     * @param difficultyString The difficulty as a string ("EASY", "MEDIUM", "HARD")
     */
    public void setDifficulty(String difficultyString) {
        try {
            // Convert string to enum
            Difficulty difficulty = Difficulty.valueOf(difficultyString.toUpperCase());
            this.currentDifficulty = difficulty;

            // Set difficulty for both AI implementations
            heuristicAI.setDifficulty(difficulty);
            recursiveAI.setDifficulty(difficulty);

            switch (difficulty) {
                case EASY:
                    isHeuristicAI = true;
                    break;
                case MEDIUM:
                    isHeuristicAI = true;
                    break;
                case HARD:
                    isHeuristicAI = false; // Use RecursiveAI for HARD
                    break;
            }
            difficultyLabel.setText("Difficulty: " + difficultyString);
        } catch (IllegalArgumentException e) {
            // Default to MEDIUM if invalid string
            System.err.println("Invalid difficulty: " + difficultyString);
            currentDifficulty = Difficulty.MEDIUM;
            isHeuristicAI = true;
            heuristicAI.setDifficulty(Difficulty.MEDIUM);
            recursiveAI.setDifficulty(Difficulty.MEDIUM);
            difficultyLabel.setText("Difficulty: Medium");
        }
    }

    private void handleCellClick(int row, int col, Button btn) {
        if (playerTurn && board.isCellEmpty(row, col)) {
            // Player's move
            btn.setText("X");
            btn.setStyle("-fx-background-color: #35C3BB;");
            board.placeMove(row, col, 'X');
            playerMoves++;
            updateMoveCountLabel();

            if (board.checkWin('X')) {
                endGame(true, false); // Player wins
                return;
            }

            if (checkDraw()) return;

            playerTurn = false;
            statusLabel.setText("AI is thinking...");

            // Use a separate thread for AI move to keep UI responsive
            new Thread(this::makeAIMove).start();
        }
    }

    private static class AlgorithmMove {
        String algorithmName;
        long executionTime;
        int moveNumber;

        public AlgorithmMove(String algorithmName, long executionTime, int moveNumber) {
            this.algorithmName = algorithmName;
            this.executionTime = executionTime;
            this.moveNumber = moveNumber;
        }
    }

    private List<AlgorithmMove> pendingAlgorithmMoves = new ArrayList<>();


    private void makeAIMove() {
        timeTracker.start();

        int[] move;
        String algorithmName;

        if (isHeuristicAI) {
            move = heuristicAI.findMove(board);
            algorithmName = "HeuristicAI";
        } else {
            move = recursiveAI.findMove(board);
            algorithmName = "RecursiveAI";
        }

        long executionTime = timeTracker.stop();

        if (move != null) {
            // Save algorithm move for later storage
            pendingAlgorithmMoves.add(new AlgorithmMove(algorithmName, executionTime, moveNumber));
            moveNumber++;

            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                board.placeMove(move[0], move[1], 'O');
                buttons[move[0]][move[1]].setText("O");
                buttons[move[0]][move[1]].setStyle("-fx-background-color: #F0B337;");
                aiMoves++;
                updateMoveCountLabel();

                if (board.checkWin('O')) {
                    endGame(false, false); // AI wins
                    return;
                }

                if (checkDraw()) return;

                playerTurn = true;
                statusLabel.setText("Your turn");
            });
        }
    }

    private void updateMoveCountLabel() {
        moveCountLabel.setText("Player Moves: " + playerMoves + " | AI Moves: " + aiMoves);
    }

    private void endGame(boolean playerWon, boolean isDraw) {
        String result;
        String message;

        if (isDraw) {
            result = "draw";
            message = "It's a Draw!";
            highlightDrawBoard();
        } else if (playerWon) {
            result = "win";
            message = playerName + " Wins!";
            highlightBoard("#90EE90"); // Light green for win
        } else {
            result = "loss";
            message = "Computer Wins!";
            highlightBoard("#FFA07A"); // Light salmon for loss
        }

        statusLabel.setText(message);
        disableBoard();

        // Ensure we have a valid user before saving the game round
        ensureValidUser();

        // Only save if we have a valid user
        if (userId > 0) {
            // Save to ttt_game_results first to get a resultId
            resultId = saveTTTGameResult(userId, playerName, currentDifficulty.name(), result, playerMoves, aiMoves);

            if (resultId <= 0) {
                System.err.println("Failed to save to ttt_game_results. User ID: " + userId);
            } else {
                System.out.println("Game result saved successfully with ID: " + resultId);

                // Now save to the standard game_rounds table too
                boolean isCorrect = "win".equals(result);
                int score = "win".equals(result) ? 100 : ("draw".equals(result) ? 50 : 0);

                roundId = saveGameRound(gameId, userId, isCorrect, score);

                if (roundId <= 0) {
                    System.err.println("Failed to save to game_rounds. User ID: " + userId + ", Game ID: " + gameId);
                } else {
                    System.out.println("Game round saved successfully with ID: " + roundId);

                    // Save to tictactoe_rounds with the generated roundId
                    boolean tttRoundSuccess = saveTicTacToeRound(roundId, gameId, userId, result, currentDifficulty.name());

                    if (!tttRoundSuccess) {
                        System.err.println("Failed to save to tictactoe_rounds. Round ID: " + roundId);
                    } else {
                        System.out.println("TicTacToe round saved successfully");
                    }
                }
            }
        } else {
            System.err.println("Cannot save game round: Invalid user ID");
        }

        if (resultId > 0) {
            // Save all pending algorithm moves
            for (AlgorithmMove move : pendingAlgorithmMoves) {
                saveAlgorithmPerformance(resultId, move.algorithmName, move.executionTime, move.moveNumber);
            }
            // Clear the list for the next game
            pendingAlgorithmMoves.clear();
        }
    }

    private void highlightBoard(String color) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setStyle("-fx-background-color: " + color + ";");
            }
        }
    }

    private void highlightDrawBoard() {
        highlightBoard("#D3D3D3"); // Light gray for draw
    }

    private void disableBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setDisable(true);
            }
        }
    }

    @FXML
    private void resetGame() {
        // Show setup panel and hide game elements
        setupPane.setVisible(true);
        gameInfoPane.setVisible(false);
        statusLabel.setVisible(false);
        gameGrid.setVisible(false);
        moveCountLabel.setVisible(false);

        // Reset game state
        resetGameState();
    }

    private boolean checkDraw() {
        if (board.isFull() && !board.checkWin('X') && !board.checkWin('O')) {
            endGame(false, true); // It's a draw
            return true;
        }
        return false;
    }

    @FXML
    private Button dataViewButton;

    /**
     * Opens the TicTacToe Data View window when the Data View button is clicked
     */
    @FXML
    private void openDataView() {
        try {
            // Load the FXML file - Fixed path issue by checking resource availability and providing clearer error
            URL dataViewLocation = getClass().getResource("/src/main/resources/fxml/TicTacToeDataView.fxml");

            // If the first path doesn't work, try alternatives
            if (dataViewLocation == null) {
                dataViewLocation = getClass().getResource("/src/main/resources/fxml/TicTacToeDataView.fxml");
            }

            // If still null, try a third possible location
            if (dataViewLocation == null) {
                dataViewLocation = getClass().getClassLoader().getResource("/src/main/resources/fxml/TicTacToeDataView.fxml");
            }

            // If still not found, provide a helpful error
            if (dataViewLocation == null) {
                throw new IOException("Could not find the FXML file for the data view. " +
                        "Please ensure tic_tac_toe_data_view.fxml exists in one of the resource paths.");
            }

            FXMLLoader loader = new FXMLLoader(dataViewLocation);
            Parent root = loader.load();

            // Create new stage for data view
            Stage dataViewStage = new Stage();
            dataViewStage.setTitle("Tic-Tac-Toe Data View");
            dataViewStage.setScene(new Scene(root));
            dataViewStage.setMinWidth(900);
            dataViewStage.setMinHeight(600);

            // Show the stage
            dataViewStage.show();
        } catch (IOException e) {
            System.err.println("Error opening Data View: " + e.getMessage());
            e.printStackTrace();

            // Show an error alert to the user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Loading Data View");
            alert.setHeaderText("Could not load the Data View");
            alert.setContentText("Error details: " + e.getMessage() +
                    "\n\nPlease make sure the FXML file exists in the correct location.");
            alert.showAndWait();
        }
    }

    private int saveUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            username = "Guest"; // Fallback to Guest if username is empty
        }

        System.out.println("Creating new user: " + username);

        // Using the new executeInsert method for cleaner code
        return DatabaseServiceHelper.getInstance().executeInsert(
                "INSERT INTO users (username, password) VALUES (?, 'default') RETURNING user_id",
                username);
    }

    private int saveTTTGameResult(int userId, String playerName, String difficulty, String result, int playerMoves, int aiMoves) {
        try {
            // Check for valid inputs
            if (userId <= 0 || playerName == null || difficulty == null || result == null) {
                System.err.println("Invalid parameters for saving TTT game result");
                return -1;
            }

            System.out.println("Saving TTT game result for user ID: " + userId);

            // Using the new executeInsert method
            return DatabaseServiceHelper.getInstance().executeInsert(
                    "INSERT INTO ttt_game_results (user_id, player_name, difficulty, result, player_moves, ai_moves) " +
                            "VALUES (?, ?, ?, ?, ?, ?) RETURNING result_id",
                    userId, playerName, difficulty, result, playerMoves, aiMoves);

        } catch (Exception e) {
            System.err.println("Error saving TTT game result: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private int saveGameRound(int gameId, int userId, boolean isCorrect, int score) {
        try {
            // Extra check to ensure valid userId and gameId
            if (userId <= 0 || gameId <= 0) {
                System.err.println("Cannot save game round: Invalid IDs. User ID: " + userId + ", Game ID: " + gameId);
                return -1;
            }

            System.out.println("Saving game round with User ID: " + userId + ", Game ID: " + gameId);

            // Using the new executeInsert method
            return DatabaseServiceHelper.getInstance().executeInsert(
                    "INSERT INTO game_rounds (game_id, user_id, is_correct, score) " +
                            "VALUES (?, ?, ?, ?) RETURNING round_id",
                    gameId, userId, isCorrect, score);

        } catch (Exception e) {
            System.err.println("Error saving game round: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private boolean saveTicTacToeRound(int roundId, int gameId, int userId, String result, String difficulty) {
        ResultSet checkRs = null;
        try {
            // First check if a record with this round_id already exists
            checkRs = DatabaseService.getInstance().executeQuery(
                    "SELECT 1 FROM tictactoe_rounds WHERE round_id = ?",
                    roundId);

            boolean recordExists = checkRs != null && checkRs.next();

            // If record already exists, return true without trying to insert
            if (recordExists) {
                System.out.println("tictactoe_rounds record already exists for round_id=" + roundId + ", skipping insertion");
                return true;
            }

            System.out.println("Inserting new record into tictactoe_rounds with round_id=" + roundId);

            // Otherwise, proceed with insertion
            boolean success = DatabaseService.getInstance().executeUpdate(
                    "INSERT INTO tictactoe_rounds (round_id, game_id, user_id, result, difficulty) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    roundId, gameId, userId, result, difficulty);

            if (success) {
                System.out.println("Successfully inserted into tictactoe_rounds table with round_id=" + roundId);
            } else {
                System.err.println("Failed to insert into tictactoe_rounds, no rows affected");
            }

            return success;

        } catch (Exception e) {
            System.err.println("Error inserting into tictactoe_rounds: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (checkRs != null) {
                try {
                    checkRs.close();
                } catch (SQLException e) {
                    System.err.println("Error closing ResultSet: " + e.getMessage());
                }
            }
        }
    }

    private void saveAlgorithmPerformance(int resultId, String algorithmName, long executionTime, int moveNumber) {
        try {
            // If we don't have a valid resultId yet, create a temporary one
            if (resultId <= 0) {
                // Create a temporary result record
                resultId = DatabaseServiceHelper.getInstance().executeInsert(
                        "INSERT INTO ttt_game_results (user_id, player_name, difficulty, result, player_moves, ai_moves) " +
                                "VALUES (?, ?, ?, ?, ?, ?) RETURNING result_id",
                        userId, playerName, currentDifficulty.name(), "in_progress", playerMoves, aiMoves);

                // Update the class field with the new resultId
                this.resultId = resultId;
            }

            if (resultId <= 0) {
                System.err.println("Cannot save algorithm performance: Failed to create temporary result record");
                return;
            }

            System.out.println("Saving algorithm performance for result ID: " + resultId);

            boolean success = DatabaseService.getInstance().executeUpdate(
                    "INSERT INTO ttt_algorithm_performance (result_id, algorithm_name, execution_time, move_number) " +
                            "VALUES (?, ?, ?, ?)",
                    resultId, algorithmName, executionTime, moveNumber);

            if (success) {
                System.out.println("Algorithm performance saved: " + algorithmName + ", Time: " + executionTime + "ms, Move: " + moveNumber);
            } else {
                System.err.println("Failed to save algorithm performance, no rows affected");
            }

        } catch (Exception e) {
            System.err.println("Failed to save algorithm performance: " + e.getMessage());
            e.printStackTrace();
        }
    }
}