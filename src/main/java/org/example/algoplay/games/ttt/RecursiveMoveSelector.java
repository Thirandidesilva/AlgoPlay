package org.example.algoplay.games.ttt;

import org.example.algoplay.models.Difficulty;
import org.example.algoplay.models.TicTacToeBoard;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RecursiveMoveSelector {
    private final Random random = new Random();
    private Difficulty currentDifficulty = Difficulty.HARD; // Default to HARD

    public void setDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }

    public int[] findMove(TicTacToeBoard board) {
        // If Easy difficulty, use a method that helps player win
        if (currentDifficulty == Difficulty.EASY) {
            return findEasyMove(board);
        }

        int size = board.getSize();
        List<int[]> winningMoves = new ArrayList<>();

        // First priority: Check if AI can win immediately
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j)) {
                    board.placeMove(i, j, 'O');
                    boolean aiWins = board.checkWin('O');
                    board.removeMove(i, j);

                    if (aiWins) {
                        winningMoves.add(new int[]{i, j});
                    }
                }
            }
        }

        if (!winningMoves.isEmpty()) {
            return winningMoves.get(random.nextInt(winningMoves.size()));
        }

        // Second priority: Block player from winning
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j)) {
                    board.placeMove(i, j, 'X'); // Simulate player move
                    boolean playerWins = board.checkWin('X');
                    board.removeMove(i, j);

                    if (playerWins) {
                        return new int[]{i, j}; // Block player
                    }
                }
            }
        }

        // Third priority: Find a safe move with strategic positioning
        List<int[]> strategicMoves = new ArrayList<>();
        int center = size / 2;

        // Prefer center position
        if (board.isCellEmpty(center, center)) {
            return new int[]{center, center};
        }

        // Prefer corners
        if (board.isCellEmpty(0, 0)) strategicMoves.add(new int[]{0, 0});
        if (board.isCellEmpty(0, size-1)) strategicMoves.add(new int[]{0, size-1});
        if (board.isCellEmpty(size-1, 0)) strategicMoves.add(new int[]{size-1, 0});
        if (board.isCellEmpty(size-1, size-1)) strategicMoves.add(new int[]{size-1, size-1});

        if (!strategicMoves.isEmpty()) {
            return strategicMoves.get(random.nextInt(strategicMoves.size()));
        }

        // Look for any safe move
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j) && isSafe(board, i, j)) {
                    return new int[]{i, j};
                }
            }
        }

        // If no safe move found, choose any available move
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j)) {
                    return new int[]{i, j};
                }
            }
        }

        return null; // No valid move found
    }

    private int[] findEasyMove(TicTacToeBoard board) {
        int size = board.getSize();
        List<int[]> availableMoves = getAvailableMoves(board);

        if (availableMoves.isEmpty()) {
            return null;
        }

        // First check if there's a move that would help player win
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!board.isCellEmpty(i, j)) continue;

                // Try this AI move
                board.placeMove(i, j, 'O');

                // Now check if any player move could win after this
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        if (!board.isCellEmpty(x, y)) continue;

                        board.placeMove(x, y, 'X');
                        boolean playerCanWin = board.checkWin('X');
                        board.removeMove(x, y);

                        if (playerCanWin) {
                            // Found a helpful move for player
                            board.removeMove(i, j);
                            return new int[]{i, j};
                        }
                    }
                }

                board.removeMove(i, j);
            }
        }

        // Find worst possible move (move that doesn't block player's progress)
        List<int[]> nonBlockingMoves = new ArrayList<>();
        for (int[] move : availableMoves) {
            boolean blocks = false;

            // Check if this move would block player's line
            int i = move[0], j = move[1];

            // Check horizontal
            int countX = 0;
            for (int y = 0; y < size; y++) {
                if (board.getBoard()[i][y] == 'X') countX++;
            }
            if (countX >= 2) blocks = true;

            // Check vertical
            countX = 0;
            for (int x = 0; x < size; x++) {
                if (board.getBoard()[x][j] == 'X') countX++;
            }
            if (countX >= 2) blocks = true;

            if (!blocks) {
                nonBlockingMoves.add(move);
            }
        }

        if (!nonBlockingMoves.isEmpty()) {
            return nonBlockingMoves.get(random.nextInt(nonBlockingMoves.size()));
        }

        // If all moves block, just pick random
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    private boolean isSafe(TicTacToeBoard board, int row, int col) {
        int size = board.getSize();

        // Check neighboring cells
        int[] dr = {-1, 0, 1, 0}, dc = {0, -1, 0, 1};
        for (int d = 0; d < 4; d++) {
            int r = row + dr[d], c = col + dc[d];
            if (r >= 0 && r < size && c >= 0 && c < size) {
                if (board.getBoard()[r][c] == 'X') return true; // Check for adjacent 'X'
            }
        }
        return false; // No adjacent 'X' found, move is safe
    }

    private List<int[]> getAvailableMoves(TicTacToeBoard board) {
        List<int[]> availableMoves = new ArrayList<>();
        int size = board.getSize();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j)) {
                    availableMoves.add(new int[]{i, j});
                }
            }
        }

        return availableMoves;
    }
}