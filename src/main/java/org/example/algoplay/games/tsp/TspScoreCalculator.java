package org.example.algoplay.games.tsp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates performance scores for TSP solutions
 * Evaluates different aspects of solutions including path length, computation time,
 * and other metrics to provide a comprehensive score.
 */
public class TspScoreCalculator {

    // Weights for different scoring factors
    private static final double WEIGHT_PATH_LENGTH = 0.65;
    private static final double WEIGHT_COMPUTATION_TIME = 0.25;
    private static final double WEIGHT_SOLUTION_QUALITY = 0.10;

    // Maximum scores for each category
    private static final int MAX_SCORE = 100;

    /**
     * Calculate scores for multiple solutions
     * @param pathLengths Map of solver name to path length
     * @param computationTimes Map of solver name to computation time in ms
     * @param cities List of city points
     * @param paths Map of solver name to solution path
     * @return Map of solver name to overall score
     */
    public Map<String, SolutionScore> calculateScores(
            Map<String, Integer> pathLengths,
            Map<String, Long> computationTimes,
            List<Point> cities,
            Map<String, List<Integer>> paths) {

        Map<String, SolutionScore> scores = new HashMap<>();

        // Find best values for normalization
        int bestLength = Integer.MAX_VALUE;
        long bestTime = Long.MAX_VALUE;

        for (String solver : pathLengths.keySet()) {
            bestLength = Math.min(bestLength, pathLengths.get(solver));
            bestTime = Math.min(bestTime, computationTimes.get(solver));
        }

        // Calculate scores for each solver
        for (String solver : pathLengths.keySet()) {
            int pathLength = pathLengths.get(solver);
            long computationTime = computationTimes.get(solver);
            List<Integer> path = paths.get(solver);

            // Calculate normalized path length score (inverse - shorter is better)
            double pathLengthScore = MAX_SCORE * ((double)bestLength / pathLength);

            // Calculate normalized computation time score (inverse - faster is better)
            double timeScore = MAX_SCORE * ((double)bestTime / computationTime);

            // Calculate solution quality score (based on crossings and distribution)
            double qualityScore = calculateQualityScore(path, cities);

            // Calculate weighted total score
            double totalScore = WEIGHT_PATH_LENGTH * pathLengthScore +
                    WEIGHT_COMPUTATION_TIME * timeScore +
                    WEIGHT_SOLUTION_QUALITY * qualityScore;

            // Round to 2 decimal places
            totalScore = Math.round(totalScore * 100.0) / 100.0;

            // Create detailed score breakdown
            SolutionScore score = new SolutionScore(
                    totalScore,
                    pathLengthScore,
                    timeScore,
                    qualityScore,
                    pathLength,
                    computationTime
            );

            scores.put(solver, score);
        }

        return scores;
    }

    /**
     * Calculate a quality score based on path characteristics
     * @param path The solution path
     * @param cities The list of city points
     * @return Quality score from 0-100
     */
    private double calculateQualityScore(List<Integer> path, List<Point> cities) {
        if (path == null || cities == null || path.isEmpty()) {
            return 0;
        }

        // Calculate path crossings (fewer is better)
        int crossings = countPathCrossings(path, cities);

        // Penalize for each crossing
        double crossingPenalty = Math.min(50, crossings * 5);

        // Analyze distribution of path segments
        double distributionScore = analyzePathDistribution(path, cities);

        // Final quality score
        return MAX_SCORE - crossingPenalty + distributionScore * 0.5;
    }

    /**
     * Count how many times the path crosses itself
     * @param path The solution path
     * @param cities The list of city points
     * @return Number of crossings
     */
    private int countPathCrossings(List<Integer> path, List<Point> cities) {
        int crossings = 0;

        // For each pair of line segments in the path
        for (int i = 0; i < path.size(); i++) {
            int i2 = (i + 1) % path.size();
            Point p1 = cities.get(path.get(i));
            Point p2 = cities.get(path.get(i2));

            for (int j = i + 2; j < path.size() + (i > 0 ? 0 : 1); j++) {
                int j2 = (j + 1) % path.size();
                if (j2 == i) continue; // Skip adjacent segments

                Point p3 = cities.get(path.get(j));
                Point p4 = cities.get(path.get(j2));

                if (doLineSegmentsIntersect(p1, p2, p3, p4)) {
                    crossings++;
                }
            }
        }

        return crossings;
    }

    /**
     * Check if two line segments intersect
     */
    private boolean doLineSegmentsIntersect(Point p1, Point p2, Point p3, Point p4) {
        double d1 = direction(p3, p4, p1);
        double d2 = direction(p3, p4, p2);
        double d3 = direction(p1, p2, p3);
        double d4 = direction(p1, p2, p4);

        // Check if the segments intersect
        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
                ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }

        // Check for collinearity
        if (d1 == 0 && onSegment(p3, p4, p1)) return true;
        if (d2 == 0 && onSegment(p3, p4, p2)) return true;
        if (d3 == 0 && onSegment(p1, p2, p3)) return true;
        if (d4 == 0 && onSegment(p1, p2, p4)) return true;

        return false;
    }

    private double direction(Point a, Point b, Point c) {
        return (c.x - a.x) * (b.y - a.y) - (b.x - a.x) * (c.y - a.y);
    }

    private boolean onSegment(Point p, Point q, Point r) {
        return r.x <= Math.max(p.x, q.x) && r.x >= Math.min(p.x, q.x) &&
                r.y <= Math.max(p.y, q.y) && r.y >= Math.min(p.y, q.y);
    }

    /**
     * Analyze the distribution of path segments
     * Rewards paths that have more consistent segment lengths
     * @param path The solution path
     * @param cities The list of city points
     * @return Distribution score from 0-100
     */
    private double analyzePathDistribution(List<Integer> path, List<Point> cities) {
        if (path.size() < 2) return 0;

        double[] distances = new double[path.size()];
        double totalDistance = 0;

        // Calculate distances between consecutive points
        for (int i = 0; i < path.size(); i++) {
            int j = (i + 1) % path.size();
            Point p1 = cities.get(path.get(i));
            Point p2 = cities.get(path.get(j));
            distances[i] = p1.distanceTo(p2);
            totalDistance += distances[i];
        }

        // Calculate average distance
        double avgDistance = totalDistance / path.size();

        // Calculate standard deviation
        double sumSquaredDiffs = 0;
        for (double d : distances) {
            sumSquaredDiffs += Math.pow(d - avgDistance, 2);
        }

        double stdDev = Math.sqrt(sumSquaredDiffs / path.size());

        // Lower standard deviation is better (more consistent distances)
        double coeffOfVariation = stdDev / avgDistance;

        // Score based on coefficient of variation (inverse relationship)
        // 0 is perfect, higher is worse
        double normalizedScore = MAX_SCORE * Math.max(0, 1 - (coeffOfVariation / 0.5));

        return Math.min(MAX_SCORE, normalizedScore);
    }

    /**
     * Class to hold detailed score information
     */
    public static class SolutionScore {
        private final double totalScore;
        private final double pathLengthScore;
        private final double timeScore;
        private final double qualityScore;
        private final int pathLength;
        private final long computationTime;

        public SolutionScore(double totalScore, double pathLengthScore,
                             double timeScore, double qualityScore,
                             int pathLength, long computationTime) {
            this.totalScore = totalScore;
            this.pathLengthScore = pathLengthScore;
            this.timeScore = timeScore;
            this.qualityScore = qualityScore;
            this.pathLength = pathLength;
            this.computationTime = computationTime;
        }

        public double getTotalScore() {
            return totalScore;
        }

        public double getPathLengthScore() {
            return pathLengthScore;
        }

        public double getTimeScore() {
            return timeScore;
        }

        public double getQualityScore() {
            return qualityScore;
        }

        public int getPathLength() {
            return pathLength;
        }

        public long getComputationTime() {
            return computationTime;
        }

        @Override
        public String toString() {
            return String.format("Score: %.2f (Path: %.2f, Time: %.2f, Quality: %.2f)",
                    totalScore, pathLengthScore, timeScore, qualityScore);
        }

        /**
         * Get a short version of the score for display
         */
        public String getShortScore() {
            return String.format("%.2f", totalScore);
        }

        /**
         * Get a detailed breakdown of the score
         */
        public String getDetailedBreakdown() {
            return String.format(
                    "Total Score: %.2f\n" +
                            "├─ Path Length: %.2f (%d units)\n" +
                            "├─ Computation: %.2f (%d ms)\n" +
                            "└─ Quality: %.2f",
                    totalScore,
                    pathLengthScore, pathLength,
                    timeScore, computationTime,
                    qualityScore
            );
        }
    }
}