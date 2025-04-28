package org.example.algoplay.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BacktrackingKnightTour {
    private static final int[] X_MOVES = {2, 1, -1, -2, -2, -1, 1, 2};
    private static final int[] Y_MOVES = {1, 2, 2, 1, -1, -2, -2, -1};
    private int boardSize;
    private int[][] board;

    public BacktrackingKnightTour(int boardSize) {
        this.boardSize = boardSize;
        this.board = new int[boardSize][boardSize];
        resetBoard();
    }

    private void resetBoard() {
        // Initialize board with -1
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = -1;
            }
        }
    }

    public boolean solveKnightTour(int startX, int startY) {
        resetBoard();
        // Place the knight at starting position
        board[startX][startY] = 0;

        // Start the recursive backtracking from move 1
        return solveKTUtil(startX, startY, 1);
    }

    private boolean solveKTUtil(int x, int y, int moveCount) {
        // Base case: If all squares are visited
        if (moveCount == boardSize * boardSize) {
            return true;
        }

        // Try all next moves from current position
        for (int i = 0; i < 8; i++) {
            int nextX = x + X_MOVES[i];
            int nextY = y + Y_MOVES[i];

            if (isValidMove(nextX, nextY)) {
                board[nextX][nextY] = moveCount;

                if (solveKTUtil(nextX, nextY, moveCount + 1)) {
                    return true;
                }

                // Backtrack if the move doesn't lead to a solution
                board[nextX][nextY] = -1;
            }
        }
        return false;
    }

    private boolean isValidMove(int x, int y) {
        return (x >= 0 && x < boardSize &&
                y >= 0 && y < boardSize &&
                board[x][y] == -1);
    }

    public int[][] getBoard() {
        return board;
    }

    public List<Point> getSolutionPath() {
        List<Point> path = new ArrayList<>();
        int[][] solution = getBoard();

        // Create a map to sort the path by move number
        Map<Integer, Point> moveMap = new HashMap<>();

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (solution[i][j] >= 0) {
                    moveMap.put(solution[i][j], new Point(i, j));
                }
            }
        }

        // Add points in order of moves
        for (int i = 0; i < moveMap.size(); i++) {
            path.add(moveMap.get(i));
        }

        return path;
    }

    // Improved method to find a move that leads to a complete tour
    public Point findCompleteTourMove(int x, int y, List<Point> existingMoves) {
        // Create a temporary board with the existing moves
        int[][] tempBoard = new int[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                tempBoard[i][j] = -1;
            }
        }

        // Mark existing moves on temp board
        for (int i = 0; i < existingMoves.size(); i++) {
            Point p = existingMoves.get(i);
            tempBoard[p.x][p.y] = i;
        }

        // Store original board and use temp board for calculations
        int[][] originalBoard = board;
        board = tempBoard;

        // Get valid moves from current position
        List<Point> validMoves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int nextX = x + X_MOVES[i];
            int nextY = y + Y_MOVES[i];

            if (nextX >= 0 && nextX < boardSize &&
                    nextY >= 0 && nextY < boardSize &&
                    board[nextX][nextY] == -1) {
                validMoves.add(new Point(nextX, nextY));
            }
        }

        // Try each move to see if it leads to a complete tour
        for (Point move : validMoves) {
            board[move.x][move.y] = existingMoves.size();
            if (solveKTUtil(move.x, move.y, existingMoves.size() + 1)) {
                // Restore original board before returning
                board = originalBoard;
                return move;
            }
            // Reset this position for the next attempt
            board[move.x][move.y] = -1;
        }

        // If no guaranteed completion found, return the move with most onward possibilities
        if (!validMoves.isEmpty()) {
            // Sort moves by accessibility (Warnsdorff's heuristic)
            int bestScore = -1;
            Point bestMove = null;
            for (Point move : validMoves) {
                int accessScore = 8 - countAccessibility(move.x, move.y);
                if (accessScore > bestScore) {
                    bestScore = accessScore;
                    bestMove = move;
                }
            }
            board = originalBoard;
            return bestMove;
        }

        // Restore original board
        board = originalBoard;
        return null;
    }

    private int countAccessibility(int x, int y) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int nextX = x + X_MOVES[i];
            int nextY = y + Y_MOVES[i];
            if (nextX >= 0 && nextX < boardSize &&
                    nextY >= 0 && nextY < boardSize &&
                    board[nextX][nextY] == -1) {
                count++;
            }
        }
        return count;
    }

    // Helper method to get all valid knight moves from a position
    public List<Point> getValidMoves(int x, int y, List<Point> existingMoves) {
        List<Point> validMoves = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            int nextX = x + X_MOVES[i];
            int nextY = y + Y_MOVES[i];

            if (nextX >= 0 && nextX < boardSize &&
                    nextY >= 0 && nextY < boardSize) {

                boolean alreadyVisited = false;
                for (Point p : existingMoves) {
                    if (p.x == nextX && p.y == nextY) {
                        alreadyVisited = true;
                        break;
                    }
                }

                if (!alreadyVisited) {
                    validMoves.add(new Point(nextX, nextY));
                }
            }
        }

        return validMoves;
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
    }
}
