package org.example.algoplay.games.EightQueens;


import org.example.algoplay.database.DatabaseController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadedEightQueensSolver {

    public static long saveThreadedResults() {
        long start = System.nanoTime();
        List<int[]> solutions = solveNQueensThreaded();
        long end = System.nanoTime();

        long durationMs = (end - start) / 1_000_000; // Convert to milliseconds

        int insertedCount = 0;

        try {
            for (int[] sol : solutions) {
                // Format the solution as a 1D array string
                StringBuilder posStr = new StringBuilder("[");
                for (int i = 0; i < sol.length; i++) {
                    posStr.append(sol[i]); // Append the column index directly
                    if (i < sol.length - 1) posStr.append(","); // Add comma between elements
                }
                posStr.append("]");

                // Save to the new threaded_solutions table
                DatabaseController.saveThreadedSolution(posStr.toString(), durationMs);
                insertedCount++;
            }

            // Save the timing
            DatabaseController.saveTiming("threaded", durationMs);
            System.out.println("🕒 Total Time for Threaded: " + durationMs + " ms");

        } catch (Exception e) {
            System.out.println("Threaded error: " + e.getMessage());
        }

        return durationMs;
    }

    public static List<int[]> solveNQueensThreaded() {
        List<int[]> allSolutions = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<List<int[]>>> futures = new ArrayList<>();

        for (int col = 0; col < 8; col++) {
            int finalCol = col;
            futures.add(executor.submit(() -> {
                List<int[]> threadSolutions = new ArrayList<>();
                int[] board = new int[8];
                for (int i = 0; i < 8; i++) board[i] = -1;
                board[0] = finalCol;
                backtrack(1, board, threadSolutions);
                return threadSolutions;
            }));
        }

        for (Future<List<int[]>> future : futures) {
            try {
                allSolutions.addAll(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return allSolutions;
    }

    private static void backtrack(int row, int[] board, List<int[]> solutions) {
        if (row == 8) {
            solutions.add(board.clone());
            return;
        }
        for (int col = 0; col < 8; col++) {
            if (isSafe(board, row, col)) {
                board[row] = col;
                backtrack(row + 1, board, solutions);
            }
        }
    }

    private static boolean isSafe(int[] board, int row, int col) {
        for (int i = 0; i < row; i++) {
            if (board[i] == col || Math.abs(board[i] - col) == Math.abs(i - row)) {
                return false;
            }
        }
        return true;
    }
}