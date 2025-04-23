package org.example.algoplay.games.tsp;

import javafx.animation.*;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PvMScreen extends Application {

    private final int canvasSize = 400; // Smaller individual canvases
    private final Canvas puzzleCanvas = new Canvas(canvasSize, canvasSize);
    private final Canvas solutionCanvas = new Canvas(canvasSize, canvasSize);

    private final GraphicsContext puzzleGc = puzzleCanvas.getGraphicsContext2D();
    private final GraphicsContext solutionGc = solutionCanvas.getGraphicsContext2D();

    private final Button nextPuzzleBtn = new Button("Next Puzzle");
    private final Button solveBtn = new Button("Solve");
    private final Button resetBtn = new Button("Reset");
    private final Button logOutBtn = new Button("Log Out");
    private final Slider difficultySetting = new Slider(5, 20, 9); // Min, Max, Default cities
    private final Label difficultyValue = new Label("9");

    private List<Point> cityPoints = new ArrayList<>();
    private final List<Integer> userPath = new ArrayList<>();

    // Results table components
    private final Label antTimeLabel = new Label("-");
    private final Label beeTimeLabel = new Label("-");
    private final Label hybridTimeLabel = new Label("-");
    private final Label userTimeLabel = new Label("-");

    private final Label antPathLabel = new Label("-");
    private final Label beePathLabel = new Label("-");
    private final Label hybridPathLabel = new Label("-");
    private final Label userPathLabel = new Label("-");

    private final Label antStatsLabel = new Label("-");
    private final Label beeStatsLabel = new Label("-");
    private final Label hybridStatsLabel = new Label("-");
    private final Label userStatsLabel = new Label("-");

    private final Label antWinnerLabel = new Label("");
    private final Label beeWinnerLabel = new Label("");
    private final Label hybridWinnerLabel = new Label("");
    private final Label userWinnerLabel = new Label("");

    // Progress indicators for solving algorithms
    private final ProgressIndicator antProgress = new ProgressIndicator();
    private final ProgressIndicator beeProgress = new ProgressIndicator();
    private final ProgressIndicator hybridProgress = new ProgressIndicator();

    // Timer for user solving
    private Timeline userTimer;
    private long startTime;
    private final Label timerLabel = new Label("00:00");

    // Tutorial state
    private boolean showingTutorial = false;

    // Add these fields to the PvMScreen class
    // Store solution paths for all algorithms
    private List<Integer> antSolutionPath = null;
    private List<Integer> beeSolutionPath = null;
    private List<Integer> hybridSolutionPath = null;

    // Store colors for each solution
    private Color antSolutionColor = Color.CYAN;
    private Color beeSolutionColor = Color.ORANGE;
    private Color hybridSolutionColor = Color.MAGENTA;
    //Storing User Scores
    private UserScoring userScoring = new UserScoring();


    // Animation timelines
    private Timeline cityBlinkTimeline;
    private Timeline pathAnimationTimeline;



    @Override
    public void start(Stage stage) {
        stage.setTitle("AlgoPlay: TSP Challenge");

        try {
            // Try to set application icon
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/tsp/icon.png")));
        } catch (Exception e) {
            System.out.println("Warning: Could not load application icon");
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a1a;");

        // Create a ScrollPane to make everything scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(root);
        scrollPane.setStyle("-fx-background: #1a1a1a; -fx-background-color: #1a1a1a;");

        // Make canvases responsive
        puzzleCanvas.widthProperty().bind(
                scrollPane.widthProperty().multiply(0.4).subtract(30));
        puzzleCanvas.heightProperty().bind(
                puzzleCanvas.widthProperty());

        solutionCanvas.widthProperty().bind(
                scrollPane.widthProperty().multiply(0.4).subtract(30));
        solutionCanvas.heightProperty().bind(
                solutionCanvas.widthProperty());

        // Header section
        HBox header = createHeader();
        header.getChildren().add(userScoring.getScoreContainer());
        root.setTop(header);

        // Main content with two canvases side by side
        HBox canvasesContainer = new HBox(20);
        canvasesContainer.setAlignment(Pos.CENTER);
        canvasesContainer.setPadding(new Insets(20));

        // Left canvas - The Puzzle
        VBox leftSection = new VBox(10);
        leftSection.setAlignment(Pos.CENTER);
        Label puzzleTitle = new Label("The Puzzle");
        puzzleTitle.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 18px;");

        // Add a drop shadow effect to the canvas
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        puzzleCanvas.setEffect(shadow);
        puzzleCanvas.setStyle("-fx-border-color: #333333; -fx-border-radius: 5;");

        leftSection.getChildren().addAll(puzzleTitle, puzzleCanvas);

        // Right canvas - User Solution
        VBox rightSection = new VBox(10);
        rightSection.setAlignment(Pos.CENTER);

        HBox solutionHeader = new HBox(10);
        solutionHeader.setAlignment(Pos.CENTER);
        Label solutionTitle = new Label("Your Solution");
        solutionTitle.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 18px;");

        timerLabel.setStyle("-fx-text-fill: #56eb61; -fx-font-size: 18px;");
        solutionHeader.getChildren().addAll(solutionTitle, timerLabel);

        Label instructionsLabel = new Label("Click cities in the order you would visit them");
        instructionsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");

        // Do this:
        DropShadow newShadow = new DropShadow();
        newShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        newShadow.setRadius(10);
        puzzleCanvas.setEffect(newShadow);
        solutionCanvas.setStyle("-fx-border-color: #333333; -fx-border-radius: 5;");

        rightSection.getChildren().addAll(solutionHeader, instructionsLabel, solutionCanvas);

        canvasesContainer.getChildren().addAll(leftSection, rightSection);
        root.setCenter(canvasesContainer);

        // Results section at bottom
        VBox resultsSection = createResultsSection();
        root.setBottom(resultsSection);

        // Setup canvas event listeners
        solutionCanvas.setOnMouseClicked(e -> {
            if (showingTutorial) {

                return;
            }

            // Start timer on first click
            if (userPath.isEmpty() && userTimer == null) {
                startUserTimer();
            }

            for (int i = 0; i < cityPoints.size(); i++) {
                Point p = cityPoints.get(i);
                if (p.isNear(e.getX(), e.getY())) {
                    if (userPath.contains(i)) {
                        // If already in path, remove this city and all after it
                        int index = userPath.indexOf(i);
                        for (int j = userPath.size() - 1; j >= index; j--) {
                            userPath.remove(j);
                        }
                    } else {
                        userPath.add(i);
                        // Add animation for the new addition
                        animateNewPathSegment(userPath.size() - 1);
                    }

                    drawCities();
                    redrawCitiesAndPath();
                    updateUserStats();

                    // Check if all cities have been visited
                    if (userPath.size() == cityPoints.size()) {
                        // Auto-complete the circuit by showing the path back to start
                        drawReturnPath();
                        solveBtn.setDisable(false);

                        // Stop the timer
                        if (userTimer != null) {
                            userTimer.stop();
                        }
                    } else {
                        solveBtn.setDisable(true);
                    }
                    break;
                }
            }
        });

        // Hover effects for canvas
        solutionCanvas.setOnMouseMoved(e -> {
            if (showingTutorial) return;

            drawCities(); // Redraw to remove previous hover effects
            drawUserPath();

            // Check if mouse is over any city
            for (int i = 0; i < cityPoints.size(); i++) {
                Point p = cityPoints.get(i);
                if (p.isNear(e.getX(), e.getY())) {
                    highlightCity(solutionGc, p, userPath.contains(i));
                    break;
                }
            }

            // Highlight next city to be visited with pulsing effect
            if (!userPath.isEmpty() && userPath.size() < cityPoints.size()) {
                Point lastPoint = cityPoints.get(userPath.get(userPath.size() - 1));
                solutionGc.setStroke(Color.WHITE);
                solutionGc.setLineDashes(5, 5);
                solutionGc.setGlobalAlpha(0.5);

                for (int i = 0; i < cityPoints.size(); i++) {
                    if (!userPath.contains(i)) {
                        Point nextPossible = cityPoints.get(i);
                        solutionGc.strokeLine(lastPoint.x, lastPoint.y, nextPossible.x, nextPossible.y);
                    }
                }

                solutionGc.setLineDashes();
                solutionGc.setGlobalAlpha(1.0);
            }
        });

        // Button event handlers
        resetBtn.setOnAction(e -> {
            userPath.clear();
            antSolutionPath = null;
            beeSolutionPath= null;
            hybridSolutionPath = null;// Clear the stored ant solution
            drawCities();
            resetResults();

            // Reset timer
            if (userTimer != null) {
                userTimer.stop();
                userTimer = null;
            }
            timerLabel.setText("00:00");

            solveBtn.setDisable(true);
        });

        solveBtn.setOnAction(e -> handleSolve());
        solveBtn.setDisable(true);

        nextPuzzleBtn.setOnAction(e -> {
            // Stop any running animations
            if (cityBlinkTimeline != null) {
                cityBlinkTimeline.stop();
            }
            if (pathAnimationTimeline != null) {
                pathAnimationTimeline.stop();
            }

            // Reset timer
            if (userTimer != null) {
                userTimer.stop();
                userTimer = null;
            }

            loadPuzzle();
        });

        // Set up difficulty slider
        difficultySetting.setMajorTickUnit(5);
        difficultySetting.setMinorTickCount(4);
        difficultySetting.setSnapToTicks(true);
        difficultySetting.setShowTickMarks(true);
        difficultySetting.setShowTickLabels(true);
        difficultySetting.valueProperty().addListener((obs, oldVal, newVal) -> {
            int citiesCount = newVal.intValue();
            difficultyValue.setText(String.valueOf(citiesCount));
        });

        // Setup progress indicators
        antProgress.setVisible(false);
        beeProgress.setVisible(false);
        hybridProgress.setVisible(false);

        // Set up tutorial steps

        loadPuzzle();

        // Create the scene with scrollPane instead of root
        Scene scene = new Scene(scrollPane, 1200, 800);

        // Add stylesheets
        URL css = getClass().getResource("/tsp/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("⚠️ Warning: styles.css not found. Applying inline styles instead.");
            applyInlineStyles();
        }

        stage.setScene(scene);
        stage.show();


    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setStyle("-fx-background-color: #121212;");

        Label title = new Label("AlgoPlay");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #56eb61;"); // Green color

        Label subtitle = new Label("Traveling Salesman - Player vs Machine");
        subtitle.setFont(Font.font("Monospace", FontWeight.NORMAL, 20));
        subtitle.setStyle("-fx-text-fill: #56eb61;"); // Green color

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Difficulty controls
        HBox difficultyControls = new HBox(10);
        difficultyControls.setAlignment(Pos.CENTER);
        Label difficultyLabel = new Label("Cities:");
        difficultyLabel.setStyle("-fx-text-fill: white;");
        difficultyValue.setStyle("-fx-text-fill: #56eb61; -fx-min-width: 30px;");
        difficultySetting.setPrefWidth(150);

        difficultyControls.getChildren().addAll(difficultyLabel, difficultySetting, difficultyValue);

        logOutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #56eb61; -fx-border-color: #56eb61; -fx-border-radius: 5;");

        header.getChildren().addAll(title, subtitle, spacer, difficultyControls, logOutBtn);
        return header;
    }

    private VBox createResultsSection() {
        VBox resultsContainer = new VBox(10);
        resultsContainer.setPadding(new Insets(20));
        resultsContainer.setStyle("-fx-background-color: #121212; -fx-border-color: #333333; -fx-border-width: 1 0 0 0;");

        // Results table with columns for each solver
        GridPane resultsTable = new GridPane();
        resultsTable.setHgap(50);
        resultsTable.setVgap(10);
        resultsTable.setPadding(new Insets(20));
        resultsTable.setAlignment(Pos.CENTER);


        // Buttons for actions with improved styling
        HBox actionsBox = new HBox(20);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(10));

        nextPuzzleBtn.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;");
        solveBtn.setStyle("-fx-background-color: #56eb61; -fx-text-fill: black; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 5; -fx-font-weight: bold;");
        resetBtn.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;");

        // Add hover effects
        nextPuzzleBtn.setOnMouseEntered(e -> nextPuzzleBtn.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;"));
        nextPuzzleBtn.setOnMouseExited(e -> nextPuzzleBtn.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;"));

        solveBtn.setOnMouseEntered(e -> solveBtn.setStyle("-fx-background-color: #7dff85; -fx-text-fill: black; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 5; -fx-font-weight: bold;"));
        solveBtn.setOnMouseExited(e -> solveBtn.setStyle("-fx-background-color: #56eb61; -fx-text-fill: black; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 5; -fx-font-weight: bold;"));

        resetBtn.setOnMouseEntered(e -> resetBtn.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;"));
        resetBtn.setOnMouseExited(e -> resetBtn.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;"));

        actionsBox.getChildren().addAll(nextPuzzleBtn, solveBtn, resetBtn);

        resultsContainer.getChildren().addAll(resultsTable, actionsBox);
        return resultsContainer;
    }

    private void loadPuzzle() {


        // Reset all solution paths
        antSolutionPath = null;
        beeSolutionPath = null;
        hybridSolutionPath = null;

        userPath.clear();
        resetResults();

        // Get number of cities from slider
        int numCities = (int) difficultySetting.getValue();
        // Reset the score for the new puzzle
        userScoring.resetScore();

        // Create evenly distributed city points that will work on both canvases
        cityPoints = generateRandomCities(numCities);

        drawCities();

        // Reset timer
        timerLabel.setText("00:00");
        if (userTimer != null) {
            userTimer.stop();
            userTimer = null;
        }

        solveBtn.setDisable(true);

        // If showing tutorial, don't do animations yet
        if (!showingTutorial) {
            // Animate cities appearing
            animateCitiesAppearing();
        }
    }

    private List<Point> generateRandomCities(int count) {
        List<Point> points = new ArrayList<>();
        Random rand = new Random(System.currentTimeMillis());

        // Generate points with padding from edges
        int padding = 50;
        int availableSpace = canvasSize - (2 * padding);

        // First attempt for more evenly distributed cities
        while (points.size() < count) {
            double x = padding + rand.nextDouble() * availableSpace;
            double y = padding + rand.nextDouble() * availableSpace;
            Point newPoint = new Point(x, y);

            // Check if the point is not too close to existing points
            boolean tooClose = false;
            for (Point p : points) {
                if (p.distanceTo(newPoint) < 30) { // Minimum distance between cities
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                points.add(newPoint);
            }
        }

        return points;
    }

    // Modify the drawCities method to redraw the solution if it exists
    private void drawCities() {
        // Clear both canvases
        puzzleGc.setFill(Color.web("#1e1e1e"));
        puzzleGc.fillRect(0, 0, canvasSize, canvasSize);

        solutionGc.setFill(Color.web("#1e1e1e"));
        solutionGc.fillRect(0, 0, canvasSize, canvasSize);

        // Draw a grid for better spatial reference
        drawGrid(puzzleGc);
        drawGrid(solutionGc);

        // Draw cities on both canvases
        for (int i = 0; i < cityPoints.size(); i++) {
            Point p = cityPoints.get(i);

            // Draw on puzzle canvas (left)
            drawCity(puzzleGc, p, i, false);

            // Draw on solution canvas (right)
            int indexInPath = userPath.indexOf(i);
            drawCity(solutionGc, p, i, indexInPath >= 0);

            if (indexInPath >= 0) {
                // Add number indicators
                solutionGc.setFill(Color.BLACK);
                solutionGc.setTextAlign(TextAlignment.CENTER);
                solutionGc.fillText(String.valueOf(indexInPath + 1), p.x, p.y + 4);
            }
        }

        redrawCitiesAndPath();

        // Redraw all solution paths if they exist
        if (antSolutionPath != null && !antSolutionPath.isEmpty()) {
            redrawSolutionPath(antSolutionPath, antSolutionColor);
        }
        if (beeSolutionPath != null && !beeSolutionPath.isEmpty()) {
            redrawSolutionPath(beeSolutionPath, beeSolutionColor);
        }
        if (hybridSolutionPath != null && !hybridSolutionPath.isEmpty()) {
            redrawSolutionPath(hybridSolutionPath, hybridSolutionColor);
        }
    }
    private void redrawSolutionPath(List<Integer> path, Color color) {
        if (path == null || path.isEmpty()) return;

        puzzleGc.setStroke(color);
        puzzleGc.setLineWidth(2);

        // Draw all path segments
        for (int i = 0; i < path.size() - 1; i++) {
            Point a = cityPoints.get(path.get(i));
            Point b = cityPoints.get(path.get(i + 1));
            puzzleGc.strokeLine(a.x, a.y, b.x, b.y);

            // Draw arrow to show direction
            drawArrow(puzzleGc, a.x, a.y, b.x, b.y);
        }

        // Complete the cycle - draw line back to start
        if (path.size() > 1) {
            Point start = cityPoints.get(path.get(0));
            Point end = cityPoints.get(path.get(path.size() - 1));
            puzzleGc.strokeLine(end.x, end.y, start.x, start.y);
            drawArrow(puzzleGc, end.x, end.y, start.x, start.y);
        }
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(0.5);

        int gridSize = 40; // Size of grid cells

        // Draw horizontal lines
        for (int y = 0; y <= canvasSize; y += gridSize) {
            gc.strokeLine(0, y, canvasSize, y);
        }

        // Draw vertical lines
        for (int x = 0; x <= canvasSize; x += gridSize) {
            gc.strokeLine(x, 0, x, canvasSize);
        }
    }

    private void drawCity(GraphicsContext gc, Point p, int index, boolean visited) {
        // Draw a shadow for 3D effect
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillOval(p.x - 4, p.y - 2, 12, 12);

        // Draw the city
        if (visited) {
            // Visited cities are highlighted
            gc.setFill(Color.ORANGE);
            gc.fillOval(p.x - 5, p.y - 5, 14, 14);

            // Add a stroke
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.5);
            gc.strokeOval(p.x - 5, p.y - 5, 14, 14);
        } else {
            // Unvisited cities
            gc.setFill(Color.WHITE);
            gc.fillOval(p.x - 5, p.y - 5, 10, 10);
        }

        // Draw small index label for reference
        gc.setFill(Color.LIGHTGRAY);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(String.valueOf(index), p.x + 8, p.y - 8);
    }

    private void highlightCity(GraphicsContext gc, Point p, boolean visited) {
        if (visited) {
            // Already visited - highlight with visited color
            gc.setFill(Color.web("#ffa500", 0.6)); // Semi-transparent orange
            gc.fillOval(p.x - 12, p.y - 12, 24, 24);
        } else {
            // Not visited yet - highlight with hover color
            gc.setFill(Color.web("#ffffff", 0.3)); // Semi-transparent white
            gc.fillOval(p.x - 12, p.y - 12, 24, 24);
        }
    }

    private void redrawCitiesAndPath() {
        // Clear previous path
        solutionGc.clearRect(0, 0, canvasSize, canvasSize);
        drawGrid(solutionGc);

        // Redraw cities
        for (int i = 0; i < cityPoints.size(); i++) {
            Point p = cityPoints.get(i);
            int indexInPath = userPath.indexOf(i);
            drawCity(solutionGc, p, i, indexInPath >= 0);

            if (indexInPath >= 0) {
                // Add number indicators
                solutionGc.setFill(Color.BLACK);
                solutionGc.setTextAlign(TextAlignment.CENTER);
                solutionGc.fillText(String.valueOf(indexInPath + 1), p.x, p.y + 4);
            }
        }

        drawUserPath();
    }

    private void drawUserPath() {
        solutionGc.setStroke(Color.ORANGE);
        solutionGc.setLineWidth(3);

        for (int i = 0; i < userPath.size() - 1; i++) {
            Point a = cityPoints.get(userPath.get(i));
            Point b = cityPoints.get(userPath.get(i + 1));
            solutionGc.strokeLine(a.x, a.y, b.x, b.y);

            // Draw arrow to show direction
            drawArrow(solutionGc, a.x, a.y, b.x, b.y);
        }
    }

    private void drawReturnPath() {
        if (userPath.size() == cityPoints.size()) {
            Point start = cityPoints.get(userPath.get(0));
            Point end = cityPoints.get(userPath.get(userPath.size() - 1));

            // Draw dashed line back to start
            solutionGc.setStroke(Color.web("#56eb61", 0.8)); // Semi-transparent green
            solutionGc.setLineWidth(2);
            solutionGc.setLineDashes(5, 5);
            solutionGc.strokeLine(end.x, end.y, start.x, start.y);
            solutionGc.setLineDashes(null);

            // Draw arrow for return path
            drawArrow(solutionGc, end.x, end.y, start.x, start.y);
        }
    }

    private void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        // Calculate direction vector
        double dx = endX - startX;
        double dy = endY - startY;

        // Normalize and scale to arrow size
        double length = Math.sqrt(dx * dx + dy * dy);
        double arrowSize = 7;

        // Only draw arrow if there's sufficient distance
        if (length > 20) {
            double unitX = dx / length;
            double unitY = dy / length;

            // Position the arrow near the end point but not exactly at it
            double arrowX = endX - unitX * 12;
            double arrowY = endY - unitY * 12;

            // Calculate perpendicular vector for arrow head
            double perpX = -unitY;
            double perpY = unitX;

            // Draw arrowhead
            double[] xPoints = {
                    arrowX,
                    arrowX - unitX * arrowSize + perpX * arrowSize/2,
                    arrowX - unitX * arrowSize - perpX * arrowSize/2
            };

            double[] yPoints = {
                    arrowY,
                    arrowY - unitY * arrowSize + perpY * arrowSize/2,
                    arrowY - unitY * arrowSize - perpY * arrowSize/2
            };

            gc.setFill(Color.ORANGE);
            gc.fillPolygon(xPoints, yPoints, 3);
        }
    }

    private void updateUserStats() {
        if (userPath.size() > 1) {
            int[][] dist = createDistanceMatrix(cityPoints);
            int pathLength = calcLen(userPath, dist);
            userPathLabel.setText(formatPath(userPath));

            double completion = (double)userPath.size() / cityPoints.size() * 100;
            DecimalFormat df = new DecimalFormat("#.0");
            userStatsLabel.setText("Length: " + pathLength + " (" + df.format(completion) + "% complete)");
        } else {
            userPathLabel.setText("-");
            userStatsLabel.setText("Click cities to begin");
        }
    }

    private String formatPath(List<Integer> path) {
        if (path.size() <= 5) {
            return path.toString();
        }
        // Just show first few and last city
        return "[" + path.get(0) + "," + path.get(1) + ",...," + path.get(path.size()-1) + "]";
    }

    private void handleSolve() {
        if (userPath.size() != cityPoints.size()) {
            // Show alert if user hasn't completed their path
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Path");
            alert.setHeaderText("Please visit all cities");
            alert.setContentText("You need to click all cities before solving.");
            alert.showAndWait();
            return;
        }

        int[][] dist = createDistanceMatrix(cityPoints);
        List<Integer> cities = new ArrayList<>();
        for (int i = 0; i < cityPoints.size(); i++) cities.add(i);

        // Calculate time taken
        long userTimeMs = System.currentTimeMillis() - startTime;

        // Store user time for display
        userTimeLabel.setText((userTimeMs / 1000.0) + "s");

        // Disable buttons during solving
        solveBtn.setDisable(true);
        nextPuzzleBtn.setDisable(true);
        resetBtn.setDisable(true);

        // Show progress indicators
        antProgress.setVisible(true);
        beeProgress.setVisible(true);
        hybridProgress.setVisible(true);

        // Run the solvers in background threads
        Task<List<Object>> solverTask = new Task<>() {
            @Override
            protected List<Object> call() {
                List<Object> results = new ArrayList<>();

                // Ant Colony Solver
                TspSolver antSolver = new SantSolver();
                antSolver.initialize(dist, cities);
                long antStartTime = System.currentTimeMillis();
                List<Integer> antPath = antSolver.solve();
                long antTime = System.currentTimeMillis() - antStartTime;
                int antLen = calcLen(antPath, dist);
                results.add(antPath);
                results.add(antTime);
                results.add(antLen);

                // Bee Colony Solver
                TspSolver beeSolver = new SbeeSolver();
                beeSolver.initialize(dist, cities);
                long beeStartTime = System.currentTimeMillis();
                List<Integer> beePath = beeSolver.solve();
                long beeTime = System.currentTimeMillis() - beeStartTime;
                int beeLen = calcLen(beePath, dist);
                results.add(beePath);
                results.add(beeTime);
                results.add(beeLen);

                // Hybrid Solver
                TspSolver hybridSolver = new HybridSolver();
                hybridSolver.initialize(dist, cities);
                long hybridStartTime = System.currentTimeMillis();
                List<Integer> hybridPath = hybridSolver.solve();
                long hybridTime = System.currentTimeMillis() - hybridStartTime;
                int hybridLen = calcLen(hybridPath, dist);
                results.add(hybridPath);
                results.add(hybridTime);
                results.add(hybridLen);

                return results;
            }
        };

        solverTask.setOnSucceeded(event -> {
            // Hide progress indicators
            antProgress.setVisible(false);
            beeProgress.setVisible(false);
            hybridProgress.setVisible(false);

            List<Object> results = solverTask.getValue();

            // Extract results
            List<Integer> antPath = (List<Integer>) results.get(0);
            long antTime = (long) results.get(1);
            int antLen = (int) results.get(2);

            List<Integer> beePath = (List<Integer>) results.get(3);
            long beeTime = (long) results.get(4);
            int beeLen = (int) results.get(5);

            List<Integer> hybridPath = (List<Integer>) results.get(6);
            long hybridTime = (long) results.get(7);
            int hybridLen = (int) results.get(8);

            // Calculate user path length
            int userLen = calcLen(userPath, dist);

            // Update the results table for consistency
            antTimeLabel.setText(antTime + "ms");
            antPathLabel.setText(formatPath(antPath));
            antStatsLabel.setText("Length: " + antLen + " (" + cityPoints.size() + " cities)");

            beeTimeLabel.setText(beeTime + "ms");
            beePathLabel.setText(formatPath(beePath));
            beeStatsLabel.setText("Length: " + beeLen + " (" + cityPoints.size() + " cities)");

            hybridTimeLabel.setText(hybridTime + "ms");
            hybridPathLabel.setText(formatPath(hybridPath));
            hybridStatsLabel.setText("Length: " + hybridLen + " (" + cityPoints.size() + " cities)");

            userPathLabel.setText(formatPath(userPath));
            userStatsLabel.setText("Length: " + userLen + " (" + cityPoints.size() + " cities)");

            // Store ant solution for redrawing
            antSolutionPath = new ArrayList<>(antPath);

            // Store bee solution for redrawing (add this where you calculate bee solutions)
            beeSolutionPath = new ArrayList<>(beePath);

            // Store hybrid solution for redrawing (add this where you calculate hybrid solutions)
            hybridSolutionPath = new ArrayList<>(hybridPath);

            // Calculate scores for all before showing results screen
            int minAlgoLen = Math.min(Math.min(antLen, beeLen), hybridLen);
            userScoring.calculateScore(userPath, antPath, dist, userTimeMs, cityPoints.size());

// Calculate scores for the algorithms
            userScoring.calculateAlgorithmScore("Ant", antPath, antPath, dist);  // Using antPath as both algorithm path and reference
            userScoring.calculateAlgorithmScore("Bee", beePath, antPath, dist);
            userScoring.calculateAlgorithmScore("Hybrid", hybridPath, antPath, dist);

            int userScore = userScoring.getCurrentScore();
            Label scoreLabel = new Label("Your Score: " + userScore);
            scoreLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 30));
            scoreLabel.setStyle("-fx-text-fill: #56eb61;");

// You could also add labels for algorithm scores if desired
            int antScore = userScoring.getAlgorithmScore("Ant");
            int beeScore = userScoring.getAlgorithmScore("Bee");
            int hybridScore = userScoring.getAlgorithmScore("Hybrid");

            Label algorithmScoresLabel = new Label(
                    "Algorithm Scores - Ant: " + antScore +
                            ", Bee: " + beeScore +
                            ", Hybrid: " + hybridScore
            );
            algorithmScoresLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 16));
            algorithmScoresLabel.setStyle("-fx-text-fill: #aaaaaa;");

// Get the best performer
            String bestPerformer = userScoring.getBestPerformer();
            Label bestPerformerLabel = new Label("Best performer: " + bestPerformer);
            bestPerformerLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
            bestPerformerLabel.setStyle("-fx-text-fill: #f4d742;");

            // Show the results screen
            showResultsScreen(antPath, beePath, hybridPath,
                    antTime, beeTime, hybridTime, userTimeMs,
                    antLen, beeLen, hybridLen, userLen);

            // Re-enable buttons
            solveBtn.setDisable(false);
            nextPuzzleBtn.setDisable(false);
            resetBtn.setDisable(false);
        });

        new Thread(solverTask).start();
    }

    private void solveWithAnimation(int[][] dist, List<Integer> cities, long userTimeMs) {
        // Disable buttons during solving
        solveBtn.setDisable(true);
        nextPuzzleBtn.setDisable(true);
        resetBtn.setDisable(true);

        // Show progress indicators
        antProgress.setVisible(true);
        beeProgress.setVisible(true);
        hybridProgress.setVisible(true);

        // Use JavaFX Task API for background processing
        Timeline solveTimeline = new Timeline();

        // First keyframe - start Ant Colony
        KeyFrame startAnt = new KeyFrame(Duration.ZERO, event -> {
            antTimeLabel.setText("Running...");
            antProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });

        // After 100ms - start Bee Colony
        KeyFrame startBee = new KeyFrame(Duration.millis(100), event -> {
            beeTimeLabel.setText("Running...");
            beeProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });

        // After 200ms - start Hybrid
        KeyFrame startHybrid = new KeyFrame(Duration.millis(200), event -> {
            hybridTimeLabel.setText("Running...");
            hybridProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });

        // After 500ms - solve with Ant Colony
        KeyFrame solveAnt = new KeyFrame(Duration.millis(500), event -> {
            TspSolver antSolver = new SantSolver();
            antSolver.initialize(dist, cities);

            long startTime = System.currentTimeMillis();
            List<Integer> antPath = antSolver.solve();
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            int antLen = calcLen(antPath, dist);
            antTimeLabel.setText(elapsedTime + "ms");
            antPathLabel.setText(formatPath(antPath));
            antStatsLabel.setText("Length: " + antLen + " (" + cityPoints.size() + " cities)");

            // Draw ant solution on left canvas with animation
            animatePath(puzzleGc, antPath, Color.CYAN);

            antProgress.setVisible(false);
        });

        // After 1000ms - solve with Bee Colony
        KeyFrame solveBee = new KeyFrame(Duration.millis(1000), event -> {
            TspSolver beeSolver = new SbeeSolver();
            beeSolver.initialize(dist, cities);

            long startTime = System.currentTimeMillis();
            List<Integer> beePath = beeSolver.solve();
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            int beeLen = calcLen(beePath, dist);
            beeTimeLabel.setText(elapsedTime + "ms");
            beePathLabel.setText(formatPath(beePath));
            beeStatsLabel.setText("Length: " + beeLen + " (" + cityPoints.size() + " cities)");

            beeProgress.setVisible(false);
        });

        // After 1500ms - solve with Hybrid
        KeyFrame solveHybrid = new KeyFrame(Duration.millis(1500), event -> {
            TspSolver hybridSolver = new HybridSolver();
            hybridSolver.initialize(dist, cities);

            long startTime = System.currentTimeMillis();
            List<Integer> hybridPath = hybridSolver.solve();
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            int hybridLen = calcLen(hybridPath, dist);
            hybridTimeLabel.setText(elapsedTime + "ms");
            hybridPathLabel.setText(formatPath(hybridPath));
            hybridStatsLabel.setText("Length: " + hybridLen + " (" + cityPoints.size() + " cities)");

            hybridProgress.setVisible(false);
        });

        // After 2000ms - calculate winner and enable buttons
        KeyFrame calculateWinner = new KeyFrame(Duration.millis(2000), event -> {
            int antLen = extractLength(antStatsLabel.getText());
            int beeLen = extractLength(beeStatsLabel.getText());
            int hybridLen = extractLength(hybridStatsLabel.getText());
            int userLen = calcLen(userPath, dist);

            // User's stats
            userPathLabel.setText(formatPath(userPath));
            userStatsLabel.setText("Length: " + userLen + " (" + cityPoints.size() + " cities)");

            // Find the winner and the best algorithm result
            int minAlgoLen = Math.min(Math.min(antLen, beeLen), hybridLen);
            int minLen = Math.min(minAlgoLen, userLen);

            // Get the optimal path (just use ant path for now)
            List<Integer> optimalPath = antSolutionPath;

            // Calculate scores for all algorithms
            userScoring.calculateScore(userPath, optimalPath, dist, userTimeMs, cityPoints.size());

// Calculate scores for the algorithms
            userScoring.calculateAlgorithmScore("Ant", antSolutionPath, optimalPath, dist);
            userScoring.calculateAlgorithmScore("Bee", beeSolutionPath, optimalPath, dist);
            userScoring.calculateAlgorithmScore("Hybrid", hybridSolutionPath, optimalPath, dist);

// ...

            int userScore = userScoring.getCurrentScore();

            // Clear previous winner indicators
            antWinnerLabel.setText("");
            beeWinnerLabel.setText("");
            hybridWinnerLabel.setText("");
            userWinnerLabel.setText("");

            // Set winner indicators with animation
            if (antLen == minLen) {
                animateWinner(antWinnerLabel);
            }
            if (beeLen == minLen) {
                animateWinner(beeWinnerLabel);
            }
            if (hybridLen == minLen) {
                animateWinner(hybridWinnerLabel);
            }
            if (userLen == minLen) {
                animateWinner(userWinnerLabel);
            }

            // Re-enable buttons
            solveBtn.setDisable(false);
            nextPuzzleBtn.setDisable(false);
            resetBtn.setDisable(false);
        });

        solveTimeline.getKeyFrames().addAll(
                startAnt, startBee, startHybrid,
                solveAnt, solveBee, solveHybrid,
                calculateWinner
        );

        solveTimeline.play();
    }

    private int extractLength(String text) {
        if (text.contains("Length: ")) {
            String lengthPart = text.substring(text.indexOf("Length: ") + 8);
            lengthPart = lengthPart.substring(0, lengthPart.indexOf(" "));
            try {
                return Integer.parseInt(lengthPart);
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        }
        return Integer.MAX_VALUE;
    }

    private void animateWinner(Label winnerLabel) {
        winnerLabel.setText("🏆");

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(500), winnerLabel);
        scaleTransition.setFromX(0.5);
        scaleTransition.setFromY(0.5);
        scaleTransition.setToX(1.2);
        scaleTransition.setToY(1.2);
        scaleTransition.setCycleCount(4);
        scaleTransition.setAutoReverse(true);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), winnerLabel);
        fadeTransition.setFromValue(0.5);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(4);
        fadeTransition.setAutoReverse(true);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(scaleTransition, fadeTransition);
        parallelTransition.play();
    }

    

    private void animatePath(GraphicsContext gc, List<Integer> path, Color color) {

        // Store the solution path and color for later redraws
        if (gc == puzzleGc) {
            antSolutionPath = new ArrayList<>(path);
            antSolutionColor = color;
        }
        // Clear previous paths
        drawCities();

        final int[] currentSegment = {0};

        pathAnimationTimeline = new Timeline();
        pathAnimationTimeline.setCycleCount(path.size());

        KeyFrame keyFrame = new KeyFrame(Duration.millis(100), event -> {
            int i = currentSegment[0];

            // Draw this segment
            if (i < path.size() - 1) {
                Point a = cityPoints.get(path.get(i));
                Point b = cityPoints.get(path.get(i + 1));

                gc.setStroke(color);
                gc.setLineWidth(2);
                gc.strokeLine(a.x, a.y, b.x, b.y);

                // Draw arrow to show direction
                drawArrow(gc, a.x, a.y, b.x, b.y);
            } else if (i == path.size() - 1) {
                // Complete the cycle
                Point start = cityPoints.get(path.get(0));
                Point end = cityPoints.get(path.get(path.size() - 1));

                gc.setStroke(color);
                gc.setLineWidth(2);
                gc.strokeLine(end.x, end.y, start.x, start.y);

                // Draw arrow for return path
                drawArrow(gc, end.x, end.y, start.x, start.y);
            }

            // Highlight current city
            Point current = cityPoints.get(path.get(i));
            gc.setFill(color);
            gc.fillOval(current.x - 7, current.y - 7, 14, 14);

            currentSegment[0]++;
        });

        pathAnimationTimeline.getKeyFrames().add(keyFrame);
        pathAnimationTimeline.play();
    }

    private void animateCitiesAppearing() {
        if (cityBlinkTimeline != null) {
            cityBlinkTimeline.stop();
        }

        cityBlinkTimeline = new Timeline();
        cityBlinkTimeline.setCycleCount(1);

        for (int i = 0; i < cityPoints.size(); i++) {
            final int cityIndex = i;

            KeyFrame keyFrame = new KeyFrame(Duration.millis(50 * i), event -> {
                Point p = cityPoints.get(cityIndex);

                // Create a glow effect
                Glow glow = new Glow();
                glow.setLevel(0.8);

                // Flash animation for each city appearing
                FillTransition fillTransition = new FillTransition(Duration.millis(300));
                Circle cityCircle = new Circle(p.x, p.y, 10, Color.WHITE);
                cityCircle.setEffect(glow);

                fillTransition.setShape(cityCircle);
                fillTransition.setFromValue(Color.WHITE);
                fillTransition.setToValue(Color.web("#56eb61"));
                fillTransition.setCycleCount(2);
                fillTransition.setAutoReverse(true);
                fillTransition.play();

                // Draw the city
                puzzleGc.setFill(Color.WHITE);
                puzzleGc.fillOval(p.x - 5, p.y - 5, 10, 10);

                solutionGc.setFill(Color.WHITE);
                solutionGc.fillOval(p.x - 5, p.y - 5, 10, 10);

                // Add small index labels
                puzzleGc.setFill(Color.LIGHTGRAY);
                puzzleGc.fillText(String.valueOf(cityIndex), p.x + 8, p.y - 8);

                solutionGc.setFill(Color.LIGHTGRAY);
                solutionGc.fillText(String.valueOf(cityIndex), p.x + 8, p.y - 8);
            });

            cityBlinkTimeline.getKeyFrames().add(keyFrame);
        }

        cityBlinkTimeline.play();
    }

    private void animateNewPathSegment(int segmentIndex) {
        if (segmentIndex > 0) {
            Point prev = cityPoints.get(userPath.get(segmentIndex - 1));
            Point curr = cityPoints.get(userPath.get(segmentIndex));

            // Draw dashed preview line first
            double[] dashPattern = {5, 5};
            solutionGc.setLineDashes(dashPattern);
            solutionGc.setStroke(Color.web("#56eb61", 0.6));
            solutionGc.setLineWidth(2);
            solutionGc.strokeLine(prev.x, prev.y, curr.x, curr.y);
            solutionGc.setLineDashes(null);

            // Create animation to transition to solid line
            final double[] progress = {0.0};
            AnimationTimer lineAnimation = new AnimationTimer() {
                private long lastUpdate = 0;

                @Override
                public void handle(long now) {
                    if (lastUpdate == 0) {
                        lastUpdate = now;
                        return;
                    }

                    double dt = (now - lastUpdate) / 1_000_000_000.0; // Convert to seconds
                    lastUpdate = now;

                    progress[0] += dt * 2; // Speed of animation

                    if (progress[0] >= 1.0) {
                        stop();
                        // Final step - draw the permanent line
                        drawUserPath();
                    } else {
                        // Draw transitional line
                        solutionGc.clearRect(0, 0, canvasSize, canvasSize);
                        drawGrid(solutionGc);

                        // Redraw cities
                        for (int i = 0; i < cityPoints.size(); i++) {
                            Point p = cityPoints.get(i);
                            drawCity(solutionGc, p, i, userPath.contains(i));

                            int indexInPath = userPath.indexOf(i);
                            if (indexInPath >= 0) {
                                // Add number indicators
                                solutionGc.setFill(Color.BLACK);
                                solutionGc.setTextAlign(TextAlignment.CENTER);
                                solutionGc.fillText(String.valueOf(indexInPath + 1), p.x, p.y + 4);
                            }
                        }

                        // Draw existing paths
                        for (int i = 0; i < userPath.size() - 2; i++) {
                            Point a = cityPoints.get(userPath.get(i));
                            Point b = cityPoints.get(userPath.get(i + 1));
                            solutionGc.setStroke(Color.ORANGE);
                            solutionGc.setLineWidth(3);
                            solutionGc.strokeLine(a.x, a.y, b.x, b.y);
                            drawArrow(solutionGc, a.x, a.y, b.x, b.y);
                        }

                        // Draw animated segment
                        double dashSize = 10 * (1 - progress[0]);
                        if (dashSize > 0) {
                            double[] dashPattern = {dashSize, dashSize};
                            solutionGc.setLineDashes(dashPattern);
                        } else {
                            solutionGc.setLineDashes(null);
                        }

                        // Transition from green to orange
                        Color lineColor = interpolateColor(
                                Color.web("#56eb61"),
                                Color.ORANGE,
                                progress[0]
                        );

                        solutionGc.setStroke(lineColor);
                        solutionGc.setLineWidth(3);
                        solutionGc.strokeLine(prev.x, prev.y, curr.x, curr.y);

                        // Only draw arrow if sufficiently progressed
                        if (progress[0] > 0.5) {
                            drawArrow(solutionGc, prev.x, prev.y, curr.x, curr.y);
                        }

                        solutionGc.setLineDashes(null);
                    }
                }
            };

            lineAnimation.start();
        }
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        double r = c1.getRed() + (c2.getRed() - c1.getRed()) * t;
        double g = c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t;
        double b = c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t;
        double a = c1.getOpacity() + (c2.getOpacity() - c1.getOpacity()) * t;
        return new Color(r, g, b, a);
    }

    private void startUserTimer() {
        startTime = System.currentTimeMillis();

        userTimer = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long seconds = elapsedTime / 1000;
            long tenths = (elapsedTime % 1000) / 100;

            String timeText = String.format("%02d:%01d", seconds, tenths);
            timerLabel.setText(timeText);
        }));

        userTimer.setCycleCount(Timeline.INDEFINITE);
        userTimer.play();
    }

    private int[][] createDistanceMatrix(List<Point> points) {
        int n = points.size();
        int[][] dist = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                dist[i][j] = (int) points.get(i).distanceTo(points.get(j));
        return dist;
    }

    private int calcLen(List<Integer> path, int[][] dist) {
        int len = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            len += dist[path.get(i)][path.get(i + 1)];
        }
        len += dist[path.get(path.size() - 1)][path.get(0)];
        return len;
    }

    private void resetResults() {
        antTimeLabel.setText("-");
        beeTimeLabel.setText("-");
        hybridTimeLabel.setText("-");
        userTimeLabel.setText("-");

        antPathLabel.setText("-");
        beePathLabel.setText("-");
        hybridPathLabel.setText("-");
        userPathLabel.setText("-");

        antStatsLabel.setText("-");
        beeStatsLabel.setText("-");
        hybridStatsLabel.setText("-");
        userStatsLabel.setText("-");

        antWinnerLabel.setText("");
        beeWinnerLabel.setText("");
        hybridWinnerLabel.setText("");
        userWinnerLabel.setText("");

        // Reset the score
        userScoring.resetScore();

        // Hide progress indicators
        antProgress.setVisible(false);
        beeProgress.setVisible(false);
        hybridProgress.setVisible(false);
    }


    private void applyInlineStyles() {
        // Apply styles if CSS file is not found
        nextPuzzleBtn.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;");
        solveBtn.setStyle("-fx-background-color: #56eb61; -fx-text-fill: black; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 5; -fx-font-weight: bold;");
        resetBtn.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;");

        // Progress indicators
        antProgress.setStyle("-fx-progress-color: cyan;");
        beeProgress.setStyle("-fx-progress-color: orange;");
        hybridProgress.setStyle("-fx-progress-color: #56eb61;");

        // Make progress indicators smaller
        antProgress.setPrefSize(20, 20);
        beeProgress.setPrefSize(20, 20);
        hybridProgress.setPrefSize(20, 20);
    }

    public static void main(String[] args) {
        launch(args);
    }

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

        boolean isNear(double clickX, double clickY) {
            return Math.hypot(clickX - x, clickY - y) <= 10;
        }
    }
    // Add this method to the PvMScreen class

    private void showResultsScreen(List<Integer> antPath, List<Integer> beePath, List<Integer> hybridPath,
                                   long antTime, long beeTime, long hybridTime, long userTime,
                                   int antLength, int beeLength, int hybridLength, int userLength) {
        // Create the dialog
        Stage resultsStage = new Stage();
        resultsStage.setTitle("Solver Results");
        resultsStage.initModality(Modality.APPLICATION_MODAL);

        // Create main container
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 20;");
        root.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("Traveling Salesman Results");
        titleLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #56eb61;");

        // Create a container for all path visualizations
        HBox pathsContainer = new HBox(15);
        pathsContainer.setAlignment(Pos.CENTER);

        // Calculate the winner
        int minLength = Math.min(Math.min(Math.min(antLength, beeLength), hybridLength), userLength);

        // Create solution canvases
        VBox userSolution = createSolutionPanel("Your Solution", userPath, userLength, userTime,
                Color.ORANGE, minLength == userLength);
        VBox antSolution = createSolutionPanel("Ant Colony", antPath, antLength, antTime,
                Color.CYAN, minLength == antLength);
        VBox beeSolution = createSolutionPanel("Bee Colony", beePath, beeLength, beeTime,
                Color.YELLOW, minLength == beeLength);
        VBox hybridSolution = createSolutionPanel("Hybrid Solution", hybridPath, hybridLength, hybridTime,
                Color.LIMEGREEN, minLength == hybridLength);

        pathsContainer.getChildren().addAll(userSolution, antSolution, beeSolution, hybridSolution);

        // User score display
        VBox scoreDisplay = new VBox(10);
        scoreDisplay.setAlignment(Pos.CENTER);
        scoreDisplay.setPadding(new Insets(20));
        scoreDisplay.setStyle("-fx-background-color: #222; -fx-background-radius: 10;");

        int userScore = userScoring.getCurrentScore();
        Label scoreLabel = new Label("Your Score: " + userScore);
        scoreLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 30));
        scoreLabel.setStyle("-fx-text-fill: #56eb61;");

        // Add efficiency comparison
        double efficiencyRatio = (double)minLength / userLength * 100;
        DecimalFormat df = new DecimalFormat("#.1f");
        String efficiencyText = "Your solution is " + df.format(efficiencyRatio) + "% efficient";
        if (minLength == userLength) {
            efficiencyText = "Your solution is optimal! 100% efficiency";
        }

        Label efficiencyLabel = new Label(efficiencyText);
        efficiencyLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 16));
        efficiencyLabel.setStyle("-fx-text-fill: white;");

        scoreDisplay.getChildren().addAll(scoreLabel, efficiencyLabel);

        // Feedback message based on performance
        Label feedbackLabel = new Label();
        feedbackLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 16));
        feedbackLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-style: italic;");

        if (minLength == userLength) {
            feedbackLabel.setText("Outstanding! You found the optimal path!");
            feedbackLabel.setStyle("-fx-text-fill: gold; -fx-font-style: italic;");
        } else if (efficiencyRatio >= 95) {
            feedbackLabel.setText("Excellent! Nearly optimal solution!");
            feedbackLabel.setStyle("-fx-text-fill: #56eb61; -fx-font-style: italic;");
        } else if (efficiencyRatio >= 85) {
            feedbackLabel.setText("Good job! Your solution is very efficient.");
        } else if (efficiencyRatio >= 75) {
            feedbackLabel.setText("Not bad! Try to find shorter connections.");
        } else {
            feedbackLabel.setText("Keep practicing! Look for shorter paths between cities.");
        }

        scoreDisplay.getChildren().add(feedbackLabel);

        // Close button
        Button closeButton = new Button("Continue");
        closeButton.setStyle("-fx-background-color: #56eb61; -fx-text-fill: black; -fx-font-size: 14px; " +
                "-fx-padding: 10 30; -fx-background-radius: 5; -fx-font-weight: bold;");
        closeButton.setOnAction(e -> resultsStage.close());

        // Create "Share Result" button
        Button shareButton = new Button("Share Result");
        shareButton.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 10 20; -fx-background-radius: 5;");
        shareButton.setOnAction(e -> {
            // Simulate sharing (could be implemented with actual sharing functionality)
            Alert shareAlert = new Alert(Alert.AlertType.INFORMATION);
            shareAlert.setTitle("Share Results");
            shareAlert.setHeaderText("Results Shared!");
            shareAlert.setContentText("Your score of " + userScore + " has been shared.");
            shareAlert.showAndWait();
        });

        // Button container
        HBox buttonContainer = new HBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(closeButton, shareButton);

        // Add all components to the root
        root.getChildren().addAll(titleLabel, pathsContainer, scoreDisplay, feedbackLabel, buttonContainer);

        // Set up the scene
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1a1a1a; -fx-background-color: #1a1a1a;");

        Scene scene = new Scene(scrollPane, 900, 650);

        // Add stylesheet if available
        URL css = getClass().getResource("/tsp/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        resultsStage.setScene(scene);
        resultsStage.show();

        // Add subtle animation to the score
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), scoreLabel);
        fadeIn.setFromValue(0.2);
        fadeIn.setToValue(1.0);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(1000), scoreLabel);
        scaleUp.setFromX(0.8);
        scaleUp.setFromY(0.8);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        ParallelTransition scoreAnimation = new ParallelTransition(fadeIn, scaleUp);
        scoreAnimation.play();
    }

    private VBox createSolutionPanel(String title, List<Integer> path, int pathLength, long time, Color pathColor, boolean isWinner) {
        VBox solutionPanel = new VBox(10);
        solutionPanel.setAlignment(Pos.CENTER);
        solutionPanel.setPadding(new Insets(10));

        // Container with border
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: #222; -fx-background-radius: 10;");

        // Add winner crown if this is the best solution
        Label solutionTitle = new Label(title);
        solutionTitle.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        solutionTitle.setStyle("-fx-text-fill: white;");

        if (isWinner) {
            HBox titleWithCrown = new HBox(5);
            titleWithCrown.setAlignment(Pos.CENTER);
            Label crownLabel = new Label("👑");
            crownLabel.setFont(Font.font(18));
            titleWithCrown.getChildren().addAll(crownLabel, solutionTitle);
            container.getChildren().add(titleWithCrown);

            // Add golden glow effect to the winner
            container.setStyle("-fx-background-color: linear-gradient(to bottom, #222, #332200); " +
                    "-fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, #ffcc00, 10, 0, 0, 0);");
        } else {
            container.getChildren().add(solutionTitle);
        }

        // Create a small canvas to display the path
        int miniCanvasSize = 200;
        Canvas pathCanvas = new Canvas(miniCanvasSize, miniCanvasSize);
        GraphicsContext gc = pathCanvas.getGraphicsContext2D();

        // Draw background
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, miniCanvasSize, miniCanvasSize);

        // Draw grid
        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(0.5);
        int gridSize = 20;

        for (int i = 0; i <= miniCanvasSize; i += gridSize) {
            gc.strokeLine(0, i, miniCanvasSize, i);
            gc.strokeLine(i, 0, i, miniCanvasSize);
        }

        // Scale the city points to fit the mini canvas
        double scale = (double)miniCanvasSize / canvasSize;

        // Draw the path
        gc.setStroke(pathColor);
        gc.setLineWidth(2);

        for (int i = 0; i < path.size() - 1; i++) {
            Point a = cityPoints.get(path.get(i));
            Point b = cityPoints.get(path.get(i + 1));
            gc.strokeLine(a.x * scale, a.y * scale, b.x * scale, b.y * scale);
        }

        // Complete the cycle
        if (path.size() > 1) {
            Point start = cityPoints.get(path.get(0));
            Point end = cityPoints.get(path.get(path.size() - 1));
            gc.strokeLine(end.x * scale, end.y * scale, start.x * scale, start.y * scale);
        }

        // Draw cities
        for (int i = 0; i < cityPoints.size(); i++) {
            Point p = cityPoints.get(i);
            gc.setFill(Color.WHITE);
            gc.fillOval(p.x * scale - 3, p.y * scale - 3, 6, 6);
        }

        container.getChildren().add(pathCanvas);

        // Stats
        HBox statsContainer = new HBox(15);
        statsContainer.setAlignment(Pos.CENTER);

        // Path length
        VBox lengthBox = new VBox(5);
        lengthBox.setAlignment(Pos.CENTER);
        Label lengthValue = new Label(String.valueOf(pathLength));
        lengthValue.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        lengthValue.setStyle("-fx-text-fill: " + (isWinner ? "#ffcc00" : "#56eb61") + ";");
        Label lengthLabel = new Label("Length");
        lengthLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");
        lengthBox.getChildren().addAll(lengthValue, lengthLabel);

        // Time
        VBox timeBox = new VBox(5);
        timeBox.setAlignment(Pos.CENTER);
        DecimalFormat df = new DecimalFormat("#.##");
        Label timeValue = new Label(df.format(time / 1000.0) + "s");
        timeValue.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        timeValue.setStyle("-fx-text-fill: " + (isWinner ? "#ffcc00" : "#56eb61") + ";");
        Label timeLabel = new Label("Time");
        timeLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");
        timeBox.getChildren().addAll(timeValue, timeLabel);

        statsContainer.getChildren().addAll(lengthBox, timeBox);
        container.getChildren().add(statsContainer);

        solutionPanel.getChildren().add(container);
        return solutionPanel;
    }
}