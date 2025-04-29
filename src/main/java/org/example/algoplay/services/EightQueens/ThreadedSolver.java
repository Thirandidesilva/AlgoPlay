package org.example.algoplay.services.EightQueens;


import org.example.algoplay.games.EightQueens.PuzzleSolution;

import java.util.ArrayList;
import java.util.List;

public class ThreadedSolver {

    private List<PuzzleSolution> solutions = new ArrayList<>();

    public void solve() {
        // Split work into threads
        // Each thread works on a portion of the solution space
        // Store solutions in solutions list
        solutions.add(new PuzzleSolution("Solution1", 0)); // Use 0 or appropriate long value for time taken

        solutions.add(new PuzzleSolution("Solution2", 0)); // Use 0 or appropriate long value for time taken
        // Call DatabaseController to save solutions
    }

    public List<PuzzleSolution> getSolutions() {
        return solutions;
    }
}
