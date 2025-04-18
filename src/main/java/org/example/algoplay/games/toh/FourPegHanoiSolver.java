package org.example.algoplay.games.toh;

import java.util.ArrayList;
import java.util.List;

public class FourPegHanoiSolver {

    public List<String> solve(int n) {
        List<String> moves = new ArrayList<>();
        solveFourPeg(n, 0, 3, 1, 2, moves);
        return moves;
    }

    private void solveFourPeg(int n, int source, int destination, int aux1, int aux2,
                              List<String> moves) {
        if (n == 0) {
            return;
        } else if (n == 1) {
            moves.add("Move disk 1 from peg " + (char)('A' + source) +
                    " to peg " + (char)('A' + destination));
            return;
        }

        // Find optimal k according to Frame-Stewart algorithm
        int k = findOptimalK(n);

        // Move top n-k disks from source to aux1 using all 4 pegs
        solveFourPeg(n - k, source, aux1, aux2, destination, moves);

        // Move remaining k disks from source to destination using 3 pegs
        solveThreePeg(k, source, destination, aux2, moves);

        // Move n-k disks from aux1 to destination using all 4 pegs
        solveFourPeg(n - k, aux1, destination, source, aux2, moves);
    }

    // Standard 3-peg Tower of Hanoi solution
    private void solveThreePeg(int n, int source, int destination, int auxiliary,
                               List<String> moves) {
        if (n == 1) {
            moves.add("Move disk " + n + " from peg " + (char)('A' + source) +
                    " to peg " + (char)('A' + destination));
            return;
        }

        solveThreePeg(n - 1, source, auxiliary, destination, moves);
        moves.add("Move disk " + n + " from peg " + (char)('A' + source) +
                " to peg " + (char)('A' + destination));
        solveThreePeg(n - 1, auxiliary, destination, source, moves);
    }

    // Find optimal k for Frame-Stewart algorithm
    private int findOptimalK(int n) {
        // This is a heuristic approach; the exact value is complex to compute
        return (int) Math.sqrt(n);
    }

    // Calculate optimal move count for 4-peg problem
    public int getOptimalMoveCount(int n) {
        if (n <= 0) return 0;
        if (n == 1) return 1;

        // Use dynamic programming to compute the optimal move count
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = 1;

        for (int i = 2; i <= n; i++) {
            dp[i] = Integer.MAX_VALUE;
            for (int j = 1; j < i; j++) {
                // Try different values of k to find minimum
                int moves = 2 * dp[i - j] + (1 << j) - 1;
                dp[i] = Math.min(dp[i], moves);
            }
        }

        return dp[n];
    }
}