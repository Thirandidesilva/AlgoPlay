package org.example.algoplay.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarnsdorffKnightTour {
    private static final int[] X_MOVES = {2, 1, -1, -2, -2, -1, 1, 2};
    private static final int[] Y_MOVES = {1, 2, 2, 1, -1, -2, -2, -1};
    private int boardSize;
    private int[][] board;

    public WarnsdorffKnightTour(int boardSize) {
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

        int moveCount = 1;
        int x = startX;
        int y = startY;

        // Continue until all squares are visited
        while (moveCount < boardSize * boardSize) {
            // Get the next position using Warnsdorff's heuristic
            Point nextMove = getNextMove(x, y);

            // If no next move is available, the algorithm fails
            if (nextMove == null) {
                return false;
            }

            // Move to the next position
            x = nextMove.x;
            y = nextMove.y;
            board[x][y] = moveCount++;
        }

        return true;
    }

    public Point getNextMove(int x, int y) {
        List<Point> neighbors = new ArrayList<>();
        List<Integer> degrees = new ArrayList<>();

        // Find all valid moves and count their accessibility scores
        for (int i = 0; i < 8; i++) {
            int nextX = x + X_MOVES[i];
            int nextY = y + Y_MOVES[i];

            if (isValidMove(nextX, nextY)) {
                neighbors.add(new Point(nextX, nextY));
                degrees.add(countAccessibility(nextX, nextY));
            }
        }

        // If no valid moves, return null
        if (neighbors.isEmpty()) {
            return null;
        }

        // Find move with minimum accessibility (Warnsdorff's heuristic)
        int minDegree = Integer.MAX_VALUE;
        int minIndex = -1;

        for (int i = 0; i < neighbors.size(); i++) {
            if (degrees.get(i) < minDegree) {
                minDegree = degrees.get(i);
                minIndex = i;
            }
        }

        return neighbors.get(minIndex);
    }

    private int countAccessibility(int x, int y) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int nextX = x + X_MOVES[i];
            int nextY = y + Y_MOVES[i];

            if (isValidMove(nextX, nextY)) {
                count++;
            }
        }
        return count;
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
    public Point findCompleteTourMove(List<Point> existingMoves) {
        if (existingMoves.isEmpty()) {
            return null;
        }

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

        // Save original board and use temp board
        int[][] originalBoard = board;
        board = tempBoard;

        // Get current position (last move)
        Point current = existingMoves.get(existingMoves.size() - 1);

        // Get all valid moves from current position
        List<Point> validMoves = getValidMoves(current.x, current.y, existingMoves);

        if (validMoves.isEmpty()) {
            board = originalBoard;
            return null;
        }

        // Sort moves using Warnsdorff's heuristic (fewer onward moves first)
        validMoves.sort((p1, p2) -> {
            int degree1 = countAccessibilityWithExisting(p1.x, p1.y, existingMoves);
            int degree2 = countAccessibilityWithExisting(p2.x, p2.y, existingMoves);
            return Integer.compare(degree1, degree2);
        });

        // Try each move to verify if it leads to a complete tour
        for (Point move : validMoves) {
            // Add this move
            List<Point> newPath = new ArrayList<>(existingMoves);
            newPath.add(move);

            // Mark the move on the board
            tempBoard[move.x][move.y] = existingMoves.size();

            // Try to solve from this position
            if (solveFromPosition(move.x, move.y, existingMoves.size() + 1)) {
                board = originalBoard;
                return move;
            }

            // Reset for next attempt
            tempBoard[move.x][move.y] = -1;
        }

        // If no guaranteed path found, return the first move (best by Warnsdorff's heuristic)
        board = originalBoard;
        return validMoves.get(0);
    }

    // New method to solve from an existing position with existing moves already made
    private boolean solveFromPosition(int x, int y, int moveCount) {
        // If all squares are visited, tour is complete
        if (moveCount == boardSize * boardSize) {
            return true;
        }

        // Get next move using Warnsdorff's heuristic
        Point nextMove = getNextMove(x, y);
        if (nextMove == null) {
            return false;
        }

        // Make the move
        board[nextMove.x][nextMove.y] = moveCount;

        // Recursively continue
        if (solveFromPosition(nextMove.x, nextMove.y, moveCount + 1)) {
            return true;
        }

        // Backtrack
        board[nextMove.x][nextMove.y] = -1;
        return false;
    }

    private int countAccessibilityWithExisting(int x, int y, List<Point> existingMoves) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int nextX = x + X_MOVES[i];
            int nextY = y + Y_MOVES[i];

            if (nextX >= 0 && nextX < boardSize &&
                    nextY >= 0 && nextY < boardSize) {

                // Check if this position is already visited
                boolean visited = false;
                for (Point p : existingMoves) {
                    if (p.x == nextX && p.y == nextY) {
                        visited = true;
                        break;
                    }
                }

                if (!visited) {
                    count++;
                }
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
