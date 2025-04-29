package org.example.algoplay.games.tsp;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Tracks performance metrics for TSP solutions and provides analytics.
 */
public class TspPerformanceTracker {

    private final Map<String, PerformanceData> currentResults = new HashMap<>();
    private final List<Map<String, PerformanceData>> historicalResults = new ArrayList<>();
    private final TspDatabaseManager dbManager;

    public TspPerformanceTracker() {
        this.dbManager = new TspDatabaseManager();

    }

    public void recordPerformance(String solutionName, int pathLength, long executionTimeMs,
                                  List<Integer> path, int cityCount) {
        PerformanceData data = new PerformanceData(solutionName, pathLength, executionTimeMs, path, cityCount);
        currentResults.put(solutionName, data);
    }

    public boolean saveToDbAndFinalize() {
        if (!currentResults.isEmpty()) {
            boolean saved = dbManager.saveStructuredData(currentResults);
            finalizeRound();
            return saved;
        }
        return false;
    }

    public void finalizeRound() {
        if (!currentResults.isEmpty()) {
            historicalResults.add(new HashMap<>(currentResults));
            currentResults.clear();
        }
    }

    public TspDatabaseManager getDatabaseManager() {
        return dbManager;
    }

    public Map<String, PerformanceData> getCurrentResults() {
        return new HashMap<>(currentResults);
    }

    public Map<String, PerformanceData> getPerformanceData() {
        return getCurrentResults();
    }

    public List<Map<String, PerformanceData>> getHistoricalResults() {
        List<Map<String, PerformanceData>> result = new ArrayList<>();
        for (Map<String, PerformanceData> round : historicalResults) {
            result.add(new HashMap<>(round));
        }
        return result;
    }

    public String getWinnerByDistance() {
        return currentResults.entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().getPathLength()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String getWinnerByTime() {
        return currentResults.entrySet().stream()
                .min(Comparator.comparingLong(e -> e.getValue().getExecutionTimeMs()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public Map<String, Double> calculateEfficiencyScores() {
        Map<String, Double> scores = new HashMap<>();
        if (currentResults.isEmpty()) return scores;

        int minDistance = currentResults.values().stream().mapToInt(PerformanceData::getPathLength).min().orElse(1);
        int maxDistance = currentResults.values().stream().mapToInt(PerformanceData::getPathLength).max().orElse(1);
        long minTime = currentResults.values().stream().mapToLong(PerformanceData::getExecutionTimeMs).min().orElse(1);
        long maxTime = currentResults.values().stream().mapToLong(PerformanceData::getExecutionTimeMs).max().orElse(1);

        for (Map.Entry<String, PerformanceData> entry : currentResults.entrySet()) {
            PerformanceData data = entry.getValue();

            double distanceScore = 50.0;
            if (maxDistance > minDistance) {
                distanceScore *= 1.0 - (double) (data.getPathLength() - minDistance) / (maxDistance - minDistance);
            }

            double timeScore = 50.0;
            if (maxTime > minTime) {
                timeScore *= 1.0 - (double) (data.getExecutionTimeMs() - minTime) / (maxTime - minTime);
            }

            scores.put(entry.getKey(), distanceScore + timeScore);
        }

        return scores;
    }

    public Map<String, Double> getImprovementStats() {
        if (historicalResults.isEmpty()) return Collections.emptyMap();

        Map<String, PerformanceData> lastRound = historicalResults.get(historicalResults.size() - 1);
        Map<String, Double> improvements = new HashMap<>();

        for (String solver : currentResults.keySet()) {
            if (lastRound.containsKey(solver)) {
                PerformanceData current = currentResults.get(solver);
                PerformanceData previous = lastRound.get(solver);

                if (current.getCityCount() == previous.getCityCount()) {
                    double lengthDiff = previous.getPathLength() - current.getPathLength();
                    double timeDiff = previous.getExecutionTimeMs() - current.getExecutionTimeMs();

                    double lengthImprovement = (lengthDiff / previous.getPathLength()) * 100.0;
                    double timeImprovement = (timeDiff / previous.getExecutionTimeMs()) * 100.0;

                    improvements.put(solver, (lengthImprovement + timeImprovement) / 2.0);
                }
            }
        }

        return improvements;
    }

    // Inner data structure for each solution's performance
    public static class PerformanceData {
        private final String solutionName;
        private final int pathLength;
        private final long executionTimeMs;
        private final List<Integer> path;
        private final int cityCount;
        private final LocalDateTime timestamp;

        public PerformanceData(String solutionName, int pathLength, long executionTimeMs, List<Integer> path, int cityCount) {
            this.solutionName = solutionName;
            this.pathLength = pathLength;
            this.executionTimeMs = executionTimeMs;
            this.path = path;
            this.cityCount = cityCount;
            this.timestamp = LocalDateTime.now();
        }

        public String getSolutionName() { return solutionName; }
        public int getPathLength() { return pathLength; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public List<Integer> getPath() { return path; }
        public int getCityCount() { return cityCount; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
