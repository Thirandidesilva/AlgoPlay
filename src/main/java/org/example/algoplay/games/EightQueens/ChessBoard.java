
package org.example.algoplay.games.EightQueens;

public class ChessBoard {
    private int size = 8; // 8x8 board
    private int[] queens = new int[size]; // Where each queen is placed

    // When we start, no queens are on the board
    public ChessBoard() {
        // Set all positions to -1 (no queen)
        for (int i = 0; i < size; i++) {
            queens[i] = -1;
        }
    }

    // Add this method - it was missing!
    public int[] getQueens() {
        return queens.clone();
    }

    // ✅ Add this method to fix the error in GameController
    public int getQueenAt(int col) {
        if (col < 0 || col >= size) {
            throw new IllegalArgumentException("Column index out of bounds: " + col);
        }
        return queens[col];
    }

    // Place a queen at a specific position
    public void placeQueen(int column, int row) {
        queens[column] = row;
    }

    // Remove a queen from a column
    public void removeQueen(int column) {
        queens[column] = -1;
    }

    // Check if a position is safe (no other queen can attack it)
    public boolean isSafe(int column, int row) {
        for (int c = 0; c < column; c++) {
            // Check if same row or same diagonal
            if (queens[c] == row ||
                    queens[c] == row - (column - c) ||
                    queens[c] == row + (column - c)) {
                return false;
            }
        }
        return true;
    }
}
