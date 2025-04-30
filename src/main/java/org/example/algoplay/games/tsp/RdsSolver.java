package org.example.algoplay.games.tsp;

import java.util.*;

public class RdsSolver implements TspSolver {
    private int[][] distances;
    private List<Integer> cities;
    private int totalCities;

    private int recursionCounter = 0;



    private static final int DREAMS_PER_STEP = 5; // You can adjust dream density
    private static final double EXPLORE_PROBABILITY = 0.2; // 20% chance to pick random city for chaos

    @Override
    public void initialize(int[][] distances, List<Integer> cities) {
        this.distances = distances;
        this.cities = cities;
        this.totalCities = cities.size();

    }

    @Override
    public List<Integer> solve() {
        return dreamSearch(new ArrayList<>(), new ArrayList<>(cities));
    }

    @Override
    public String getName() {
        return "RDS";
    }

    private static final int MAX_DEPTH = 9;       // Depth limit to avoid stack explosion
    private static final int MAX_DREAMS = 5;      // Only explore top 5 dreams per level

    protected List<Integer> dreamSearch(List<Integer> currentPath, List<Integer> remaining) {
        // Stop if we've explored all cities or hit max depth
        if (remaining.isEmpty() || currentPath.size() >= MAX_DEPTH) {
            return new ArrayList<>(currentPath);
        }

        recursionCounter++;
        if (recursionCounter % 50 == 0) {
            double progress = (double) currentPath.size() / totalCities;
            org.example.algoplay.controllers.games.tsp.PvMPageController.updateDreamProgress(progress);
        }

        List<List<Integer>> dreams = generateLucidDreams(currentPath, remaining);

        // ✅ Limit the number of dreams explored (prune excess)
        dreams = dreams.subList(0, Math.min(dreams.size(), MAX_DREAMS));

        List<Integer> bestDream = null;
        int bestLen = Integer.MAX_VALUE;

        for (List<Integer> dream : dreams) {
            List<Integer> newRemaining = new ArrayList<>(remaining);
            newRemaining.removeAll(dream.subList(currentPath.size(), dream.size()));

            List<Integer> result = dreamSearch(dream, newRemaining);

            int len = calcLen(result);
            if (len < bestLen) {
                bestLen = len;
                bestDream = result;
            }
        }

        return bestDream;
    }



    private List<List<Integer>> generateLucidDreams(List<Integer> path, List<Integer> remaining) {
        List<List<Integer>> dreams = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < DREAMS_PER_STEP; i++) {
            List<Integer> newDream = new ArrayList<>(path);

            if (!remaining.isEmpty()) {
                Integer nextCity;

                if (newDream.isEmpty()) {
                    // Pick random starting city if no path yet
                    nextCity = remaining.get(rand.nextInt(remaining.size()));
                } else {
                    // Pick next city based on proximity (lucid dreaming)
                    int lastCity = newDream.get(newDream.size() - 1);

                    if (rand.nextDouble() < EXPLORE_PROBABILITY) {
                        // 20% chaos: pick random city
                        nextCity = remaining.get(rand.nextInt(remaining.size()));
                    } else {
                        // 80% lucid: pick closer cities with higher chance
                        nextCity = pickCloseCity(lastCity, remaining, rand);
                    }
                }

                newDream.add(nextCity);
            }

            dreams.add(newDream);
        }

        return dreams;
    }

    private int pickCloseCity(int fromCity, List<Integer> candidates, Random rand) {
        // Invert distances so closer cities have higher weight
        List<Double> weights = new ArrayList<>();
        double total = 0.0;

        for (int toCity : candidates) {
            double weight = 1.0 / (distances[fromCity][toCity] + 1); // +1 to avoid div by zero
            weights.add(weight);
            total += weight;
        }

        // Roulette wheel selection
        double r = rand.nextDouble() * total;
        double cumulative = 0.0;
        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights.get(i);
            if (r <= cumulative) {
                return candidates.get(i);
            }
        }

        // Fallback
        return candidates.get(rand.nextInt(candidates.size()));
    }

    private int calcLen(List<Integer> path) {
        int len = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            len += distances[path.get(i)][path.get(i + 1)];
        }
        len += distances[path.get(path.size() - 1)][path.get(0)]; // close loop
        return len;
    }
}
