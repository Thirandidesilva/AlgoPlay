package org.example.algoplay.games.EightQueens;

public class User {
    private String name;
    private int[] positions;
    private Integer solutionId; // Add this field to store the solution ID

    public User(String name, int[] positions) {
        this.name = name;
        this.positions = positions;
    }

    public String getName() {
        return name;
    }

    public int[] getPositions() {
        return positions;
    }

    public Integer getSolutionId() {
        return solutionId; // Getter for solutionId
    }

    public void setSolutionId(Integer solutionId) {
        this.solutionId = solutionId; // Setter for solutionId
    }
}
