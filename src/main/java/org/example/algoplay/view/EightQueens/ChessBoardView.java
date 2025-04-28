
package org.example.algoplay.view.EightQueens;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ChessBoardView extends VBox {
    private static final int SIZE = 8;
    private static final int SQUARE_SIZE = 60;
    private final GridPane board = new GridPane();
    private final StackPane[][] cells = new StackPane[SIZE][SIZE];
    private final ImageView[][] queenImages = new ImageView[SIZE][SIZE];
    private final Image queenImage;
    private final Label messageLabel = new Label();

    private static final Color LIGHT_SQUARE = Color.rgb(240, 217, 181);
    private static final Color DARK_SQUARE = Color.rgb(145, 98, 67);
    private static final Color BORDER_COLOR = Color.rgb(121, 72, 57);

    public ChessBoardView() {
        queenImage = loadQueenImage();
        configureBoard();
        configureMessageLabel();

        setSpacing(10);
        setAlignment(Pos.CENTER);
        getChildren().addAll(board, messageLabel);
    }

    private Image loadQueenImage() {
        try {
            Image img = new Image(getClass().getResourceAsStream("/images/queen.png"));
            if (img.isError()) {
                System.err.println("Error loading queen image: " + img.getException().getMessage());
            }
            return img;
        } catch (Exception e) {
            System.err.println("Could not load queen image: " + e.getMessage());
            return null;
        }
    }

    private void configureBoard() {
        board.setAlignment(Pos.CENTER);
        board.setHgap(1);
        board.setVgap(1);
        board.setStyle("-fx-background-color: " + toRGBCode(BORDER_COLOR) + "; -fx-padding: 5;");
        createBoard();
    }

    private void configureMessageLabel() {
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        messageLabel.setAlignment(Pos.CENTER);
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    private void createBoard() {
        DropShadow dropShadow = new DropShadow(2.0, 1.0, 1.0, Color.rgb(0, 0, 0, 0.3));

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                StackPane cell = createCell(row, col, dropShadow);
                cells[row][col] = cell;
                board.add(cell, col, row);
            }
        }
    }

    public void placeQueen(int row, int col) {
        if (queenImage == null || queenImage.isError()) {
            System.err.println("Cannot place queen: image not available");
            return;
        }

        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return;

        // Check if the position already has a queen
        if (hasQueen(row, col)) {
            System.out.println("Queen already present at this position");
            return;
        }

        ImageView queen = createQueenImageView();
        FadeTransition fade = createQueenFadeTransition(queen);

        queenImages[row][col] = queen; // Update queenImages array
        cells[row][col].getChildren().add(queen);
        fade.play();
    }

    public boolean isCurrentSolutionCorrect() {
        // Check if all queens are placed correctly
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (queenImages[row][col] != null) { // There's a queen at position (row, col)
                    // Check if the queen is safe: no other queens in the same column or diagonal
                    for (int otherRow = 0; otherRow < SIZE; otherRow++) {
                        if (otherRow != row && queenImages[otherRow][col] != null) {
                            return false; // Found another queen in the same column
                        }
                    }

                    for (int otherCol = 0; otherCol < SIZE; otherCol++) {
                        if (otherCol != col && queenImages[row][otherCol] != null) {
                            return false; // Found another queen in the same row
                        }
                    }

                    // Check diagonals
                    if (isQueenOnDiagonal(row, col)) {
                        return false; // Found another queen on the same diagonal
                    }
                }
            }
        }
        return true; // No conflicts found, solution is correct
    }

    private boolean isQueenOnDiagonal(int row, int col) {
        // Check for diagonals (top-left to bottom-right and top-right to bottom-left)
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (Math.abs(row - i) == Math.abs(col - j) && queenImages[i][j] != null && (i != row || j != col)) {
                    return true; // Found another queen on the same diagonal
                }
            }
        }
        return false; // No other queen found on the diagonal
    }

    public void removeQueen(int row, int col) {
        if (queenImages[row][col] != null) {
            cells[row][col].getChildren().remove(queenImages[row][col]);
            queenImages[row][col] = null; // Update queenImages array
        }
    }

    private ImageView createQueenImageView() {
        ImageView queen = new ImageView(queenImage);
        queen.setFitWidth(SQUARE_SIZE * 0.8);
        queen.setFitHeight(SQUARE_SIZE * 0.8);
        queen.setPreserveRatio(true);
        queen.setSmooth(true);
        queen.setEffect(new DropShadow(3.0, 2.0, 2.0, Color.rgb(0, 0, 0, 0.5)));
        return queen;
    }

    private FadeTransition createQueenFadeTransition(ImageView queen) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), queen);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        return fade;
    }

    public void removeQueen(int col) {
        for (int row = 0; row < SIZE; row++) {
            if (queenImages[row][col] != null) {
                cells[row][col].getChildren().remove(queenImages[row][col]);
                queenImages[row][col] = null;
                break;
            }
        }
    }

    public void highlightSquare(int row, int col, Color color) {
        if (isValidPosition(row, col)) {
            Rectangle square = (Rectangle) cells[row][col].getChildren().get(0);
            square.setStroke(color);
            square.setStrokeWidth(3);
        }
    }

    public void unhighlightSquare(int row, int col) {
        if (isValidPosition(row, col)) {
            Rectangle square = (Rectangle) cells[row][col].getChildren().get(0);
            square.setStroke(null);
        }
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public void clearBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                removeQueen(row, col);
                unhighlightSquare(row, col);
            }
        }
    }

    private StackPane createCell(int row, int col, DropShadow dropShadow) {
        StackPane cell = new StackPane();
        cell.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

        Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
        square.setFill((row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
        square.setEffect(dropShadow);

        cell.setUserData(new int[]{row, col});
        cell.getChildren().add(square);

        // ✅ Updated logic to limit to 8 queens and toggle on click
        cell.setOnMouseClicked(event -> {
            if (hasQueen(row, col)) {
                removeQueen(row, col);
                showMessage(""); // Clear message when queen is removed
            } else {
                if (countQueens() < SIZE) {
                    placeQueen(row, col);
                } else {
                    showAlert("You can only place 8 queens!");
                }
            }
        });

        return cell;
    }

    // ✅ New method to count queens on the board
    public int countQueens() {
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (queenImages[row][col] != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public void loadSolution(int[] queenPositions) {
        clearBoard();
        for (int row = 0; row < SIZE; row++) {
            int col = queenPositions[row];
            if (col >= 0 && col < SIZE) {
                placeQueen(row, col);
            }
        }
    }

    public int[] getCurrentPlayerSolution() {
        int[] positions = new int[SIZE];
        for (int row = 0; row < SIZE; row++) {
            positions[row] = -1;
            for (int col = 0; col < SIZE; col++) {
                if (queenImages[row][col] != null) {
                    positions[row] = col;
                    break;
                }
            }
        }
        return positions;
    }

    public boolean hasQueen(int row, int col) {
        return isValidPosition(row, col) && queenImages[row][col] != null;
    }

    public void showMessage(String message) {
        messageLabel.setText(message);
    }

    public boolean isValidSolution() {
        int[] positions = getCurrentPlayerSolution();
        for (int i = 0; i < positions.length; i++) {
            for (int j = i + 1; j < positions.length; j++) {
                if (positions[i] == positions[j] || Math.abs(i - j) == Math.abs(positions[i] - positions[j])) {
                    return false; // Found an invalid solution
                }
            }
        }
        return true; // Solution is valid
    }

    public void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}



