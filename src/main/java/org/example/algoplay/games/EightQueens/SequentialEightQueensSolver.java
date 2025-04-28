package org.example.algoplay.games.EightQueens;


import  org.example.algoplay.database.DatabaseController;

import java.util.ArrayList;
import java.util.List;

public class SequentialEightQueensSolver {

    public static long saveSequentialResults() {
        long start = System.nanoTime();
        List<int[]> solutions = solveNQueensSequential();
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

                // Save to the new sequential_solutions table
                DatabaseController.saveSequentialSolution(posStr.toString(), durationMs);

                // Save to the general puzzle_solutions table as well
                long timeTaken = 0; // You can set this to the actual time taken if needed
                DatabaseController.saveSolution(new PuzzleSolution(posStr.toString(), timeTaken), timeTaken);
                insertedCount++;
            }

            // Save the timing
            DatabaseController.saveTiming("sequential", durationMs);

            System.out.println("🕒 Total Time for Sequential: " + durationMs + " ms");

        } catch (Exception e) {
            System.out.println("Sequential error: " + e.getMessage());
        }

        return durationMs;
    }

    public static List<int[]> solveNQueensSequential() {
        List<int[]> solutions = new ArrayList<>();
        int[] board = new int[8];
        for (int i = 0; i < 8; i++) board[i] = -1;
        backtrack(0, board, solutions);
        return solutions;
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
                board[row] = -1; // backtrack
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