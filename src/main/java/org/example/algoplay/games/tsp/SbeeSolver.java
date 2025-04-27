package org.example.algoplay.games.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SbeeSolver implements TspSolver {

    private int[][] distances;
    private List<Integer> cities;

    private int numBees = 10;
    private int numOnlookerBees = 10;
    private int maxIterations = 100;

    private Random random = new Random();

    @Override
    public void initialize(int[][] distanceMatrix, List<Integer> cities) {
        this.distances = distanceMatrix;
        this.cities = cities;
    }

    @Override
    public List<Integer> solve() {
        List<List<Integer>> beePopulation = new ArrayList<>();
        List<Integer> lengths = new ArrayList<>();

        // Step 1: Generate initial population
        for (int i = 0; i < numBees; i++) {
            List<Integer> path;
            if (initialPopulation != null && i < initialPopulation.size()) {
                path = new ArrayList<>(initialPopulation.get(i));
            } else {
                path = generateRandomPath();
            }
            beePopulation.add(path);
            lengths.add(calculateTourLength(path));
        }


        List<Integer> bestPath = beePopulation.get(0);
        int bestLength = calculateTourLength(bestPath);

        for (int iter = 0; iter < maxIterations; iter++) {
            // Step 2: Employed bees explore neighbors
            for (int i = 0; i < numBees; i++) {
                List<Integer> neighbor = generateNeighborPath(beePopulation.get(i));
                int neighborLength = calculateTourLength(neighbor);
                if (neighborLength < lengths.get(i)) {
                    beePopulation.set(i, neighbor);
                    lengths.set(i, neighborLength);
                }
            }

            // Step 3: Onlooker bees probabilistically pick good paths
            List<List<Integer>> onlookerPaths = new ArrayList<>();
            double[] probabilities = calculateSelectionProbabilities(lengths);

            for (int i = 0; i < numOnlookerBees; i++) {
                int selectedIndex = selectPathByProbability(probabilities);
                List<Integer> neighbor = generateNeighborPath(beePopulation.get(selectedIndex));
                onlookerPaths.add(neighbor);
            }

            // Step 4: Evaluate onlooker paths
            for (List<Integer> path : onlookerPaths) {
                int len = calculateTourLength(path);
                if (len < bestLength) {
                    bestLength = len;
                    bestPath = path;
                }
            }
        }

        return bestPath;
    }

    @Override
    public String getName() {
        return "Bee Colony Optimization";
    }

    private List<Integer> generateRandomPath() {
        List<Integer> path = new ArrayList<>();
        for (int i = 0; i < cities.size(); i++) {
            path.add(i);
        }
        Collections.shuffle(path, random);
        return path;
    }

    private List<Integer> generateNeighborPath(List<Integer> base) {
        List<Integer> newPath = new ArrayList<>(base);
        int i = random.nextInt(newPath.size());
        int j = random.nextInt(newPath.size());
        Collections.swap(newPath, i, j);
        return newPath;
    }

    private int calculateTourLength(List<Integer> tour) {
        int length = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            length += distances[tour.get(i)][tour.get(i + 1)];
        }
        length += distances[tour.get(tour.size() - 1)][tour.get(0)];
        return length;
    }

    private double[] calculateSelectionProbabilities(List<Integer> lengths) {
        double[] fitness = new double[lengths.size()];
        double totalFitness = 0.0;

        for (int i = 0; i < lengths.size(); i++) {
            fitness[i] = 1.0 / (lengths.get(i) + 1e-6); // Avoid division by zero
            totalFitness += fitness[i];
        }

        double[] probabilities = new double[fitness.length];
        for (int i = 0; i < fitness.length; i++) {
            probabilities[i] = fitness[i] / totalFitness;
        }
        return probabilities;
    }

    private int selectPathByProbability(double[] probabilities) {
        double r = random.nextDouble();
        double cumulative = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (r <= cumulative) return i;
        }
        return probabilities.length - 1; // Fallback
    }
    private List<List<Integer>> initialPopulation = null;

    public void setIterations(int iter) {
        this.maxIterations = iter;
    }

    public void setInitialPopulation(List<List<Integer>> initPop) {
        this.initialPopulation = initPop;
    }

}
