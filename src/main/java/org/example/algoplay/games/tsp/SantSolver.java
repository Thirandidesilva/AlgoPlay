package org.example.algoplay.games.tsp;

import java.util.*;

public class SantSolver implements TspSolver {

    private int[][] distances;
    private List<Integer> cities;

    private int numAnts = 10;
    private int maxIterations = 100;
    private double evaporationRate = 0.5;

    private double alpha = 1.0; // Importance of pheromone
    private double beta = 2.0;  // Importance of distance

    private double[][] pheromones;
    private Random random = new Random();

    @Override
    public void initialize(int[][] distanceMatrix, List<Integer> cities) {
        this.distances = distanceMatrix;
        this.cities = cities;
        this.pheromones = new double[cities.size()][cities.size()];

        // Initialize pheromones to 1
        for (int i = 0; i < cities.size(); i++) {
            for (int j = 0; j < cities.size(); j++) {
                pheromones[i][j] = 1.0;
            }
        }
    }

    @Override
    public List<Integer> solve() {
        List<Integer> bestTour = null;
        int bestTourLength = Integer.MAX_VALUE;

        for (int iter = 0; iter < maxIterations; iter++) {
            List<List<Integer>> allAntTours = new ArrayList<>();
            List<Integer> allAntLengths = new ArrayList<>();

            for (int k = 0; k < numAnts; k++) {
                List<Integer> tour = generateAntTour();
                int length = calculateTourLength(tour);
                allAntTours.add(tour);
                allAntLengths.add(length);

                if (length < bestTourLength) {
                    bestTourLength = length;
                    bestTour = tour;
                }
            }

            evaporatePheromones();

            for (int i = 0; i < allAntTours.size(); i++) {
                depositPheromones(allAntTours.get(i), allAntLengths.get(i));
            }
        }

        return bestTour;
    }

    private List<Integer> generateAntTour() {
        List<Integer> tour = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        int current = random.nextInt(cities.size());
        tour.add(current);
        visited.add(current);

        while (tour.size() < cities.size()) {
            int next = selectNextCity(current, visited);
            tour.add(next);
            visited.add(next);
            current = next;
        }

        return tour;
    }

    private int selectNextCity(int current, Set<Integer> visited) {
        double[] probabilities = new double[cities.size()];
        double sum = 0.0;

        for (int i = 0; i < cities.size(); i++) {
            if (!visited.contains(i)) {
                double pheromone = pheromones[current][i];
                double distance = distances[current][i];
                probabilities[i] = Math.pow(pheromone, alpha) * Math.pow(1.0 / distance, beta);
                sum += probabilities[i];
            }
        }

        // Roulette wheel selection
        double r = random.nextDouble() * sum;
        double total = 0.0;

        for (int i = 0; i < cities.size(); i++) {
            if (!visited.contains(i)) {
                total += probabilities[i];
                if (total >= r) {
                    return i;
                }
            }
        }

        // Fallback
        for (int i = 0; i < cities.size(); i++) {
            if (!visited.contains(i)) return i;
        }

        return -1; // Should not happen
    }

    private int calculateTourLength(List<Integer> tour) {
        int length = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            length += distances[tour.get(i)][tour.get(i + 1)];
        }
        length += distances[tour.get(tour.size() - 1)][tour.get(0)];
        return length;
    }

    private void evaporatePheromones() {
        for (int i = 0; i < cities.size(); i++) {
            for (int j = 0; j < cities.size(); j++) {
                pheromones[i][j] *= (1 - evaporationRate);
            }
        }
    }

    private void depositPheromones(List<Integer> tour, int tourLength) {
        double deposit = 100.0 / tourLength;
        for (int i = 0; i < tour.size() - 1; i++) {
            int from = tour.get(i);
            int to = tour.get(i + 1);
            pheromones[from][to] += deposit;
            pheromones[to][from] += deposit;
        }

        // Close the loop
        int last = tour.get(tour.size() - 1);
        int first = tour.get(0);
        pheromones[last][first] += deposit;
        pheromones[first][last] += deposit;
    }

    public void setIterations(int iterations) {
        this.maxIterations = iterations;
    }

    public List<List<Integer>> solveAndReturnTopPaths(int numPaths) {
        List<List<Integer>> allAntTours = new ArrayList<>();
        List<Integer> allAntLengths = new ArrayList<>();

        List<Integer> bestTour = null;
        int bestTourLength = Integer.MAX_VALUE;

        for (int iter = 0; iter < maxIterations; iter++) {
            for (int k = 0; k < numAnts; k++) {
                List<Integer> tour = generateAntTour();
                int length = calculateTourLength(tour);
                allAntTours.add(tour);
                allAntLengths.add(length);

                if (length < bestTourLength) {
                    bestTourLength = length;
                    bestTour = tour;
                }
            }

            evaporatePheromones();

            for (int i = 0; i < allAntTours.size(); i++) {
                depositPheromones(allAntTours.get(i), allAntLengths.get(i));
            }
        }

        // Sort tours by length
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < allAntTours.size(); i++) indices.add(i);
        indices.sort(Comparator.comparingInt(allAntLengths::get));

        List<List<Integer>> topPaths = new ArrayList<>();
        for (int i = 0; i < Math.min(numPaths, indices.size()); i++) {
            topPaths.add(allAntTours.get(indices.get(i)));
        }

        return topPaths;
    }

    @Override
    public String getName() {
        return "Sant Colony Optimization";
    }
}
