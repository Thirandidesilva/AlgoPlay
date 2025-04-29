package org.example.algoplay.games.tsp;

import java.util.List;

public class HybridSolver implements TspSolver {

    private int[][] distances;
    private List<Integer> cities;

    private SantSolver antSolver = new SantSolver();
    private SbeeSolver beeSolver = new SbeeSolver();

    private int antIterations = 50;
    private int beeIterations = 100;

    private int numElitePathsToPass = 5;

    @Override
    public void initialize(int[][] distanceMatrix, List<Integer> cities) {
        this.distances = distanceMatrix;
        this.cities = cities;

        // Initialize sub-solvers
        antSolver.initialize(distanceMatrix, cities);
        beeSolver.initialize(distanceMatrix, cities);
    }

    @Override
    public List<Integer> solve() {
        // Step 1: Run Ant Colony Optimization
        antSolver.setIterations(antIterations);
        List<List<Integer>> antSolutions = antSolver.solveAndReturnTopPaths(numElitePathsToPass);

        // Step 2: Pass best paths to Bee Colony as initial population
        beeSolver.setIterations(beeIterations);
        beeSolver.setInitialPopulation(antSolutions);

        // Step 3: Let Bee Colony refine and return the final best path
        return beeSolver.solve();
    }

    @Override
    public String getName() {
        return "Hybrid Ant + Bee Optimization";
    }

    // Optional setters if you want to customize iterations
    public void setAntIterations(int iter) {
        this.antIterations = iter;
    }

    public void setBeeIterations(int iter) {
        this.beeIterations = iter;
    }

    public void setNumElitePathsToPass(int num) {
        this.numElitePathsToPass = num;
    }
}
