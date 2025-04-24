package org.example.algoplay.games.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
public class SbeeSolver implements TspSolver {

    private int[][] distances;
    private List<Integer> cities;

    private int numEmployedBees = 10;
    private int numOnlookerBees = 10;
    private int numScoutBees = 2;
    private int maxIterations = 100;
    private int scoutLimit = 5; // How many iterations before a solution is abandoned

    private List<List<Integer>> initialPopulation = null;
    private Random random = new Random();

    // Track how long each solution has gone without improvement
    private int[] solutionTrials;

    @Override
    public void initialize(int[][] distanceMatrix, List<Integer> cities) {
        this.distances = distanceMatrix;
        this.cities = cities;
    }

    @Override
    public List<Integer> solve() {
        List<List<Integer>> beePopulation = new ArrayList<>();
        List<Integer> lengths = new ArrayList<>();
        solutionTrials = new int[numEmployedBees];

        // Step 1: Generate initial population
        for (int i = 0; i < numEmployedBees; i++) {
            List<Integer> path;
            if (initialPopulation != null && i < initialPopulation.size()) {
                path = new ArrayList<>(initialPopulation.get(i));
            } else {
                path = generateRandomPath();
            }
            beePopulation.add(path);
            lengths.add(calculateTourLength(path));
        }

        List<Integer> bestPath = new ArrayList<>(beePopulation.get(0));
        int bestLength = calculateTourLength(bestPath);

        // Update best solution from initial population
        for (int i = 0; i < beePopulation.size(); i++) {
            if (lengths.get(i) < bestLength) {
                bestLength = lengths.get(i);
                bestPath = new ArrayList<>(beePopulation.get(i));
            }
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            // Step 2: Employed bees explore neighbors
            for (int i = 0; i < numEmployedBees; i++) {
                List<Integer> neighbor = generateNeighborPath(beePopulation.get(i));
                int neighborLength = calculateTourLength(neighbor);

                if (neighborLength < lengths.get(i)) {
                    beePopulation.set(i, neighbor);
                    lengths.set(i, neighborLength);
                    solutionTrials[i] = 0; // Reset trial counter

                    // Update best solution if needed
                    if (neighborLength < bestLength) {
                        bestLength = neighborLength;
                        bestPath = new ArrayList<>(neighbor);
                    }
                } else {
                    solutionTrials[i]++; // Increment trial counter
                }
            }

            // Step 3: Onlooker bees probabilistically pick good paths
            double[] probabilities = calculateSelectionProbabilities(lengths);

            for (int i = 0; i < numOnlookerBees; i++) {
                int selectedIndex = selectPathByProbability(probabilities);
                List<Integer> currentPath = beePopulation.get(selectedIndex);
                List<Integer> neighbor = generateEnhancedNeighborPath(currentPath);
                int neighborLength = calculateTourLength(neighbor);

                if (neighborLength < lengths.get(selectedIndex)) {
                    beePopulation.set(selectedIndex, neighbor);
                    lengths.set(selectedIndex, neighborLength);
                    solutionTrials[selectedIndex] = 0;

                    // Update best solution if needed
                    if (neighborLength < bestLength) {
                        bestLength = neighborLength;
                        bestPath = new ArrayList<>(neighbor);
                    }
                }
            }

            // Step 4: Scout bees replace abandoned solutions
            for (int i = 0; i < numEmployedBees; i++) {
                if (solutionTrials[i] >= scoutLimit) {
                    beePopulation.set(i, generateRandomPath());
                    lengths.set(i, calculateTourLength(beePopulation.get(i)));
                    solutionTrials[i] = 0;
                }
            }

            // Additional scout bees explore new random solutions
            for (int i = 0; i < numScoutBees; i++) {
                List<Integer> newPath = generateRandomPath();
                int newLength = calculateTourLength(newPath);

                if (newLength < bestLength) {
                    bestLength = newLength;
                    bestPath = new ArrayList<>(newPath);
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
        List<Integer> path = new ArrayList<>(cities.size());
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

    // Enhanced neighborhood search using 2-opt
    private List<Integer> generateEnhancedNeighborPath(List<Integer> base) {
        List<Integer> newPath = new ArrayList<>(base);

        // Choose random segment to reverse
        int i = random.nextInt(newPath.size());
        int j = random.nextInt(newPath.size());

        if (i > j) {
            int temp = i;
            i = j;
            j = temp;
        }

        // Reverse the segment between i and j
        while (i < j) {
            Collections.swap(newPath, i, j);
            i++;
            j--;
        }

        return newPath;
    }

    private int calculateTourLength(List<Integer> tour) {
        int length = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            length += distances[tour.get(i)][tour.get(i + 1)];
        }
        length += distances[tour.get(tour.size() - 1)][tour.get(0)]; // Return to start
        return length;
    }

    private double[] calculateSelectionProbabilities(List<Integer> lengths) {
        // Find maximum length to calculate fitness
        int maxLength = Collections.max(lengths);

        double[] fitness = new double[lengths.size()];
        double totalFitness = 0.0;

        for (int i = 0; i < lengths.size(); i++) {
            // Better fitness calculation - higher values for shorter paths
            fitness[i] = (maxLength + 1.0) - lengths.get(i);
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

    public void setIterations(int iter) {
        this.maxIterations = iter;
    }

    public void setInitialPopulation(List<List<Integer>> initPop) {
        this.initialPopulation = initPop;
    }

    public void setEmployedBees(int count) {
        this.numEmployedBees = count;
    }

    public void setOnlookerBees(int count) {
        this.numOnlookerBees = count;
    }

    public void setScoutBees(int count) {
        this.numScoutBees = count;
    }

    public void setScoutLimit(int limit) {
        this.scoutLimit = limit;
    }
}