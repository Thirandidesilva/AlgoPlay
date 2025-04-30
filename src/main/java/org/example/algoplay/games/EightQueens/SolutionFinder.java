package org.example.algoplay.games.EightQueens;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SolutionFinder {

    private final List<int[]> solutions = new ArrayList<>();

    // Sequential backtracking
    public List<int[]> findSolutionsSequential() {
        solutions.clear();
        int[] board = new int[8];
        for (int i = 0; i < 8; i++) board[i] = -1;
        solveSequential(board, 0);
        return new ArrayList<>(solutions);
    }

    private void solveSequential(int[] board, int col) {
        if (col == 8) {
            solutions.add(board.clone());
            return;
        }

        for (int row = 0; row < 8; row++) {
            if (isSafe(board, row, col)) {
                board[col] = row;
                solveSequential(board, col + 1);
                board[col] = -1;
            }
        }
    }

    // Threaded solver: One thread for each row in the first column
    public List<int[]> findSolutionsThreaded() {
        solutions.clear();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<List<int[]>>> futures = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            final int startRow = row;
            futures.add(executor.submit(() -> {
                List<int[]> threadSolutions = new ArrayList<>();
                int[] board = new int[8];
                for (int i = 0; i < 8; i++) board[i] = -1;
                board[0] = startRow;
                solveThreaded(board, 1, threadSolutions);
                return threadSolutions;
            }));
        }

        for (Future<List<int[]>> future : futures) {
            try {
                solutions.addAll(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return new ArrayList<>(solutions);
    }

    private void solveThreaded(int[] board, int col, List<int[]> threadSolutions) {
        if (col == 8) {
            threadSolutions.add(board.clone());
            return;
        }

        for (int row = 0; row < 8; row++) {
            if (isSafe(board, row, col)) {
                board[col] = row;
                solveThreaded(board, col + 1, threadSolutions);
                board[col] = -1;
            }
        }
    }

    private boolean isSafe(int[] board, int row, int col) {
        for (int c = 0; c < col; c++) {
            int r = board[c];
            if (r == row || Math.abs(r - row) == Math.abs(c - col)) {
                return false;
            }
        }
        return true;
    }
}
