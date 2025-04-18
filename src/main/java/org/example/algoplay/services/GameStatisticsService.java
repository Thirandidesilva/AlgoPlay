package org.example.algoplay.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.algoplay.models.TowerOfHanoiRound;

/**
 * Service for handling game statistics and analytics
 */
public class GameStatisticsService {
    private static GameStatisticsService instance;
    private DatabaseService dbService;

    private GameStatisticsService() {
        dbService = DatabaseService.getInstance();
    }

    public static GameStatisticsService getInstance() {
        if (instance == null) {
            instance = new GameStatisticsService();
        }
        return instance;
    }

    /**
     * Gets the high scores for Tower of Hanoi
     * @param limit maximum number of scores to return
     * @return list of high score data
     */
    public List<Map<String, Object>> getHanoiHighScores(int limit) {
        String sql = "SELECT t.*, u.username FROM tower_of_hanoi_rounds t " +
                "JOIN users u ON t.user_id = u.user_id " +
                "WHERE t.is_correct = true " +
                "ORDER BY t.moves_count ASC, t.num_disks DESC LIMIT ?";

        List<Map<String, Object>> highScores = new ArrayList<>();

        try (ResultSet rs = dbService.executeQuery(sql, limit)) {
            if (rs != null) {
                while (rs.next()) {
                    Map<String, Object> score = new HashMap<>();
                    score.put("username", rs.getString("username"));
                    score.put("numDisks", rs.getInt("num_disks"));
                    score.put("movesCount", rs.getInt("moves_count"));
                    score.put("optimalMoves", rs.getInt("optimal_moves"));
                    score.put("efficiency", calculateEfficiency(
                            rs.getInt("moves_count"),
                            rs.getInt("optimal_moves")));
                    highScores.add(score);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting high scores: " + e.getMessage());
        }

        return highScores;
    }

    /**
     * Gets algorithm performance comparison data
     * @return list of algorithm performance data
     */
    public List<Map<String, Object>> getAlgorithmPerformance() {
        String sql = "SELECT algorithm_type, " +
                "AVG(execution_time) as avg_time, " +
                "MIN(execution_time) as min_time, " +
                "MAX(execution_time) as max_time " +
                "FROM hanoi_algorithm_performance " +
                "GROUP BY algorithm_type " +
                "ORDER BY avg_time";

        List<Map<String, Object>> performances = new ArrayList<>();

        try (ResultSet rs = dbService.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    Map<String, Object> perf = new HashMap<>();
                    String type = rs.getString("algorithm_type");

                    // Format algorithm name for display
                    String displayName = formatAlgorithmName(type);

                    perf.put("algorithmType", displayName);
                    perf.put("avgTime", rs.getDouble("avg_time"));
                    perf.put("minTime", rs.getDouble("min_time"));
                    perf.put("maxTime", rs.getDouble("max_time"));
                    performances.add(perf);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting algorithm comparison: " + e.getMessage());
        }

        return performances;
    }

    /**
     * Gets the user's personal statistics for Tower of Hanoi
     * @param userId the user ID
     * @return map of user statistics
     */
    public Map<String, Object> getUserHanoiStats(int userId) {
        Map<String, Object> stats = new HashMap<>();

        String sql = "SELECT COUNT(*) as games_played, " +
                "AVG(moves_count) as avg_moves, " +
                "MIN(moves_count) as best_moves, " +
                "MAX(num_disks) as max_disks " +
                "FROM tower_of_hanoi_rounds " +
                "WHERE user_id = ? AND is_correct = true";

        try (ResultSet rs = dbService.executeQuery(sql, userId)) {
            if (rs != null && rs.next()) {
                stats.put("gamesPlayed", rs.getInt("games_played"));
                stats.put("avgMoves", rs.getDouble("avg_moves"));
                stats.put("bestMoves", rs.getInt("best_moves"));
                stats.put("maxDisks", rs.getInt("max_disks"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting user stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Gets difficulty distribution data for Tower of Hanoi
     * @return map of disk count to number of plays
     */
    public Map<Integer, Integer> getHanoiDifficultyDistribution() {
        Map<Integer, Integer> distribution = new HashMap<>();

        String sql = "SELECT num_disks, COUNT(*) as count " +
                "FROM tower_of_hanoi_rounds " +
                "GROUP BY num_disks " +
                "ORDER BY num_disks";

        try (ResultSet rs = dbService.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    distribution.put(rs.getInt("num_disks"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting difficulty distribution: " + e.getMessage());
        }

        return distribution;
    }

    /**
     * Calculates efficiency as a percentage of optimal moves vs. actual moves
     * @param movesCount actual number of moves
     * @param optimalMoves optimal number of moves
     * @return efficiency percentage (100% is optimal, lower is less efficient)
     */
    private double calculateEfficiency(int movesCount, int optimalMoves) {
        if (movesCount <= 0) return 0;
        return (double) optimalMoves / movesCount * 100;
    }

    /**
     * Formats algorithm name for display
     * @param type algorithm type from database
     * @return formatted display name
     */
    private String formatAlgorithmName(String type) {
        switch (type) {
            case "recursive":
                return "Recursive";
            case "iterative":
                return "Iterative";
            case "four_peg":
                return "Four Peg";
            default:
                return type.substring(0, 1).toUpperCase() + type.substring(1);
        }
    }
}