package org.example.algoplay.games.toh;

import java.util.ArrayList;
import java.util.List;

public class RecursiveHanoiSolver {

    public List<String> solve(int n, int source, int destination, int auxiliary) {
        List<String> moves = new ArrayList<>();

        // Base case
        if (n == 1) {
            moves.add("Move disk 1 from peg " + (char)('A' + source) +
                    " to peg " + (char)('A' + destination));
            return moves;
        }

        // Move n-1 disks from source to auxiliary peg
        moves.addAll(solve(n-1, source, auxiliary, destination));

        // Move the nth disk from source to destination
        moves.add("Move disk " + n + " from peg " + (char)('A' + source) +
                " to peg " + (char)('A' + destination));

        // Move n-1 disks from auxiliary to destination
        moves.addAll(solve(n-1, auxiliary, destination, source));

        return moves;
    }
}