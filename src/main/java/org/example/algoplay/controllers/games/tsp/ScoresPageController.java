package org.example.algoplay.controllers.games.tsp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.example.algoplay.games.tsp.TspScoreCalculator.SolutionScore;
import org.example.algoplay.games.tsp.Point;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Scores Page view
 * Displays detailed scoring information and comparisons between solutions
 */
public class ScoresPageController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private TableView<SolutionScoreRow> scoresTable;
    @FXML private TableColumn<SolutionScoreRow, String> solverColumn;
    @FXML private TableColumn<SolutionScoreRow, Double> totalScoreColumn;
    @FXML private TableColumn<SolutionScoreRow, Double> pathScoreColumn;
    @FXML private TableColumn<SolutionScoreRow, Double> timeScoreColumn;
    @FXML private TableColumn<SolutionScoreRow, Double> qualityScoreColumn;
    @FXML private TableColumn<SolutionScoreRow, Integer> pathLengthColumn;
    @FXML private TableColumn<SolutionScoreRow, Long> computationTimeColumn;

    @FXML private Label winnerLabel;
    @FXML private Label sessionStatsLabel;
    @FXML private Button backButton;
    @FXML private Canvas solutionsCanvas;
    @FXML private BarChart<String, Number> scoreComparisonChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private GraphicsContext gc;
    private List<Point> cityPoints;
    private Map<String, List<Integer>> paths;
    private Map<String, SolutionScore> scores;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = solutionsCanvas.getGraphicsContext2D();

        // Initialize table columns
        solverColumn.setCellValueFactory(new PropertyValueFactory<>("solverName"));
        totalScoreColumn.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
        pathScoreColumn.setCellValueFactory(new PropertyValueFactory<>("pathScore"));
        timeScoreColumn.setCellValueFactory(new PropertyValueFactory<>("timeScore"));
        qualityScoreColumn.setCellValueFactory(new PropertyValueFactory<>("qualityScore"));
        pathLengthColumn.setCellValueFactory(new PropertyValueFactory<>("pathLength"));
        computationTimeColumn.setCellValueFactory(new PropertyValueFactory<>("computationTime"));

        backButton.setOnAction(e -> goBack());

        // Make canvas resize with window
        solutionsCanvas.widthProperty().bind(mainContainer.widthProperty().multiply(0.6));
        solutionsCanvas.heightProperty().bind(mainContainer.heightProperty().multiply(0.4));
        solutionsCanvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (cityPoints != null) drawSolutions();
        });
        solutionsCanvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (cityPoints != null) drawSolutions();
        });
    }

    /**
     * Set the data to display on this page
     */
    public void setData(List<Point> cityPoints, Map<String, List<Integer>> paths,
                        Map<String, SolutionScore> scores, int numCities) {
        this.cityPoints = cityPoints;
        this.paths = paths;
        this.scores = scores;

        populateTable();
        createScoreChart();
        drawSolutions();
        updateWinnerLabel();

        sessionStatsLabel.setText("Session Stats: " + numCities + " cities");
    }

    private void populateTable() {
        scoresTable.getItems().clear();

        for (Map.Entry<String, SolutionScore> entry : scores.entrySet()) {
            String solver = entry.getKey();
            SolutionScore score = entry.getValue();

            scoresTable.getItems().add(new SolutionScoreRow(
                    solver,
                    score.getTotalScore(),
                    score.getPathLengthScore(),
                    score.getTimeScore(),
                    score.getQualityScore(),
                    score.getPathLength(),
                    score.getComputationTime()
            ));
        }
    }

    private void createScoreChart() {
        scoreComparisonChart.getData().clear();

        // Create series for different score components
        XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
        totalSeries.setName("Total Score");

        XYChart.Series<String, Number> pathSeries = new XYChart.Series<>();
        pathSeries.setName("Path Score");

        XYChart.Series<String, Number> timeSeries = new XYChart.Series<>();
        timeSeries.setName("Time Score");

        XYChart.Series<String, Number> qualitySeries = new XYChart.Series<>();
        qualitySeries.setName("Quality Score");

        // Add data for each solver
        for (Map.Entry<String, SolutionScore> entry : scores.entrySet()) {
            String solver = entry.getKey();
            SolutionScore score = entry.getValue();

            totalSeries.getData().add(new XYChart.Data<>(solver, score.getTotalScore()));
            pathSeries.getData().add(new XYChart.Data<>(solver, score.getPathLengthScore()));
            timeSeries.getData().add(new XYChart.Data<>(solver, score.getTimeScore()));
            qualitySeries.getData().add(new XYChart.Data<>(solver, score.getQualityScore()));
        }

        scoreComparisonChart.getData().addAll(totalSeries, pathSeries, timeSeries, qualitySeries);
    }

    private void drawSolutions() {
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRect(0, 0, solutionsCanvas.getWidth(), solutionsCanvas.getHeight());

        // Scale coordinates to fit canvas
        double maxX = 0, maxY = 0;
        for (Point p : cityPoints) {
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }

        double scaleX = (solutionsCanvas.getWidth() - 40) / maxX;
        double scaleY = (solutionsCanvas.getHeight() - 40) / maxY;
        double scale = Math.min(scaleX, scaleY);

        // Draw all cities
        for (int i = 0; i < cityPoints.size(); i++) {
            Point p = cityPoints.get(i);
            double x = 20 + p.x * scale;
            double y = 20 + p.y * scale;

            gc.setFill(Color.DODGERBLUE);
            gc.fillOval(x - 3, y - 3, 6, 6);
        }

        // Draw paths with different colors
        Color[] pathColors = {Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.LIGHTGREEN};
        int colorIndex = 0;

        for (Map.Entry<String, List<Integer>> entry : paths.entrySet()) {
            String solver = entry.getKey();
            List<Integer> path = entry.getValue();

            Color pathColor = pathColors[colorIndex % pathColors.length];
            colorIndex++;

            // Choose line width based on if this is the winning solution
            double lineWidth = isWinner(solver) ? 2.5 : 1.5;

            // Draw path
            gc.setStroke(pathColor);
            gc.setLineWidth(lineWidth);

            for (int i = 0; i < path.size(); i++) {
                int current = path.get(i);
                int next = path.get((i + 1) % path.size());

                Point p1 = cityPoints.get(current);
                Point p2 = cityPoints.get(next);

                double x1 = 20 + p1.x * scale;
                double y1 = 20 + p1.y * scale;
                double x2 = 20 + p2.x * scale;
                double y2 = 20 + p2.y * scale;

                gc.strokeLine(x1, y1, x2, y2);
            }

            // Add label for this solution
            gc.setFill(pathColor);
            gc.fillText(solver, 10, 15 + colorIndex * 15);
        }
    }

    private boolean isWinner(String solver) {
        double maxScore = 0;
        for (SolutionScore score : scores.values()) {
            maxScore = Math.max(maxScore, score.getTotalScore());
        }

        return scores.get(solver).getTotalScore() >= maxScore - 0.01;
    }

    private void updateWinnerLabel() {
        String winner = "";
        double maxScore = 0;

        for (Map.Entry<String, SolutionScore> entry : scores.entrySet()) {
            if (entry.getValue().getTotalScore() > maxScore) {
                maxScore = entry.getValue().getTotalScore();
                winner = entry.getKey();
            }
        }

        winnerLabel.setText("Winner: " + winner + " (Score: " + String.format("%.2f", maxScore) + ")");
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Inner class for table data
     */
    public static class SolutionScoreRow {
        private final String solverName;
        private final double totalScore;
        private final double pathScore;
        private final double timeScore;
        private final double qualityScore;
        private final int pathLength;
        private final long computationTime;

        public SolutionScoreRow(String solverName, double totalScore, double pathScore,
                                double timeScore, double qualityScore,
                                int pathLength, long computationTime) {
            this.solverName = solverName;
            this.totalScore = totalScore;
            this.pathScore = pathScore;
            this.timeScore = timeScore;
            this.qualityScore = qualityScore;
            this.pathLength = pathLength;
            this.computationTime = computationTime;
        }

        public String getSolverName() { return solverName; }
        public double getTotalScore() { return totalScore; }
        public double getPathScore() { return pathScore; }
        public double getTimeScore() { return timeScore; }
        public double getQualityScore() { return qualityScore; }
        public int getPathLength() { return pathLength; }
        public long getComputationTime() { return computationTime; }
    }
}