package org.example.algoplay.models;

import org.example.algoplay.services.DatabaseService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TowerOfHanoiRound {
    private int hanoiId;
    private int userId;
    private int numDisks;
    private int movesCount;
    private String movesSequence;
    private int optimalMoves;
    private boolean isCorrect;

    // Algorithm performance
    private long recursiveTime;
    private long iterativeTime;
    private long fourPegTime;

    // Constructor
    public TowerOfHanoiRound(int userId, int numDisks, int movesCount,
                             String movesSequence, int optimalMoves, boolean isCorrect) {
        this.userId = userId;
        this.numDisks = numDisks;
        this.movesCount = movesCount;
        this.movesSequence = movesSequence;
        this.optimalMoves = optimalMoves;
        this.isCorrect = isCorrect;
    }

    // Add this method to TowerOfHanoiGame.java
    // Add this method to TowerOfHanoiGame.java


    // Set algorithm performance times
    public void setAlgorithmTimes(long recursiveTime, long iterativeTime, long fourPegTime) {
        this.recursiveTime = recursiveTime;
        this.iterativeTime = iterativeTime;
        this.fourPegTime = fourPegTime;
    }

    // Save to database
    public boolean save() {
        DatabaseService db = DatabaseService.getInstance();
        ResultSet rs = null;
        boolean success = false;

        // Insert into tower_of_hanoi_rounds table
        String sql = "INSERT INTO tower_of_hanoi_rounds (user_id, num_disks, moves_count, " +
                "moves_sequence, optimal_moves, is_correct) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING hanoi_id";

        try {
            rs = db.executeQuery(sql, userId, numDisks, movesCount,
                    movesSequence, optimalMoves, isCorrect);

            if (rs != null && rs.next()) {
                this.hanoiId = rs.getInt("hanoi_id");
                System.out.println("Tower of Hanoi round saved with ID: " + hanoiId);

                // Save algorithm performance metrics
                saveAlgorithmPerformance("recursive", recursiveTime);
                saveAlgorithmPerformance("iterative", iterativeTime);
                if (fourPegTime > 0) {
                    saveAlgorithmPerformance("four_peg", fourPegTime);
                }

                success = true;
            } else {
                System.err.println("Failed to get hanoi_id from inserted record");
            }
        } catch (SQLException e) {
            System.err.println("Error saving Tower of Hanoi round: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Properly close the ResultSet
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("Error closing ResultSet: " + e.getMessage());
                }
            }
        }

        return success;
    }

    // Save algorithm performance
    private boolean saveAlgorithmPerformance(String algorithmType, long executionTime) {
        if (executionTime <= 0) return false;

        DatabaseService db = DatabaseService.getInstance();
        String sql = "INSERT INTO hanoi_algorithm_performance " +
                "(hanoi_id, algorithm_type, execution_time) VALUES (?, ?, ?)";

        boolean success = db.executeUpdate(sql, hanoiId, algorithmType, executionTime);
        if (success) {
            System.out.println("Saved " + algorithmType + " performance: " + executionTime + "ms");
        } else {
            System.err.println("Failed to save " + algorithmType + " performance");
        }
        return success;
    }

    // Get high scores
    public static List<TowerOfHanoiRound> getHighScores(int limit) {
        DatabaseService db = DatabaseService.getInstance();
        String sql = "SELECT t.*, u.username FROM tower_of_hanoi_rounds t " +
                "JOIN users u ON t.user_id = u.user_id " +
                "WHERE t.is_correct = true " +
                "ORDER BY t.moves_count ASC, t.num_disks DESC LIMIT ?";

        List<TowerOfHanoiRound> highScores = new ArrayList<>();

        try (ResultSet rs = db.executeQuery(sql, limit)) {
            if (rs != null) {
                while (rs.next()) {
                    // Create TowerOfHanoiRound from ResultSet
                    // (simplified for brevity)
                    TowerOfHanoiRound round = new TowerOfHanoiRound(
                            rs.getInt("user_id"),
                            rs.getInt("num_disks"),
                            rs.getInt("moves_count"),
                            rs.getString("moves_sequence"),
                            rs.getInt("optimal_moves"),
                            rs.getBoolean("is_correct")
                    );
                    round.hanoiId = rs.getInt("hanoi_id");

                    // Add username if needed
                    // round.username = rs.getString("username");

                    highScores.add(round);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting high scores: " + e.getMessage());
        }

        return highScores;
    }

    // Get algorithm performance comparison
    public static List<AlgorithmPerformance> getAlgorithmComparison() {
        DatabaseService db = DatabaseService.getInstance();
        String sql = "SELECT algorithm_type, AVG(execution_time) as avg_time " +
                "FROM hanoi_algorithm_performance " +
                "GROUP BY algorithm_type " +
                "ORDER BY avg_time";

        List<AlgorithmPerformance> performances = new ArrayList<>();

        try (ResultSet rs = db.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    String type = rs.getString("algorithm_type");
                    double avgTime = rs.getDouble("avg_time");
                    performances.add(new AlgorithmPerformance(type, avgTime));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting algorithm comparison: " + e.getMessage());
        }

        return performances;
    }

    // Inner class for algorithm performance
    public static class AlgorithmPerformance {
        private String algorithmType;
        private double avgExecutionTime;

        public AlgorithmPerformance(String algorithmType, double avgExecutionTime) {
            this.algorithmType = algorithmType;
            this.avgExecutionTime = avgExecutionTime;
        }

        // Getters
        public String getAlgorithmType() { return algorithmType; }
        public double getAvgExecutionTime() { return avgExecutionTime; }
    }
}