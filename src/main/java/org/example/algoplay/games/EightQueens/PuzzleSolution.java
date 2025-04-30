package org.example.algoplay.games.EightQueens;

public class PuzzleSolution {
    private String positions; // This will store the 1D array as a string
    private long timeTaken;

    // Constructor that accepts a string for positions and a long for time taken
    public PuzzleSolution(String positions, long timeTaken) {
        this.positions = positions;
        this.timeTaken = timeTaken;
    }

    public String getPositions() {
        return positions; // Returns the string representation of the 1D array
    }

    public long getTimeTaken() {
        return timeTaken;
    }
}