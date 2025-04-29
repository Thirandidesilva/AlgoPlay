package org.example.algoplay.models;

public class TicTacToeBoard {
    private final char[][] board;
    private final int size;

    public TicTacToeBoard(int size) {
        this.size = size;
        this.board = new char[size][size];
    }

    public char[][] getBoard() {
        return board;
    }

    public int getSize() {
        return size;
    }

    public boolean isCellEmpty(int r, int c) {
        return board[r][c] == '\0';
    }

    public void placeMove(int r, int c, char player) {
        board[r][c] = player;
    }

    public void removeMove(int r, int c) {
        board[r][c] = '\0'; // Reset the cell to empty
    }

    public boolean checkWin(char player) {
        // Check rows and columns
        for (int i = 0; i < size; i++) {
            if (checkLine(player, board[i])) return true;

            char[] col = new char[size];
            for (int j = 0; j < size; j++) {
                col[j] = board[j][i];
            }
            if (checkLine(player, col)) return true;
        }

        // Check diagonals
        return checkDiagonals(player);
    }

    private boolean checkLine(char player, char[] line) {
        int count = 0;
        for (char cell : line) {
            count = (cell == player) ? count + 1 : 0;
            if (count == 5) return true; // Win condition is 5 in a row
        }
        return false;
    }

    private boolean checkDiagonals(char player) {
        // Check all diagonals (top-left to bottom-right)
        for (int i = 0; i <= size - 5; i++) {
            for (int j = 0; j <= size - 5; j++) {
                int count = 0;
                for (int k = 0; k < 5; k++) {
                    if (board[i + k][j + k] == player) {
                        count++;
                        if (count == 5) return true;
                    } else {
                        break;
                    }
                }
            }
        }

        // Check all diagonals (top-right to bottom-left)
        for (int i = 0; i <= size - 5; i++) {
            for (int j = 4; j < size; j++) {
                int count = 0;
                for (int k = 0; k < 5; k++) {
                    if (board[i + k][j - k] == player) {
                        count++;
                        if (count == 5) return true;
                    } else {
                        break;
                    }
                }
            }
        }

        return false;
    }

    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (isCellEmpty(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void clearBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = '\0'; // Reset all cells to empty
            }
        }
    }
}