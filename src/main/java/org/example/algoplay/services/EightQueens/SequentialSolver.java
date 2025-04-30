package org.example.algoplay.services.EightQueens;


import org.example.algoplay.games.EightQueens.PuzzleSolution;

import java.util.ArrayList;
import java.util.List;

public class SequentialSolver {

    private List<PuzzleSolution> solutions = new ArrayList<>();

    public void solve() {
        // Sequential solving logic
        // Generate solutions for the 8 queens puzzle
        // Store the solutions in the solutions list
        solutions.add(new PuzzleSolution("Solution1", 0));
        solutions.add(new PuzzleSolution("Solution2", 0));
        // Call DatabaseController to save solutions
    }

    public List<PuzzleSolution> getSolutions() {
        return solutions;
    }
}
