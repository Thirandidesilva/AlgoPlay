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
                return getRandomMove(board);
            case MEDIUM:
                return getMediumMove(board);
            case HARD:
            default:
                return getHardMove(board);
        }
    }

    private int[] getRandomMove(TicTacToeBoard board) {
        List<int[]> availableMoves = getAvailableMoves(board);
        if (!availableMoves.isEmpty()) {
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }
        return null; // No moves available
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

        // 50% chance to choose a winning move
        if (!winningMoves.isEmpty() && random.nextBoolean()) {
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

        // If no winning/blocking move, pick a random available move
        return getRandomMove(board);
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