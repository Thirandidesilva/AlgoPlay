package org.example.algoplay.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

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

    /**
     * Ensures there is a valid user ID available
     */
    private void ensureValidUser() {
        if (userId <= 0) {
            // First check if user exists
            try {
                ResultSet rs = DatabaseService.getInstance().executeQuery(
                        "SELECT user_id FROM users WHERE username = ?", playerName);

                if (rs != null && rs.next()) {
                    userId = rs.getInt("user_id");
                    rs.close();
                } else if (rs != null) {
                    rs.close();
                    // Create new user if not exists
                    userId = saveUser(playerName);

                    // Update UserSessionService if user is created
                    if (userId > 0) {
                        User user = new User(userId, playerName);
                        UserSessionService.getInstance().setCurrentUser(user);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error checking user: " + e.getMessage());
                // Create user as fallback
                userId = saveUser(playerName);

                // Update UserSessionService if user is created
                if (userId > 0) {
                    User user = new User(userId, playerName);
                    UserSessionService.getInstance().setCurrentUser(user);
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
            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                board.placeMove(move[0], move[1], 'O');
                buttons[move[0]][move[1]].setText("O");
                buttons[move[0]][move[1]].setStyle("-fx-background-color: #F0B337;");
                aiMoves++;
                updateMoveCountLabel();

                // Save algorithm performance if we have a valid resultId
                if (resultId > 0) {
                    saveAlgorithmPerformance(resultId, algorithmName, executionTime, moveNumber);
                    moveNumber++;
                }

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

    // Database methods adapted to use DatabaseService
    private int saveUser(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                username = "Guest"; // Fallback to Guest if username is empty
            }

            System.out.println("Creating new user: " + username);

            ResultSet rs = DatabaseService.getInstance().executeQuery(
                    "INSERT INTO users (username, password) VALUES (?, 'default') RETURNING user_id",
                    username);

            if (rs != null && rs.next()) {
                int newUserId = rs.getInt(1);
                rs.close();
                System.out.println("Successfully created user with ID: " + newUserId);
                return newUserId;
            } else if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Save game result to the ttt_game_results table
     */
    private int saveTTTGameResult(int userId, String playerName, String difficulty, String result, int playerMoves, int aiMoves) {
        try {
            // Check for valid inputs
            if (userId <= 0 || playerName == null || difficulty == null || result == null) {
                System.err.println("Invalid parameters for saving TTT game result");
                return -1;
            }

            ResultSet rs = DatabaseService.getInstance().executeQuery(
                    "INSERT INTO ttt_game_results (user_id, player_name, difficulty, result, player_moves, ai_moves) " +
                            "VALUES (?, ?, ?, ?, ?, ?) RETURNING result_id",
                    userId, playerName, difficulty, result, playerMoves, aiMoves);

            if (rs != null && rs.next()) {
                int newResultId = rs.getInt(1);
                rs.close();
                System.out.println("Successfully saved TTT game result with ID: " + newResultId);
                return newResultId;
            } else if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error saving TTT game result: " + e.getMessage());
        }
        return -1;
    }

    private int saveGameRound(int gameId, int userId, boolean isCorrect, int score) {
        try {
            // Extra check to ensure valid userId and gameId
            if (userId <= 0 || gameId <= 0) {
                System.err.println("Cannot save game round: Invalid IDs. User ID: " + userId + ", Game ID: " + gameId);
                return -1;
            }

            System.out.println("Saving game round with User ID: " + userId + ", Game ID: " + gameId);

            ResultSet gameRoundRs = DatabaseService.getInstance().executeQuery(
                    "INSERT INTO game_rounds (game_id, user_id, is_correct, score) " +
                            "VALUES (?, ?, ?, ?) RETURNING round_id",
                    gameId, userId, isCorrect, score);

            int gameRoundId = -1;
            if (gameRoundRs != null && gameRoundRs.next()) {
                gameRoundId = gameRoundRs.getInt(1);
                gameRoundRs.close();
                System.out.println("Successfully inserted into game_rounds with ID: " + gameRoundId);
                return gameRoundId;
            } else if (gameRoundRs != null) {
                gameRoundRs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error saving game round: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Save data to tictactoe_rounds table
     */
    private boolean saveTicTacToeRound(int roundId, int gameId, int userId, String result, String difficulty) {
        try {
            // First check if a record with this round_id already exists
            ResultSet checkRs = DatabaseService.getInstance().executeQuery(
                    "SELECT 1 FROM tictactoe_rounds WHERE round_id = ?",
                    roundId);

            boolean recordExists = checkRs != null && checkRs.next();
            if (checkRs != null) {
                checkRs.close();
            }

            // If record already exists, return true without trying to insert
            if (recordExists) {
                System.out.println("tictactoe_rounds record already exists for round_id=" + roundId + ", skipping insertion");
                return true;
            }

            // Otherwise, proceed with insertion
            DatabaseService.getInstance().executeUpdate(
                    "INSERT INTO tictactoe_rounds (round_id, game_id, user_id, result, difficulty) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    roundId, gameId, userId, result, difficulty);

            System.out.println("Successfully inserted into tictactoe_rounds table with round_id=" + roundId);
            return true;
        } catch (Exception e) {
            System.err.println("Error inserting into tictactoe_rounds: " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves the algorithm performance data to the database
     * @param resultId The result ID from ttt_game_results table
     * @param algorithmName The name of the algorithm used
     * @param executionTime The execution time in milliseconds
     * @param moveNumber The move number in the game
     */
    private void saveAlgorithmPerformance(int resultId, String algorithmName, long executionTime, int moveNumber) {
        String insertQuery = "INSERT INTO ttt_algorithm_performance (result_id, algorithm_name, execution_time, move_number) " +
                "VALUES (?, ?, ?, ?)";

        try {
            DatabaseService.getInstance().executeUpdate(insertQuery,
                    resultId,
                    algorithmName,
                    executionTime,
                    moveNumber);
            System.out.println("Algorithm performance saved: " + algorithmName + ", Time: " + executionTime + "ms, Move: " + moveNumber);
        } catch (Exception e) {
            System.err.println("Failed to save algorithm performance: " + e.getMessage());
        }
    }
}