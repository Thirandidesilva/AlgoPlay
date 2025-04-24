package org.example.algoplay.games.tsp;

import java.util.*;

public class RecursiveDivideAndConquerSolver implements TspSolver {

    private int[][] distances;
    private List<Integer> cities;

    @Override
    public void initialize(int[][] distanceMatrix, List<Integer> cities) {
        this.distances = distanceMatrix;
        this.cities = cities;
    }

    @Override
    public List<Integer> solve() {
        return solveRecursive(cities);
    }

    private List<Integer> solveRecursive(List<Integer> subCities) {
        if (subCities.size() <= 4) {
            // Base case: small TSP solved with SantSolver
            SantSolver sant = new SantSolver();
            sant.setIterations(50); // less iterations for small problem
            sant.initialize(distances, subCities);
            return sant.solve();
        }

        // Divide
        int mid = subCities.size() / 2;
        List<Integer> left = subCities.subList(0, mid);
        List<Integer> right = subCities.subList(mid, subCities.size());

        // Conquer
        List<Integer> leftTour = solveRecursive(new ArrayList<>(left));
        List<Integer> rightTour = solveRecursive(new ArrayList<>(right));

        // Combine using greedy merge
        return mergeTours(leftTour, rightTour);
    }

    private List<Integer> mergeTours(List<Integer> tourA, List<Integer> tourB) {
        // Find best connection between ends of A and B
        int bestCost = Integer.MAX_VALUE;
        List<Integer> bestTour = null;

        for (int i = 0; i < tourA.size(); i++) {
            for (int j = 0; j < tourB.size(); j++) {
                List<Integer> merged = new ArrayList<>();
                merged.addAll(rotate(tourA, i));
                merged.addAll(rotate(tourB, j));

                int cost = calculateTourLength(merged);
                if (cost < bestCost) {
                    bestCost = cost;
                    bestTour = merged;
                }
            }
        }

        return bestTour;
    }

    private List<Integer> rotate(List<Integer> tour, int startIndex) {
        List<Integer> rotated = new ArrayList<>();
        for (int i = 0; i < tour.size(); i++) {
            rotated.add(tour.get((startIndex + i) % tour.size()));
        }
        return rotated;
    }

    private int calculateTourLength(List<Integer> tour) {
        int total = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            total += distances[tour.get(i)][tour.get(i + 1)];
        }
        total += distances[tour.get(tour.size() - 1)][tour.get(0)];
        return total;
    }

    @Override
    public String getName() {
        return "Recursive Divide and Conquer";
    }
}
