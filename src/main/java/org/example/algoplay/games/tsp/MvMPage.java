package org.example.algoplay.games.tsp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MvMPage extends Application {

    private final int canvasWidth = 600;
    private final int canvasHeight = 400; // Reduced canvas height
    private final List<Point> cityPoints = new ArrayList<>();
    private final Canvas canvas = new Canvas(canvasWidth, canvasHeight);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    private final Button runRaceBtn = new Button("Start Race");
    private final Button resetBtn = new Button("Reset");
    private final Button logOutBtn = new Button("Log-Out");
    private final Button newCitiesBtn = new Button("Random Cities");
    private final Slider citiesSlider = new Slider(5, 20, 10);

    // Result labels for each algorithm
    private final Label antTimeLabel = new Label("-");
    private final Label beeTimeLabel = new Label("-");
    private final Label hybridTimeLabel = new Label("-");

    private final Label antPathLabel = new Label("-");
    private final Label beePathLabel = new Label("-");
    private final Label hybridPathLabel = new Label("-");

    private final Label antStatsLabel = new Label("-");
    private final Label beeStatsLabel = new Label("-");
    private final Label hybridStatsLabel = new Label("-");

    private final Label antWinnerLabel = new Label("");
    private final Label beeWinnerLabel = new Label("");
    private final Label hybridWinnerLabel = new Label("");

    private final ProgressIndicator antProgress = new ProgressIndicator();
    private final ProgressIndicator beeProgress = new ProgressIndicator();
    private final ProgressIndicator hybridProgress = new ProgressIndicator();

    private final Slider iterationsSlider = new Slider(50, 500, 100);
    private final Label iterationsLabel = new Label("Iterations: 100");
    private final Slider populationSlider = new Slider(10, 100, 30);
    private final Label populationLabel = new Label("Population: 30");

    @Override
    public void start(Stage stage) {
        stage.setTitle("AlgoPlay - TSP Visualization");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");

        // Top: Header
        HBox header = createHeader();
        root.setTop(header);

        HBox paramControls = createParamControls();
        root.setTop(paramControls);

        // Center: Canvas with border
        StackPane canvasWrapper = new StackPane(canvas);
        canvasWrapper.setStyle("-fx-border-color: #444444; -fx-border-width: 1; -fx-padding: 0;");
        canvasWrapper.setPadding(new Insets(10));
        root.setCenter(canvasWrapper);

        // Bottom: Results table
        VBox resultsArea = createResultsArea();
        root.setBottom(resultsArea);

        // Canvas setup
        setupCanvas();

        // Button actions
        runRaceBtn.setOnAction(e -> runRace());
        resetBtn.setOnAction(e -> resetCanvas());
        logOutBtn.setOnAction(e -> System.out.println("Log out clicked"));
        newCitiesBtn.setOnAction(e -> generateRandomCities((int) citiesSlider.getValue()));

        Scene scene = new Scene(root, 1200, 750); // Also reduced scene height
        stage.setScene(scene);
        stage.show();
    }
    private HBox createParamControls() {
        HBox paramControls = new HBox(20);

        // Iterations slider with label
        VBox iterBox = new VBox(5);
        iterationsSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            iterationsLabel.setText("Iterations: " + newVal.intValue());
        });
        iterBox.getChildren().addAll(iterationsLabel, iterationsSlider);

        // Population slider with label
        VBox popBox = new VBox(5);
        populationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            populationLabel.setText("Population: " + newVal.intValue());
        });
        popBox.getChildren().addAll(populationLabel, populationSlider);

        paramControls.getChildren().addAll(iterBox, popBox);
        return paramControls;
    }

    private HBox createHeader() {
        Label titleLabel = new Label("AlgoPlay");
        titleLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #50fa7b;");

        Label subtitleLabel = new Label("Traveling Salesman - Machine vs Machine");
        subtitleLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 24));
        subtitleLabel.setStyle("-fx-text-fill: #50fa7b;");

        logOutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #50fa7b; -fx-border-color: #50fa7b; -fx-border-radius: 5;");

        HBox leftSide = new HBox(20, titleLabel, subtitleLabel);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        HBox rightSide = new HBox(logOutBtn);
        rightSide.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox();
        header.getChildren().addAll(leftSide, rightSide);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(15));
        header.setSpacing(20);
        HBox.setHgrow(leftSide, Priority.ALWAYS);

        return header;
    }

    private void setupCanvas() {
        // Set initial canvas state
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Text for empty canvas
        gc.setFill(Color.GRAY);
        gc.setFont(Font.font("Monospace", 20));
        gc.fillText("Click on canvas to add cities", canvasWidth/2 - 150, canvasHeight/2);

        // Canvas event
        canvas.setOnMouseClicked(e -> {
            if (cityPoints.size() >= 20) return;
            cityPoints.add(new Point(e.getX(), e.getY()));
            drawCities();
        });
    }

    private VBox createResultsArea() {
        // Create header row
        Label antHeader = createHeaderLabel("Ant");
        Label beeHeader = createHeaderLabel("Bee");
        Label hybridHeader = createHeaderLabel("Hybrid");

        // Create row headers
        Label timeHeader = createRowHeaderLabel("Time Taken");
        Label pathHeader = createRowHeaderLabel("Best Path");
        Label statHeader = createRowHeaderLabel("Path Details");
        Label winnerHeader = createRowHeaderLabel("Winner");

        // Style result labels
        styleResultLabel(antTimeLabel);
        styleResultLabel(beeTimeLabel);
        styleResultLabel(hybridTimeLabel);
        styleResultLabel(antPathLabel);
        styleResultLabel(beePathLabel);
        styleResultLabel(hybridPathLabel);
        styleResultLabel(antStatsLabel);
        styleResultLabel(beeStatsLabel);
        styleResultLabel(hybridStatsLabel);
        styleResultLabel(antWinnerLabel);
        styleResultLabel(beeWinnerLabel);
        styleResultLabel(hybridWinnerLabel);

        // Create grid for results
        GridPane resultsGrid = new GridPane();
        resultsGrid.setHgap(20);
        resultsGrid.setVgap(10);
        resultsGrid.setPadding(new Insets(20));
        resultsGrid.setStyle("-fx-background-color: #1e1e1e;");

        // First row - headers
        resultsGrid.add(new Label(""), 0, 0);
        resultsGrid.add(antHeader, 1, 0);
        resultsGrid.add(beeHeader, 2, 0);
        resultsGrid.add(hybridHeader, 3, 0);

        // Data rows
        resultsGrid.add(timeHeader, 0, 1);
        resultsGrid.add(antTimeLabel, 1, 1);
        resultsGrid.add(beeTimeLabel, 2, 1);
        resultsGrid.add(hybridTimeLabel, 3, 1);

        resultsGrid.add(pathHeader, 0, 2);
        resultsGrid.add(antPathLabel, 1, 2);
        resultsGrid.add(beePathLabel, 2, 2);
        resultsGrid.add(hybridPathLabel, 3, 2);

        resultsGrid.add(statHeader, 0, 3);
        resultsGrid.add(antStatsLabel, 1, 3);
        resultsGrid.add(beeStatsLabel, 2, 3);
        resultsGrid.add(hybridStatsLabel, 3, 3);

        resultsGrid.add(winnerHeader, 0, 4);
        resultsGrid.add(antWinnerLabel, 1, 4);
        resultsGrid.add(beeWinnerLabel, 2, 4);
        resultsGrid.add(hybridWinnerLabel, 3, 4);

        // Set column constraints to make columns equal width
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setPercentWidth(15);

        ColumnConstraints colN = new ColumnConstraints();
        colN.setPercentWidth(28.33);

        resultsGrid.getColumnConstraints().addAll(col0, colN, colN, colN);

        // Random city generator controls
        HBox sliderBox = new HBox(10);
        Label sliderLabel = new Label("Cities:");
        sliderLabel.setStyle("-fx-text-fill: white;");
        citiesSlider.setShowTickLabels(true);
        citiesSlider.setShowTickMarks(true);
        citiesSlider.setBlockIncrement(1);
        citiesSlider.setMajorTickUnit(5);
        citiesSlider.setMinorTickCount(4);
        citiesSlider.setSnapToTicks(true);
        newCitiesBtn.setStyle("-fx-background-color: #bd93f9; -fx-text-fill: white;");
        sliderBox.getChildren().addAll(sliderLabel, citiesSlider, newCitiesBtn);
        sliderBox.setAlignment(Pos.CENTER);
        sliderBox.setPadding(new Insets(5, 15, 5, 15));

        // Control buttons
        HBox controlButtons = new HBox(20);
        runRaceBtn.setStyle("-fx-background-color: #50fa7b; -fx-text-fill: #282a36; -fx-font-weight: bold;");
        resetBtn.setStyle("-fx-background-color: #ff5555; -fx-text-fill: white; -fx-font-weight: bold;");
        controlButtons.getChildren().addAll(runRaceBtn, resetBtn);
        controlButtons.setAlignment(Pos.CENTER);
        controlButtons.setPadding(new Insets(15));

        // Create the results container
        VBox resultsArea = new VBox(sliderBox, controlButtons, resultsGrid);
        return resultsArea;
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        label.setStyle("-fx-text-fill: white;");
        return label;
    }

    private Label createRowHeaderLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        label.setStyle("-fx-text-fill: #bbbbbb;");
        return label;
    }

    private void styleResultLabel(Label label) {
        label.setFont(Font.font("Monospace", 14));
        label.setStyle("-fx-text-fill: white;");
    }

    private void drawCities() {
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Cities: " + cityPoints.size(), 10, 20);

        for (int i = 0; i < cityPoints.size(); i++) {
            Point p = cityPoints.get(i);
            gc.setFill(Color.DODGERBLUE);
            gc.fillOval(p.x - 5, p.y - 5, 10, 10);
            gc.setFill(Color.WHITE);
            gc.fillText("C" + i, p.x + 5, p.y - 5);
        }
    }

    private void resetCanvas() {
        cityPoints.clear();
        setupCanvas();

        // Reset result labels
        antTimeLabel.setText("-");
        beeTimeLabel.setText("-");
        hybridTimeLabel.setText("-");
        antPathLabel.setText("-");
        beePathLabel.setText("-");
        hybridPathLabel.setText("-");
        antStatsLabel.setText("-");
        beeStatsLabel.setText("-");
        hybridStatsLabel.setText("-");
        antWinnerLabel.setText("");
        beeWinnerLabel.setText("");
        hybridWinnerLabel.setText("");
    }

    private void generateRandomCities(int count) {
        cityPoints.clear();

        // Generate cities with padding from edges
        int padding = 40;
        int availableWidth = canvasWidth - (2 * padding);
        int availableHeight = canvasHeight - (2 * padding);
        Random rand = new Random(System.currentTimeMillis());

        for (int i = 0; i < count; i++) {
            double x = padding + rand.nextDouble() * availableWidth;
            double y = padding + rand.nextDouble() * availableHeight;
            cityPoints.add(new Point(x, y));
        }

        drawCities();

        // Reset results
        antTimeLabel.setText("-");
        beeTimeLabel.setText("-");
        hybridTimeLabel.setText("-");
        antPathLabel.setText("-");
        beePathLabel.setText("-");
        hybridPathLabel.setText("-");
        antStatsLabel.setText("-");
        beeStatsLabel.setText("-");
        hybridStatsLabel.setText("-");
        antWinnerLabel.setText("");
        beeWinnerLabel.setText("");
        hybridWinnerLabel.setText("");
    }

    private void runRace() {

        int n = cityPoints.size();
        if (n < 3) {
            showAlert("Please place at least 3 cities.");
            return;
        }

        // Create distance matrix
        int[][] distances = new int[n][n];
        List<Integer> cities = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            cities.add(i);
            for (int j = 0; j < n; j++) {
                distances[i][j] = (int) cityPoints.get(i).distanceTo(cityPoints.get(j));
            }
        }

        // Run the algorithms
        // Ant Solver
        long startTime = System.currentTimeMillis();
        TspSolver antSolver = new SantSolver();
        antSolver.initialize(distances, cities);
        List<Integer> antPath = antSolver.solve();
        long endTime = System.currentTimeMillis();
        int antLen = calcLen(antPath, distances);
        antTimeLabel.setText((endTime - startTime) + "ms");
        antPathLabel.setText(formatPath(antPath));
        antStatsLabel.setText("Length: " + antLen + " (" + cityPoints.size() + " cities)");

        // Bee Solver
        startTime = System.currentTimeMillis();
        TspSolver beeSolver = new SbeeSolver();
        beeSolver.initialize(distances, cities);
        List<Integer> beePath = beeSolver.solve();
        endTime = System.currentTimeMillis();
        int beeLen = calcLen(beePath, distances);
        beeTimeLabel.setText((endTime - startTime) + "ms");
        beePathLabel.setText(formatPath(beePath));
        beeStatsLabel.setText("Length: " + beeLen + " (" + cityPoints.size() + " cities)");

        // Hybrid Solver
        startTime = System.currentTimeMillis();
        TspSolver hybridSolver = new HybridSolver();
        hybridSolver.initialize(distances, cities);
        List<Integer> hybridPath = hybridSolver.solve();
        endTime = System.currentTimeMillis();
        int hybridLen = calcLen(hybridPath, distances);
        hybridTimeLabel.setText((endTime - startTime) + "ms");
        hybridPathLabel.setText(formatPath(hybridPath));
        hybridStatsLabel.setText("Length: " + hybridLen + " (" + cityPoints.size() + " cities)");

        // Redraw everything with paths
        drawCities();
        drawPath(antPath, Color.CYAN);
        drawPath(beePath, Color.ORANGE);
        drawPath(hybridPath, Color.LIGHTGREEN);

        // Determine winner
        antWinnerLabel.setText("");
        beeWinnerLabel.setText("");
        hybridWinnerLabel.setText("");

        int minLen = Math.min(antLen, Math.min(beeLen, hybridLen));

        antWinnerLabel.setText(antLen == minLen ? "🏆" : "");
        beeWinnerLabel.setText(beeLen == minLen ? "🏆" : "");
        hybridWinnerLabel.setText(hybridLen == minLen ? "🏆" : "");
    }

    private String formatPath(List<Integer> path) {
        if (path.size() <= 5) {
            return path.toString();
        }
        // Just show first few and last city
        return "[" + path.get(0) + "," + path.get(1) + ",...," + path.get(path.size()-1) + "]";
    }

    private int calcLen(List<Integer> path, int[][] dist) {
        int len = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            len += dist[path.get(i)][path.get(i + 1)];
        }
        len += dist[path.get(path.size() - 1)][path.get(0)];
        return len;
    }

    private void drawPath(List<Integer> path, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(2);
        for (int i = 0; i < path.size() - 1; i++) {
            Point a = cityPoints.get(path.get(i));
            Point b = cityPoints.get(path.get(i + 1));
            gc.strokeLine(a.x, a.y, b.x, b.y);
        }
        // Close loop
        Point start = cityPoints.get(path.get(0));
        Point end = cityPoints.get(path.get(path.size() - 1));
        gc.strokeLine(end.x, end.y, start.x, start.y);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Helper class for points
    private static class Point {
        double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        double distanceTo(Point other) {
            double dx = x - other.x;
            double dy = y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }
}