package org.example.algoplay.games.tsp;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserScoring {

    // Constants for score calculation
    private static final int MAX_TIME_SCORE = 1000;
    private static final int MAX_PATH_SCORE = 1000;
    private static final int MAX_TOTAL_SCORE = MAX_TIME_SCORE + MAX_PATH_SCORE;

    // For formatting numbers
    private final DecimalFormat scoreFormat = new DecimalFormat("#,###");
    private final DecimalFormat percentFormat = new DecimalFormat("#.0");

    // UI elements
    private final Label scoreLabel = new Label("0");  // Kept original name for compatibility
    private final Label scoreMessage = new Label("");
    private final HBox scoreContainer = new HBox();

    // Current score data
    private int currentScore = 0;  // Kept original name for compatibility
    private int timeScore = 0;
    private int pathScore = 0;

    // High score for current difficulty
    private int highScore = 0;

    // Algorithm scores
    private Map<String, Integer> algorithmScores = new HashMap<>();
    private Map<String, Label> algorithmLabels = new HashMap<>();

    // Names of algorithms to track
    private static final String[] ALGORITHMS = {"User", "Ant", "Bee", "Hybrid"};

    public UserScoring() {
        // Initialize algorithm scores
        for (String algorithm : ALGORITHMS) {
            algorithmScores.put(algorithm, 0);
        }

        initializeScoreUI();
    }

    /**
     * Calculate user score based on time taken and path quality
     * Original method kept for backward compatibility
     */
    public int calculateScore(List<Integer> userPath, List<Integer> optimalPath,
                              int[][] dist, long timeTakenMs, int cityCount) {
        // Reset scores
        timeScore = 0;
        pathScore = 0;

        // Calculate path quality score
        int userPathLength = calcLen(userPath, dist);
        int optimalPathLength = calcLen(optimalPath, dist);

        // Path score is based on how close to optimal the user's solution is
        double pathRatio = (double) optimalPathLength / userPathLength;
        pathScore = (int) (MAX_PATH_SCORE * pathRatio);

        // Calculate time score
        // Base expected time: 1 second per city with additional 5 seconds base time
        long expectedTimeMs = (cityCount * 1000) + 5000;

        // Time score decreases as user takes longer than expected time
        double timeRatio = (double) expectedTimeMs / timeTakenMs;
        timeRatio = Math.min(1.0, timeRatio); // Cap at 1.0 (100%)
        timeScore = (int) (MAX_TIME_SCORE * timeRatio);

        // Calculate total score
        currentScore = timeScore + pathScore;
        algorithmScores.put("User", currentScore);

        // Update high score if needed
        if (currentScore > highScore) {
            highScore = currentScore;
        }

        // Update UI
        updateScoreDisplay();

        return currentScore;
    }

    /**
     * Calculate score for an algorithm based on path quality only
     *
     * @param algorithmName Name of the algorithm (Ant, Bee, or Hybrid)
     * @param algorithmPath The path solution found by the algorithm
     * @param optimalPath The best known optimal path
     * @param dist Distance matrix for the cities
     * @return The score achieved by the algorithm
     */
    public int calculateAlgorithmScore(String algorithmName, List<Integer> algorithmPath,
                                       List<Integer> optimalPath, int[][] dist) {
        // Validate algorithm name
        if (!algorithmScores.containsKey(algorithmName)) {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }

        // Calculate path quality score
        int algorithmPathLength = calcLen(algorithmPath, dist);
        int optimalPathLength = calcLen(optimalPath, dist);

        // Path score is based on how close to optimal the algorithm's solution is
        double pathRatio = (double) optimalPathLength / algorithmPathLength;
        int algorithmScore = (int) (MAX_PATH_SCORE * pathRatio);

        // Algorithms get a fixed time score since they're programmatic
        int totalScore = algorithmScore + MAX_TIME_SCORE;

        // Store the score
        algorithmScores.put(algorithmName, totalScore);

        // Update the UI
        updateScoreDisplay();

        return totalScore;
    }

    /**
     * Display the score with animation
     */
    public void displayScore() {
        // Create animations
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(500), scoreLabel);
        scaleTransition.setFromX(0.5);
        scaleTransition.setFromY(0.5);
        scaleTransition.setToX(1.2);
        scaleTransition.setToY(1.2);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), scoreContainer);
        fadeTransition.setFromValue(0.5);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(1);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(scaleTransition, fadeTransition);
        parallelTransition.play();

        // Set appropriate message based on score
        setScoreMessage();
    }

    /**
     * Reset all scores for a new puzzle
     */
    public void resetScore() {
        currentScore = 0;
        timeScore = 0;
        pathScore = 0;

        // Reset algorithm scores
        for (String algorithm : ALGORITHMS) {
            algorithmScores.put(algorithm, 0);
        }

        updateScoreDisplay();
        scoreMessage.setText("");
    }

    /**
     * Get the HBox container with score information to add to the UI
     */
    public HBox getScoreContainer() {
        return scoreContainer;
    }

    /**
     * Get current user score - original method kept for compatibility
     */
    public int getCurrentScore() {
        return currentScore;
    }

    /**
     * Get high score
     */
    public int getHighScore() {
        return highScore;
    }

    /**
     * Get score for a specific algorithm
     */
    public int getAlgorithmScore(String algorithmName) {
        if (!algorithmScores.containsKey(algorithmName)) {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
        return algorithmScores.get(algorithmName);
    }

    /**
     * Get the algorithm with the highest score
     */
    public String getBestPerformer() {
        String bestAlgorithm = "";
        int highestScore = -1;

        for (Map.Entry<String, Integer> entry : algorithmScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                bestAlgorithm = entry.getKey();
            }
        }

        return bestAlgorithm;
    }

    /**
     * Initialize the score UI elements
     */
    private void initializeScoreUI() {
        // Style the user score display
        scoreLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        scoreLabel.setStyle("-fx-text-fill: #56eb61;"); // Green color

        // Add glow effect to score
        Glow glow = new Glow();
        glow.setLevel(0.4);
        scoreLabel.setEffect(glow);

        // Style message
        scoreMessage.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        scoreMessage.setStyle("-fx-text-fill: #aaaaaa;");

        // Create score details panel
        VBox scoreDetails = new VBox(5);

        // Create labels for time and path scores
        Label timeScoreLabel = new Label("Time: 0");
        timeScoreLabel.setStyle("-fx-text-fill: #aaaaaa;");

        Label pathScoreLabel = new Label("Path: 0");
        pathScoreLabel.setStyle("-fx-text-fill: #aaaaaa;");

        Label highScoreLabel = new Label("High: 0");
        highScoreLabel.setStyle("-fx-text-fill: #f4d742;");

        scoreDetails.getChildren().addAll(timeScoreLabel, pathScoreLabel, highScoreLabel);

        // Create algorithm score panel
        VBox algorithmPanel = new VBox(5);
        algorithmPanel.setStyle("-fx-padding: 5; -fx-background-color: #222222; -fx-background-radius: 5;");
        Text algoTitle = new Text("ALGORITHMS");
        algoTitle.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        algoTitle.setFill(Color.WHITE);
        algorithmPanel.getChildren().add(algoTitle);

        // Create labels for each algorithm (excluding user which is shown separately)
        for (String algorithm : ALGORITHMS) {
            if (!"User".equals(algorithm)) {
                Label algoLabel = new Label(algorithm + ": 0");
                algoLabel.setStyle("-fx-text-fill: #aaaaaa;");
                algorithmLabels.put(algorithm, algoLabel);
                algorithmPanel.getChildren().add(algoLabel);
            }
        }

        // Add shadow to container
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);

        // Text showing "USER SCORE"
        Text scoreTitleText = new Text("USER SCORE");
        scoreTitleText.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        scoreTitleText.setFill(Color.WHITE);

        // Organize the score display in a VBox
        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.getChildren().addAll(scoreTitleText, scoreLabel, scoreMessage);

        // Main container
        scoreContainer.setAlignment(Pos.CENTER);
        scoreContainer.setSpacing(20);
        scoreContainer.setPadding(new Insets(15));
        scoreContainer.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 10;");
        scoreContainer.setEffect(shadow);
        scoreContainer.getChildren().addAll(scoreBox, scoreDetails, algorithmPanel);
    }

    /**
     * Update the score display with current values
     */
    private void updateScoreDisplay() {
        scoreLabel.setText(scoreFormat.format(currentScore));

        // Update the detailed score labels
        Label timeScoreLabel = (Label) ((VBox) scoreContainer.getChildren().get(1)).getChildren().get(0);
        timeScoreLabel.setText("Time: " + scoreFormat.format(timeScore));

        Label pathScoreLabel = (Label) ((VBox) scoreContainer.getChildren().get(1)).getChildren().get(1);
        pathScoreLabel.setText("Path: " + scoreFormat.format(pathScore));

        Label highScoreLabel = (Label) ((VBox) scoreContainer.getChildren().get(1)).getChildren().get(2);
        highScoreLabel.setText("High: " + scoreFormat.format(highScore));

        // Update algorithm scores
        for (String algorithm : ALGORITHMS) {
            if (!"User".equals(algorithm) && algorithmLabels.containsKey(algorithm)) {
                algorithmLabels.get(algorithm).setText(algorithm + ": " +
                        scoreFormat.format(algorithmScores.get(algorithm)));
            }
        }
    }

    /**
     * Set message based on the current score and comparison with algorithms
     */
    private void setScoreMessage() {
        // First determine generic message based on score
        if (currentScore >= MAX_TOTAL_SCORE * 0.95) {
            scoreMessage.setText("Perfect!");
            scoreMessage.setStyle("-fx-text-fill: gold;");
        } else if (currentScore >= MAX_TOTAL_SCORE * 0.8) {
            scoreMessage.setText("Excellent!");
            scoreMessage.setStyle("-fx-text-fill: #56eb61;");
        } else if (currentScore >= MAX_TOTAL_SCORE * 0.6) {
            scoreMessage.setText("Great job!");
            scoreMessage.setStyle("-fx-text-fill: #56eb61;");
        } else if (currentScore >= MAX_TOTAL_SCORE * 0.4) {
            scoreMessage.setText("Good effort!");
            scoreMessage.setStyle("-fx-text-fill: #aaaaaa;");
        } else {
            scoreMessage.setText("Keep practicing!");
            scoreMessage.setStyle("-fx-text-fill: #aaaaaa;");
        }

        // Now compare with algorithms and add comparative message
        String bestAlgorithm = getBestPerformer();
        if ("User".equals(bestAlgorithm) && currentScore > 0) {
            scoreMessage.setText(scoreMessage.getText() + " You beat all algorithms!");
            scoreMessage.setStyle("-fx-text-fill: gold;");
        }
    }

    /**
     * Calculate the length of a path
     */
    private int calcLen(List<Integer> path, int[][] dist) {
        int len = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            len += dist[path.get(i)][path.get(i + 1)];
        }
        len += dist[path.get(path.size() - 1)][path.get(0)];
        return len;
    }
}