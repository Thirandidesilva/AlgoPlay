package org.example.algoplay.games.ttt;

import org.example.algoplay.models.Difficulty; // Import proper Difficulty enum
import org.example.algoplay.models.TicTacToeBoard;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Updated HeuristicAI class that uses the proper Difficulty enum
public class HeuristicAI {
    private final Random random = new Random();
    private Difficulty difficulty = Difficulty.HARD; // Default to HARD

    public HeuristicAI() {
        // Default constructor
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int[] findMove(TicTacToeBoard board) {
        switch (difficulty) {
            case EASY:
                return getEasyMove(board);
            case MEDIUM:
                return getMediumMove(board);
            case HARD:
            default:
                return getHardMove(board);
        }
    }

    private int[] getEasyMove(TicTacToeBoard board) {
        int size = board.getSize();
        List<int[]> availableMoves = getAvailableMoves(board);

        if (availableMoves.isEmpty()) {
            return null; // No moves available
        }

        // First check if player can win in the next move with any of the available moves
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j)) {
                    // Check if this move would let player win on their next turn
                    board.placeMove(i, j, 'O'); // AI makes this move

                    // Now check all possible player moves after this
                    for (int x = 0; x < size; x++) {
                        for (int y = 0; y < size; y++) {
                            if (board.isCellEmpty(x, y)) {
                                board.placeMove(x, y, 'X'); // Try player move
                                boolean playerWouldWin = board.checkWin('X');
                                board.removeMove(x, y);

                                if (playerWouldWin) {
                                    // Found a move that will enable player to win next turn
                                    board.removeMove(i, j);
                                    return new int[]{i, j};
                                }
                            }
                        }
                    }

                    board.removeMove(i, j);
                }
            }
        }

        // If no move helps player win, avoid blocking player's potential line
        List<int[]> nonBlockingMoves = new ArrayList<>();
        for (int[] move : availableMoves) {
            int i = move[0], j = move[1];

            // Check if this move would block player's potential line
            boolean blocks = false;

            // Check horizontal line
            int playerCount = 0;
            for (int y = Math.max(0, j-4); y <= Math.min(size-1, j+4); y++) {
                if (y != j && y >= 0 && y < size && board.getBoard()[i][y] == 'X') {
                    playerCount++;
                    if (playerCount >= 2) {
                        blocks = true;
                        break;
                    }
                }
            }

            // Check vertical line
            if (!blocks) {
                playerCount = 0;
                for (int x = Math.max(0, i-4); x <= Math.min(size-1, i+4); x++) {
                    if (x != i && x >= 0 && x < size && board.getBoard()[x][j] == 'X') {
                        playerCount++;
                        if (playerCount >= 2) {
                            blocks = true;
                            break;
                        }
                    }
                }
            }

            if (!blocks) {
                nonBlockingMoves.add(move);
            }
        }

        // Choose a non-blocking move if available
        if (!nonBlockingMoves.isEmpty()) {
            return nonBlockingMoves.get(random.nextInt(nonBlockingMoves.size()));
        }

        // Otherwise, just pick any random move
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    private int[] getMediumMove(TicTacToeBoard board) {
        int size = board.getSize();

        // Try to block player's winning move
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j)) {
                    board.placeMove(i, j, 'X'); // Assume player move
                    boolean playerWins = board.checkWin('X');
                    board.removeMove(i, j);

                    if (playerWins) {
                        return new int[]{i, j}; // Block player
                    }
                }
            }
        }

        // If no immediate threat, choose random move
        return getRandomMove(board);
    }

    private int[] getHardMove(TicTacToeBoard board) {
        int size = board.getSize();
        List<int[]> winningMoves = new ArrayList<>();

        // Check if AI can win in the next move
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j)) {
                    board.placeMove(i, j, 'O'); // AI move
                    boolean aiWins = board.checkWin('O');
                    board.removeMove(i, j);

                    if (aiWins) {
                        winningMoves.add(new int[]{i, j});
                    }
                }
            }
        }

        // Always choose a winning move if available (no more 50% chance)
        if (!winningMoves.isEmpty()) {
            return winningMoves.get(random.nextInt(winningMoves.size()));
        }

        // Otherwise, block player if they are about to win
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j)) {
                    board.placeMove(i, j, 'X'); // Assume player move
                    boolean playerWins = board.checkWin('X');
                    board.removeMove(i, j);

                    if (playerWins) {
                        return new int[]{i, j}; // Block player
                    }
                }
            }
        }

        // If no winning/blocking move, try to make a strategic move
        List<int[]> strategicMoves = new ArrayList<>();

        // Prefer center and corners
        int center = size / 2;
        if (board.isCellEmpty(center, center)) {
            strategicMoves.add(new int[]{center, center});
        }

        if (board.isCellEmpty(0, 0)) strategicMoves.add(new int[]{0, 0});
        if (board.isCellEmpty(0, size-1)) strategicMoves.add(new int[]{0, size-1});
        if (board.isCellEmpty(size-1, 0)) strategicMoves.add(new int[]{size-1, 0});
        if (board.isCellEmpty(size-1, size-1)) strategicMoves.add(new int[]{size-1, size-1});

        if (!strategicMoves.isEmpty()) {
            return strategicMoves.get(random.nextInt(strategicMoves.size()));
        }

        // If no strategic moves available, pick a random available move
        return getRandomMove(board);
    }

    private int[] getRandomMove(TicTacToeBoard board) {
        List<int[]> availableMoves = getAvailableMoves(board);
        if (!availableMoves.isEmpty()) {
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }
        return null; // No moves available
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