package org.example.algoplay.games.ttt;

import org.example.algoplay.models.TicTacToeBoard;

public class RecursiveMoveSelector {

    public int[] findMove(TicTacToeBoard board) {
        int size = board.getSize();

        // Look for a safe move
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isCellEmpty(i, j) && isSafe(board, i, j)) {
                    return new int[]{i, j};
                }
            }
        }
        return null; // No valid move found
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
}