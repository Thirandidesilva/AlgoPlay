package org.example.algoplay.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.algoplay.games.ttt.HeuristicAI;
import org.example.algoplay.games.ttt.RecursiveMoveSelector;
import org.example.algoplay.models.Difficulty; // Import proper Difficulty enum
import org.example.algoplay.models.TicTacToeBoard;
import org.example.algoplay.services.DatabaseService;
import org.example.algoplay.services.UserSessionService; // Import UserSessionService
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

    private final int SIZE = 5;
    private Button[][] buttons;
    private TicTacToeBoard board;
    private boolean playerTurn = true;
    private final HeuristicAI heuristicAI = new HeuristicAI();
    private final RecursiveMoveSelector recursiveAI = new RecursiveMoveSelector();

    private int playerMoves = 0;
    private int aiMoves = 0;
    private String playerName;
    private boolean isHeuristicAI = true;
    private Difficulty currentDifficulty = Difficulty.MEDIUM;

    // DATABASE VARIABLES
    private int userId;
    private int gameId;
    private int roundId;
    private int moveNumber = 1;
    private final TimeTracker timeTracker = new TimeTracker();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check if user is already logged in via UserSessionService
        if (UserSessionService.getInstance().isLoggedIn()) {
            playerName = UserSessionService.getInstance().getCurrentUser().getUsername();
            userId = UserSessionService.getInstance().getCurrentUserId();
            playerNameLabel.setText("Player: " + playerName);

            // Create a new game entry
            gameId = saveGame("Tic Tac Toe 5x5");
        }

        initializeGame();
    }

    /**
     * Set the player name for this game session
     * @param playerName The name of the player
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        playerNameLabel.setText("Player: " + playerName);

        // Save or retrieve user from database
        try {
            ResultSet rs = DatabaseService.getInstance().executeQuery(
                    "SELECT user_id FROM users WHERE username = ?", playerName);

            if (rs != null && rs.next()) {
                userId = rs.getInt("user_id");
            } else {
                // Create new user if not exists
                saveUser(playerName);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
        }

        // Create a new game entry
        gameId = saveGame("Tic Tac Toe 5x5");
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

            switch (difficulty) {
                case EASY:
                    isHeuristicAI = true;
                    heuristicAI.setDifficulty(Difficulty.EASY);
                    break;
                case MEDIUM:
                    isHeuristicAI = true;
                    heuristicAI.setDifficulty(Difficulty.MEDIUM);
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
            difficultyLabel.setText("Difficulty: Medium");
        }
    }

    private void initializeGame() {
        board = new TicTacToeBoard(SIZE);
        buttons = new Button[SIZE][SIZE];

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

        // Reset game state
        playerTurn = true;
        playerMoves = 0;
        aiMoves = 0;
        moveNumber = 1;
        updateMoveCountLabel();
        statusLabel.setText("Your turn");
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

                // Save algorithm performance
                if (roundId != 0) {
                    saveAlgorithmPerformance(gameId, roundId, algorithmName, executionTime, moveNumber++);
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

        // Save game round result
        roundId = saveGameRound(gameId, userId, result);
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
        initializeGame();
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
            ResultSet rs = DatabaseService.getInstance().executeQuery(
                    "INSERT INTO users (username, password) VALUES (?, 'default') RETURNING user_id",
                    username);

            if (rs != null && rs.next()) {
                userId = rs.getInt(1);
                rs.close();
                return userId;
            }
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
        return -1;
    }

    private int saveGame(String gameName) {
        try {
            ResultSet rs = DatabaseService.getInstance().executeQuery(
                    "INSERT INTO games (game_name) VALUES (?) RETURNING game_id",
                    gameName);

            if (rs != null && rs.next()) {
                int id = rs.getInt(1);
                rs.close();
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error saving game: " + e.getMessage());
        }
        return -1;
    }

    private int saveGameRound(int gameId, int userId, String result) {
        try {
            ResultSet rs = DatabaseService.getInstance().executeQuery(
                    "INSERT INTO game_rounds (game_id, user_id, result, created_at) " +
                            "VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING round_id",
                    gameId, userId, result);

            if (rs != null && rs.next()) {
                int id = rs.getInt(1);
                rs.close();
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error saving game round: " + e.getMessage());
        }
        return -1;
    }

    private void saveAlgorithmPerformance(int gameId, int roundId, String algorithmName,
                                          long executionTime, int moveNumber) {
        DatabaseService.getInstance().executeUpdate(
                "INSERT INTO algorithm_performance (game_id, round_id, algorithm_name, " +
                        "execution_time, move_number, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                gameId, roundId, algorithmName, executionTime, moveNumber);
    }
}