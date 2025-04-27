package org.example.algoplay.games.tsp;

import java.time.LocalDateTime;
import java.util.*;

/**
 * This class tracks performance metrics for TSP solutions and provides methods to analyze results.
 */
public class TspPerformanceTracker {

    // Class to hold performance metrics for a single solution
    public static class PerformanceData {
        private final String solutionName;
        private final int pathLength;
        private final long executionTimeMs;
        private final List<Integer> path;
        private final int cityCount;
        private final LocalDateTime timestamp;

        public PerformanceData(String solutionName, int pathLength, long executionTimeMs,
                               List<Integer> path, int cityCount) {
            this.solutionName = solutionName;
            this.pathLength = pathLength;
            this.executionTimeMs = executionTimeMs;
            this.path = new ArrayList<>(path);
            this.cityCount = cityCount;
            this.timestamp = LocalDateTime.now();
        }

        // Getters
        public String getSolutionName() { return solutionName; }
        public int getPathLength() { return pathLength; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public List<Integer> getPath() { return new ArrayList<>(path); }
        public int getCityCount() { return cityCount; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("%s: length=%d, time=%dms, cities=%d, path=%s",
                    solutionName, pathLength, executionTimeMs, cityCount,
                    path.size() > 10 ? path.subList(0, 3) + "..." + path.subList(path.size()-3, path.size()) : path);
        }
    }

    private final Map<String, PerformanceData> currentResults = new HashMap<>();
    private final List<Map<String, PerformanceData>> historicalResults = new ArrayList<>();
    private final TspDatabaseManager dbManager;  // Reference to the database manager

    public TspPerformanceTracker() {
        this.dbManager = new TspDatabaseManager();
        // Initialize database tables on startup
        this.dbManager.initializeDatabase();
    }

    /**
     * Get the database manager instance
     */
    public TspDatabaseManager getDatabaseManager() {
        return dbManager;
    }

    /**
     * Records performance data for a solution
     */
    public void recordPerformance(String solutionName, int pathLength, long executionTimeMs,
                                  List<Integer> path, int cityCount) {
        PerformanceData data = new PerformanceData(
                solutionName, pathLength, executionTimeMs, path, cityCount);
        currentResults.put(solutionName, data);
    }

    /**
     * Records all current results in the historical archive and clears current results
     */
    public void finalizeRound() {
        if (!currentResults.isEmpty()) {
            historicalResults.add(new HashMap<>(currentResults));
            currentResults.clear();
        }
    }

    /**
     * Saves current results to database and finalizes the round
     */
    public boolean saveToDbAndFinalize() {
        if (!currentResults.isEmpty()) {
            boolean saved = dbManager.savePerformanceData(currentResults);
            finalizeRound();
            return saved;
        }
        return false;
    }

    /**
     * Returns the solution name with the shortest path in current results
     */
    public String getWinnerByDistance() {
        String winner = null;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<String, PerformanceData> entry : currentResults.entrySet()) {
            if (entry.getValue().getPathLength() < minDistance) {
                minDistance = entry.getValue().getPathLength();
                winner = entry.getKey();
            }
        }

        return winner;
    }

    /**
     * Returns the solution name with the fastest execution time in current results
     */
    public String getWinnerByTime() {
        String winner = null;
        long minTime = Long.MAX_VALUE;

        for (Map.Entry<String, PerformanceData> entry : currentResults.entrySet()) {
            if (entry.getValue().getExecutionTimeMs() < minTime) {
                minTime = entry.getValue().getExecutionTimeMs();
                winner = entry.getKey();
            }
        }

        return winner;
    }

    /**
     * Calculate efficiency scores for all current solutions
     * Higher score is better (normalized between 0-100)
     */
    public Map<String, Double> calculateEfficiencyScores() {
        Map<String, Double> scores = new HashMap<>();

        // Find min and max values for normalization
        int minDistance = Integer.MAX_VALUE;
        int maxDistance = Integer.MIN_VALUE;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        for (PerformanceData data : currentResults.values()) {
            minDistance = Math.min(minDistance, data.getPathLength());
            maxDistance = Math.max(maxDistance, data.getPathLength());
            minTime = Math.min(minTime, data.getExecutionTimeMs());
            maxTime = Math.max(maxTime, data.getExecutionTimeMs());
        }

        // Calculate scores (higher is better)
        for (Map.Entry<String, PerformanceData> entry : currentResults.entrySet()) {
            String name = entry.getKey();
            PerformanceData data = entry.getValue();

            // Normalize distance (0-50 points, inversely proportional to distance)
            double distanceScore = 50.0;
            if (maxDistance > minDistance) {
                distanceScore = 50.0 * (1.0 - (data.getPathLength() - minDistance) /
                        (double)(maxDistance - minDistance));
            }

            // Normalize time (0-50 points, inversely proportional to time)
            double timeScore = 50.0;
            if (maxTime > minTime) {
                timeScore = 50.0 * (1.0 - (data.getExecutionTimeMs() - minTime) /
                        (double)(maxTime - minTime));
            }

            // Total score (0-100)
            scores.put(name, distanceScore + timeScore);
        }

        return scores;
    }

    /**
     * Get current results
     */
    public Map<String, PerformanceData> getCurrentResults() {
        return new HashMap<>(currentResults);
    }

    /**
     * Get historical results
     */
    public List<Map<String, PerformanceData>> getHistoricalResults() {
        List<Map<String, PerformanceData>> result = new ArrayList<>();
        for (Map<String, PerformanceData> round : historicalResults) {
            result.add(new HashMap<>(round));
        }
        return result;
    }

    /**
     * Get performance improvement or regression compared to last round
     * Returns a map of solution names to improvement percentages
     * Positive values mean improvement, negative values mean regression
     */
    public Map<String, Double> getImprovementStats() {
        if (historicalResults.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, PerformanceData> lastRound = historicalResults.get(historicalResults.size() - 1);
        Map<String, Double> improvements = new HashMap<>();

        for (String solution : currentResults.keySet()) {
            if (lastRound.containsKey(solution)) {
                PerformanceData current = currentResults.get(solution);
                PerformanceData previous = lastRound.get(solution);

                // Only compare if city counts are the same
                if (current.getCityCount() == previous.getCityCount()) {
                    // Calculate path length improvement
                    double lengthImprovement = (previous.getPathLength() - current.getPathLength()) /
                            (double) previous.getPathLength() * 100.0;

                    // Calculate time improvement
                    double timeImprovement = (previous.getExecutionTimeMs() - current.getExecutionTimeMs()) /
                            (double) previous.getExecutionTimeMs() * 100.0;

                    // Average of both improvements
                    improvements.put(solution, (lengthImprovement + timeImprovement) / 2.0);
                }
            }
        }

        return improvements;
    }
}