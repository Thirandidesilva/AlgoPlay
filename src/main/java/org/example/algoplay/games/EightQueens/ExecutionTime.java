package org.example.algoplay.games.EightQueens;

public class ExecutionTime {

    private int solutionId;
    private String solverType;
    private double executionTime;

    public ExecutionTime(int solutionId, String solverType, double executionTime) {
        this.solutionId = solutionId;
        this.solverType = solverType;
        this.executionTime = executionTime;
    }

    public int getSolutionId() {
        return solutionId;
    }

    public String getSolverType() {
        return solverType;
    }

    public double getExecutionTime() {
        return executionTime;
    }
}
