package org.example.algoplay.controllers.games;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.example.algoplay.models.BacktrackingKnightTour;
import org.example.algoplay.models.WarnsdorffKnightTour;
import org.example.algoplay.services.DatabaseManager;
import org.example.algoplay.services.UserSessionService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KTController {
    @FXML private ComboBox<String> algorithmComboBox;
    @FXML private GridPane chessBoard;
    @FXML private Label statusLabel;
    @FXML private Label startPositionLabel;
    @FXML private Label movesCountLabel;
    @FXML private Label timeLabel;
    @FXML private Button undoButton;

    private final int BOARD_SIZE = 5;
    private Rectangle[][] boardSquares;
    private Point startingPosition;
    private List<Point> solutionPath;
    private DatabaseManager dbManager;
    private UserSessionService userSessionService;

    // Added for manual movement
    private Point currentPosition;
    private int moveCount = 0;
    private List<Point> playerMoves = new ArrayList<>();
    private boolean gameInProgress = false;
    private long startTime; // Track when the game started

    // For hint functionality
    private Point hintPosition = null;

    // Add this to track if backtracking is active
    private boolean backtrackingEnabled = false;

    // Pre-calculated complete tour for hints
    private List<Point> completeTourPath = null;

    // Model instances
    private BacktrackingKnightTour backtrackingModel;
    private WarnsdorffKnightTour warnsdorffModel;

    @FXML
    public void initialize() {
        algorithmComboBox.getItems().addAll("Backtracking", "Warnsdorff");
        algorithmComboBox.setValue("Warnsdorff");
        initializeChessBoard();
        dbManager = new DatabaseManager();
        userSessionService = UserSessionService.getInstance(); // Initialize the user session service

        // Check if user is logged in
        if (userSessionService.isLoggedIn()) {
            String username = userSessionService.getCurrentUser().getUsername();
            statusLabel.setText("Welcome " + username + "! Click Start Game");

        }else {
            statusLabel.setText("Please log in to play the game");
        }

        // Initialize models
        backtrackingModel = new BacktrackingKnightTour(BOARD_SIZE);
        warnsdorffModel = new WarnsdorffKnightTour(BOARD_SIZE);

        // Initialize undo button as disabled
        undoButton.setDisable(true);

        // Enable backtracking when "Backtracking" algorithm is selected
        algorithmComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            backtrackingEnabled = "Backtracking".equals(newValue);
        });

    }

    private void initializeChessBoard() {
        boardSquares = new Rectangle[BOARD_SIZE][BOARD_SIZE];
        chessBoard.getChildren().clear();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle square = new Rectangle(70, 70);
                square.setFill((row + col) % 2 == 0 ? Color.WHITE : Color.DARKGRAY);

                final int r = row;
                final int c = col;
                square.setOnMouseClicked(e -> handleSquareClick(r, c));

                boardSquares[row][col] = square;

                // Create a StackPane to properly display the square
                StackPane stackPane = new StackPane();
                stackPane.getChildren().add(square);

                // Add mouse click event to the StackPane itself
                stackPane.setOnMouseClicked(e -> handleSquareClick(r, c));

                // Add the StackPane to the grid instead of just the rectangle
                chessBoard.add(stackPane, col, row);
            }
        }
    }

    @FXML
    private void startGame() {
        // Check if user is logged in
        if (!userSessionService.isLoggedIn()) {
            showAlert("Error", "You must be logged in to play");
            return;
        }

        // Enable or disable backtracking based on algorithm choice
        backtrackingEnabled = "Backtracking".equals(algorithmComboBox.getValue());

        // Enable/disable undo button based on algorithm
        undoButton.setDisable(!backtrackingEnabled);

        // Reset any previous game state
        resetGame();

        // Disable algorithm selection after game starts
        algorithmComboBox.setDisable(true);

        // Enable game in progress flag
        gameInProgress = true;

        // Record start time
        startTime = System.currentTimeMillis();

        // Find a starting position that guarantees a solution
        findSuitableStartingPosition();

        // Initialize player moves list
        playerMoves = new ArrayList<>();
        playerMoves.add(new Point(startingPosition.x, startingPosition.y));
        currentPosition = new Point(startingPosition.x, startingPosition.y);
        moveCount = 0;

        resetChessBoard();

        // Mark starting position with blue and place a knight symbol
        Rectangle startSquare = new Rectangle(50, 50);
        startSquare.setFill(Color.CORNFLOWERBLUE);

        Text knightText = new Text("♞"); // Unicode knight chess symbol
        knightText.setStyle("-fx-font-size: 24;");

        // Update the UI with the knight at starting position
        updateChessBoardCell(startingPosition.x, startingPosition.y, startSquare, knightText);

        startPositionLabel.setText("Starting Position: (" + startingPosition.x + "," + startingPosition.y + ")");
        statusLabel.setText("Game Start! Click on valid squares to move the knight.");
        movesCountLabel.setText("Moves: " + moveCount);

        // Show valid moves from starting position
        highlightValidMoves(startingPosition.x, startingPosition.y);
    }

    // Find a truly random starting position that guarantees a solution
    private void findSuitableStartingPosition() {
        String selectedAlgorithm = algorithmComboBox.getValue();
        boolean solutionFound = false;
        int attempts = 0;
        Random random = new Random();

        while (!solutionFound && attempts < 100) { // You can adjust the attempt limit if needed
            attempts++;
            int startX = random.nextInt(BOARD_SIZE);
            int startY = random.nextInt(BOARD_SIZE);

            if (tryStartingPosition(startX, startY, selectedAlgorithm)) {
                startingPosition = new Point(startX, startY);
                solutionFound = true;
                break;
            }
        }

        if (!solutionFound) {
            // As a very last resort, fall back to (0,0) but it's unlikely if attempts are enough
            startingPosition = new Point(0, 0);
            System.out.println("Warning: Could not find a guaranteed random start, using fallback (0,0).");

            if ("Backtracking".equals(selectedAlgorithm)) {
                if (!backtrackingModel.solveKnightTour(0, 0)) {
                    warnsdorffModel.solveKnightTour(0, 0);
                    completeTourPath = convertWarnsdorffPathToPoints(warnsdorffModel.getSolutionPath());
                } else {
                    completeTourPath = convertBacktrackingPathToPoints(backtrackingModel.getSolutionPath());
                }
            } else {
                if (!warnsdorffModel.solveKnightTour(0, 0)) {
                    backtrackingModel.solveKnightTour(0, 0);
                    completeTourPath = convertBacktrackingPathToPoints(backtrackingModel.getSolutionPath());
                } else {
                    completeTourPath = convertWarnsdorffPathToPoints(warnsdorffModel.getSolutionPath());
                }
            }
        }
    }

    private boolean tryStartingPosition(int startX, int startY, String selectedAlgorithm) {
        if ("Backtracking".equals(selectedAlgorithm)) {
            backtrackingModel = new BacktrackingKnightTour(BOARD_SIZE);
            if (backtrackingModel.solveKnightTour(startX, startY)) {
                completeTourPath = convertBacktrackingPathToPoints(backtrackingModel.getSolutionPath());
                return true;
            }
        } else { // Warnsdorff
            warnsdorffModel = new WarnsdorffKnightTour(BOARD_SIZE);
            if (warnsdorffModel.solveKnightTour(startX, startY)) {
                completeTourPath = convertWarnsdorffPathToPoints(warnsdorffModel.getSolutionPath());
                return true;
            }
        }

        // Try the other algorithm if the first one failed
        if ("Backtracking".equals(selectedAlgorithm)) {
            warnsdorffModel = new WarnsdorffKnightTour(BOARD_SIZE);
            if (warnsdorffModel.solveKnightTour(startX, startY)) {
                completeTourPath = convertWarnsdorffPathToPoints(warnsdorffModel.getSolutionPath());
                return true;
            }
        } else {
            backtrackingModel = new BacktrackingKnightTour(BOARD_SIZE);
            if (backtrackingModel.solveKnightTour(startX, startY)) {
                completeTourPath = convertBacktrackingPathToPoints(backtrackingModel.getSolutionPath());
                return true;
            }
        }

        return false;
    }

    private List<Point> convertBacktrackingPathToPoints(List<BacktrackingKnightTour.Point> modelPath) {
        List<Point> path = new ArrayList<>();
        for (BacktrackingKnightTour.Point p : modelPath) {
            path.add(new Point(p.x, p.y));
        }
        return path;
    }

    private List<Point> convertWarnsdorffPathToPoints(List<WarnsdorffKnightTour.Point> modelPath) {
        List<Point> path = new ArrayList<>();
        for (WarnsdorffKnightTour.Point p : modelPath) {
            path.add(new Point(p.x, p.y));
        }
        return path;
    }

    private void resetChessBoard() {
        // Clear the chess board and rebuild it
        chessBoard.getChildren().clear();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle square = new Rectangle(50, 50);
                square.setFill((row + col) % 2 == 0 ? Color.MINTCREAM : Color.MIDNIGHTBLUE);
                boardSquares[row][col] = square;

                final int r = row;
                final int c = col;
                square.setOnMouseClicked(e -> handleSquareClick(r, c));

                StackPane stackPane = new StackPane();
                stackPane.getChildren().add(square);

                // Add mouse click event to the StackPane itself as well
                final int finalRow = row;
                final int finalCol = col;
                stackPane.setOnMouseClicked(e -> handleSquareClick(finalRow, finalCol));

                chessBoard.add(stackPane, col, row);
            }
        }
    }

    private void handleSquareClick(int row, int col) {

        // Check if user is logged in
        if (!userSessionService.isLoggedIn()) {
            showAlert("Error", "You must be logged in to play");
            return;
        }

        // If game is in progress, handle move
        if (gameInProgress) {
            makeMove(row, col);
        }
    }

    private void makeMove(int row, int col) {
        // Check if the move is valid
        if (!isValidKnightMove(currentPosition.x, currentPosition.y, row, col)) {
            statusLabel.setText("Invalid move! Knights move in an L-shape.");
            return;
        }

        // Check if the square has been visited before
        if (hasVisited(row, col)) {
            statusLabel.setText("Square already visited! Try another move.");
            return;
        }

        // Clear previous hint if any
        if (hintPosition != null) {
            clearHint();
        }

        // Valid move, update game state
        moveCount++;
        currentPosition = new Point(row, col);
        playerMoves.add(currentPosition);

        // Update the UI
        resetChessBoard();
        drawVisitedPath();

        // Place the knight at the new position
        Rectangle knightSquare = new Rectangle(50, 50);
        knightSquare.setFill(Color.CORNFLOWERBLUE);
        Text knightText = new Text("♞");
        knightText.setStyle("-fx-font-size: 24;");
        updateChessBoardCell(row, col, knightSquare, knightText);

        // Update labels
        movesCountLabel.setText("Moves: " + moveCount);

        // Highlight valid moves from the new position
        highlightValidMoves(row, col);

        // Check if the game is complete
        checkGameCompletion();
    }

    private boolean isValidKnightMove(int fromX, int fromY, int toX, int toY) {
        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);
        return (dx == 1 && dy == 2) || (dx == 2 && dy == 1);
    }

    private boolean hasVisited(int row, int col) {
        for (Point p : playerMoves) {
            if (p.x == row && p.y == col) {
                return true;
            }
        }
        return false;
    }

    private void highlightValidMoves(int row, int col) {
        List<Point> validMoves = getValidKnightMoves(row, col);

        for (Point move : validMoves) {
            Rectangle validSquare = new Rectangle(50, 50);
            validSquare.setFill(Color.LIGHTGREEN);
            validSquare.setOpacity(0.5);

            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(validSquare);

            final int r = move.x;
            final int c = move.y;
            stackPane.setOnMouseClicked(e -> handleSquareClick(r, c));

            // Add to the board
            chessBoard.add(stackPane, move.y, move.x);
        }
    }

    private List<Point> getValidKnightMoves(int row, int col) {
        List<Point> validMoves = new ArrayList<>();

        // Knight's move offsets (L-shape)
        int[] dx = {2, 1, -1, -2, -2, -1, 1, 2};
        int[] dy = {1, 2, 2, 1, -1, -2, -2, -1};

        // Check all possible knight moves
        for (int i = 0; i < 8; i++) {
            int newRow = row + dx[i];
            int newCol = col + dy[i];

            // Check if the move is within the board
            if (newRow >= 0 && newRow < BOARD_SIZE && newCol >= 0 && newCol < BOARD_SIZE) {
                // Check if the square has been visited
                boolean visited = false;
                for (Point p : playerMoves) {
                    if (p.x == newRow && p.y == newCol) {
                        visited = true;
                        break;
                    }
                }

                if (!visited) {
                    validMoves.add(new Point(newRow, newCol));
                }
            }
        }

        return validMoves;
    }

    private void drawVisitedPath() {
        // Draw visited squares with numbers
        for (int i = 0; i < playerMoves.size(); i++) {
            Point p = playerMoves.get(i);

            Rectangle visitedSquare = new Rectangle(50, 50);
            // Use different color for starting position
            if (i == 0) {
                visitedSquare.setFill(Color.LIGHTBLUE);
            } else {
                visitedSquare.setFill(Color.LIGHTSALMON);
            }

            Text moveNumber = new Text(Integer.toString(i));
            moveNumber.setStyle("-fx-font-size: 12;");

            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(visitedSquare, moveNumber);

            // Don't add click handler for visited squares

            chessBoard.add(stackPane, p.y, p.x);
        }
    }

    private void checkGameCompletion() {
        // Game is complete if all squares are visited
        if (playerMoves.size() == BOARD_SIZE * BOARD_SIZE) {
            gameInProgress = false;
            statusLabel.setText("Congratulations! You've completed the Knight's Tour!");

            // Show victory popup
            showAlert("Victory!", "Congratulations! You've completed the Knight's Tour in " + moveCount + " moves and " + (System.currentTimeMillis() - startTime) + "ms!");

            // Save to database
            saveGameToDatabase();

            // Allow starting a new game
            algorithmComboBox.setDisable(false);
            undoButton.setDisable(true);
        } else {
            // Check if there are any valid moves left
            List<Point> validMoves = getValidKnightMoves(currentPosition.x, currentPosition.y);
            if (validMoves.isEmpty()) {
                if (backtrackingEnabled && playerMoves.size() > 1) {
                    statusLabel.setText("No more valid moves! You can backtrack using the Undo button.");
                    // Keep undo button enabled for backtracking
                    undoButton.setDisable(false);
                } else {
                    gameInProgress = false;
                    statusLabel.setText("No more valid moves! Game Over.");

                    // Show dead end popup
                    showAlert("Game Over", "You've reached a dead end with " + moveCount + " moves. No more valid moves available!");

                    // Allow starting a new game
                    algorithmComboBox.setDisable(false);
                    undoButton.setDisable(true);
                }
            } else {
                // Valid moves exist, make sure undo is enabled if backtracking is allowed
                undoButton.setDisable(!backtrackingEnabled);
            }
        }
    }

    private void saveGameToDatabase() {
        String algorithm = algorithmComboBox.getValue();
        String startPosition = startingPosition.x + "," + startingPosition.y;

        // Format solution path as string in the format expected by DatabaseManager
        StringBuilder solutionPathBuilder = new StringBuilder();
        for (int i = 0; i < playerMoves.size(); i++) {
            Point p = playerMoves.get(i);
            solutionPathBuilder.append(p.x).append(",").append(p.y);
            if (i < playerMoves.size() - 1) {
                solutionPathBuilder.append(";");
            }
        }
        String solutionPath = solutionPathBuilder.toString();

        // Calculate execution time
        long executionTime = System.currentTimeMillis() - startTime;

        // Display time in the UI
        timeLabel.setText("Time: " + executionTime + "ms");

        // Save to database
        dbManager.saveSolution(algorithm, startPosition, solutionPath, executionTime);
    }

    @FXML
    private void undoMove() {
        if (!gameInProgress || !backtrackingEnabled || playerMoves.size() <= 1) {
            return;
        }

        // Remove the last move
        playerMoves.remove(playerMoves.size() - 1);

        // Update current position to the previous position
        currentPosition = playerMoves.get(playerMoves.size() - 1);

        // Decrement move counter
        moveCount--;

        // Update UI
        resetChessBoard();
        drawVisitedPath();

        // Place knight at current position
        Rectangle knightSquare = new Rectangle(50, 50);
        knightSquare.setFill(Color.CORNFLOWERBLUE);
        Text knightText = new Text("♞");
        knightText.setStyle("-fx-font-size: 24;");
        updateChessBoardCell(currentPosition.x, currentPosition.y, knightSquare, knightText);

        // Update labels
        movesCountLabel.setText("Moves: " + moveCount);
        statusLabel.setText("Move undone. Try a different path.");

        // Highlight valid moves from new position
        highlightValidMoves(currentPosition.x, currentPosition.y);
    }

    @FXML
    private void resetGame() {
        gameInProgress = false;
        moveCount = 0;
        currentPosition = null;
        hintPosition = null;
        playerMoves.clear();
        resetChessBoard();
        statusLabel.setText("Game reset. Enter your name and click Start Game");
        startPositionLabel.setText("Starting Position: ");
        movesCountLabel.setText("Moves: 0");
        timeLabel.setText("");

        // Allow editing player name and algorithm
        algorithmComboBox.setDisable(false);
        undoButton.setDisable(true);
    }

    @FXML
    private void showHint() {
        if (!gameInProgress) {
            return;
        }

        // Clear previous hint if any
        if (hintPosition != null) {
            clearHint();
        }

        // Find next optimal move in the completeTourPath
        hintPosition = findNextOptimalMove();

        if (hintPosition != null) {
            // Display hint
            Rectangle hintSquare = new Rectangle(50, 50);
            hintSquare.setFill(Color.GOLD);
            hintSquare.setOpacity(0.7);

            Text hintText = new Text("?");
            hintText.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

            StackPane hintPane = new StackPane();
            hintPane.getChildren().addAll(hintSquare, hintText);

            // Add click handler to make the move
            final int hintX = hintPosition.x;
            final int hintY = hintPosition.y;
            hintPane.setOnMouseClicked(e -> handleSquareClick(hintX, hintY));

            chessBoard.add(hintPane, hintPosition.y, hintPosition.x);

            statusLabel.setText("Hint: Move your knight to the highlighted square.");
        } else {
            statusLabel.setText("No optimal move found. Try a different approach.");
        }
    }

    private Point findNextOptimalMove() {
        // Find current position in the completeTourPath
        if (completeTourPath == null || completeTourPath.isEmpty()) {
            // If no pre-calculated path, use the model to find a good move
            return getBestMoveFromModels();
        }

        // First, verify that the player's moves so far match part of the completeTourPath
        boolean pathMatch = true;
        for (int i = 0; i < playerMoves.size(); i++) {
            if (i >= completeTourPath.size() ||
                    playerMoves.get(i).x != completeTourPath.get(i).x ||
                    playerMoves.get(i).y != completeTourPath.get(i).y) {
                pathMatch = false;
                break;
            }
        }

        if (pathMatch && playerMoves.size() < completeTourPath.size()) {
            // Next move in the pre-calculated path
            return completeTourPath.get(playerMoves.size());
        } else {
            // If the player has deviated from the pre-calculated path, use the models
            return getBestMoveFromModels();
        }
    }

    private Point getBestMoveFromModels() {
        // Use current algorithm to find the best next move
        String selectedAlgorithm = algorithmComboBox.getValue();

        if ("Backtracking".equals(selectedAlgorithm)) {
            return convertBacktrackingModelPoint(
                    backtrackingModel.findCompleteTourMove(
                            currentPosition.x, currentPosition.y,
                            convertToBacktrackingModelPoints(playerMoves)
                    )
            );
        } else {
            return convertWarnsdorffModelPoint(
                    warnsdorffModel.findCompleteTourMove(
                            convertToWarnsdorffModelPoints(playerMoves)
                    )
            );
        }
    }

    private List<BacktrackingKnightTour.Point> convertToBacktrackingModelPoints(List<Point> points) {
        List<BacktrackingKnightTour.Point> result = new ArrayList<>();
        for (Point p : points) {
            result.add(new BacktrackingKnightTour.Point(p.x, p.y));
        }
        return result;
    }

    private List<WarnsdorffKnightTour.Point> convertToWarnsdorffModelPoints(List<Point> points) {
        List<WarnsdorffKnightTour.Point> result = new ArrayList<>();
        for (Point p : points) {
            result.add(new WarnsdorffKnightTour.Point(p.x, p.y));
        }
        return result;
    }

    private Point convertBacktrackingModelPoint(BacktrackingKnightTour.Point p) {
        if (p == null) return null;
        return new Point(p.x, p.y);
    }

    private Point convertWarnsdorffModelPoint(WarnsdorffKnightTour.Point p) {
        if (p == null) return null;
        return new Point(p.x, p.y);
    }

    private void clearHint() {
        // Redraw the board without changing position
        resetChessBoard();
        drawVisitedPath();

        // Place the knight at the current position
        Rectangle knightSquare = new Rectangle(50, 50);
        knightSquare.setFill(Color.CORNFLOWERBLUE);
        Text knightText = new Text("♞");
        knightText.setStyle("-fx-font-size: 24;");
        updateChessBoardCell(currentPosition.x, currentPosition.y, knightSquare, knightText);

        // Highlight valid moves
        highlightValidMoves(currentPosition.x, currentPosition.y);

        hintPosition = null;
    }

    private void updateChessBoardCell(int row, int col, Rectangle square, Text text) {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(square, text);

        // Add click handler
        final int r = row;
        final int c = col;
        stackPane.setOnMouseClicked(e -> handleSquareClick(r, c));

        chessBoard.add(stackPane, col, row);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Point {
        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point point = (Point) obj;
            return x == point.x && y == point.y;
        }
    }
}