package org.example.algoplay.controllers.games.tsp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import org.example.algoplay.services.UserSessionService;
import org.example.algoplay.view.ScoresPage;
import org.example.algoplay.games.tsp.TspPerformanceTracker;
import org.example.algoplay.games.tsp.*;

import java.net.URL;
import java.util.*;

/**
 * Controller for the PvM (Person vs Machine) Page view
 * Allows users to compete against algorithms in the TSP challenge
 */
public class PvMPageController implements Initializable {

    @FXML private Canvas canvas;
    @FXML private Button runAlgosBtn;
    @FXML private Button resetBtn;
    @FXML private Button logOutBtn;
    @FXML private Button newCitiesBtn;
    @FXML private Button finishPathBtn;
    @FXML private Button clearPathBtn;
    @FXML private Slider citiesSlider;
    @FXML private Slider iterationsSlider;
    @FXML private Slider populationSlider;
    @FXML private Label iterationsLabel;
    @FXML private Label populationLabel;
    @FXML private Label userInstructLabel;

    // Result labels
    @FXML private Label userTimeLabel;
    @FXML private Label antTimeLabel;
    @FXML private Label beeTimeLabel;
    @FXML private Label hybridTimeLabel;
    @FXML private Label userPathLabel;
    @FXML private Label antPathLabel;
    @FXML private Label beePathLabel;
    @FXML private Label hybridPathLabel;
    @FXML private Label userStatsLabel;
    @FXML private Label antStatsLabel;
    @FXML private Label beeStatsLabel;
    @FXML private Label hybridStatsLabel;
    @FXML private Label userWinnerLabel;
    @FXML private Label antWinnerLabel;
    @FXML private Label beeWinnerLabel;
    @FXML private Label hybridWinnerLabel;

    @FXML private VBox advancedSection;
    @FXML private VBox pathDetailsBox;
    @FXML private Label citiesValueLabel;
    @FXML private Label iterationsValueLabel;
    @FXML private Label populationValueLabel;
    @FXML private Label statusOverlay;
    @FXML private ProgressBar algorithmProgress;
    @FXML private Button startPathBtn;
    @FXML private Button helpBtn;
    @FXML private Button expandResultsBtn;
    @FXML private Button showAllPathsBtn;
    @FXML private Button showBestPathBtn;
    @FXML private ToggleButton advancedToggle;
    @FXML private BorderPane mainContainer;
    @FXML private Label rdsPathLabel;
    @FXML private Label rdsTimeLabel;
    @FXML private Label rdsStatsLabel;
    @FXML private Label rdsWinnerLabel;
    @FXML private ProgressIndicator rdsProgress;

    @FXML private ProgressIndicator antProgress;
    @FXML private ProgressIndicator beeProgress;
    @FXML private ProgressIndicator hybridProgress;

    @FXML private Button viewScoresBtn;

    private Map<String, List<Integer>> allPaths = new HashMap<>();
    private Map<String, TspScoreCalculator.SolutionScore> allScores = new HashMap<>();

    private GraphicsContext gc;
    private final List<Point> cityPoints = new ArrayList<>();
    private final List<Integer> userPath = new ArrayList<>();
    private boolean pathCreationMode = false;
    private long userStartTime = 0;
    private long userEndTime = 0;

    private long antTime = 0;
    private long beeTime = 0;
    private long hybridTime = 0;

    private TspPerformanceTracker performanceTracker = new TspPerformanceTracker();
    private static PvMPageController instance;


    // Color scheme
    private final Color backgroundColor = Color.web("#1e1e2e");
    private final Color primaryColor = Color.web("#89b4fa");
    private final Color secondaryColor = Color.web("#a6e3a1");
    private final Color accentColor = Color.web("#f5c2e7");
    private final Color textColor = Color.web("#cdd6f4");
    private final Color userPathColor = Color.web("#645dd7");
    private final Color antPathColor = Color.web("#ff1654");
    private final Color beePathColor = Color.web("#f2af29");
    private final Color hybridPathColor = Color.web("#0ead69");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize graphics context
        gc = canvas.getGraphicsContext2D();
        instance = this;


        // Apply styling to elements
        applyStyles();

        // Setup canvas events
        canvas.setOnMouseClicked(this::handleCanvasClick);

        // Improve sidebar layout
        ScrollPane rightPanel = (ScrollPane) mainContainer.getRight();
        rightPanel.setPrefWidth(420);
        rightPanel.setMinWidth(420);
        rightPanel.setStyle("-fx-background-color: #1e1e2e; -fx-border-color: #313244; -fx-border-width: 0 0 0 1;");

        // Responsive layout
        mainContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            double sidebarWidth = 400;
            canvas.setWidth(newVal.doubleValue() - sidebarWidth);
        });

        canvas.widthProperty().bind(mainContainer.widthProperty().subtract(rightPanel.widthProperty()));
        canvas.heightProperty().bind(mainContainer.heightProperty());

        // Setup sliders with value displays
        setupSliders();

        // Initial canvas setup
        setupCanvas();
        updateUserInstructions("Click on canvas to add cities");

        // Test database connection
        testDatabaseConnection();

        // Setup the view scores button (initially disabled)
        viewScoresBtn.setDisable(true);
        viewScoresBtn.setOnAction(e -> openScoresPage());

        // Disable buttons that shouldn't be available initially
        finishPathBtn.setDisable(true);
        clearPathBtn.setDisable(true);

        // Add tooltips to buttons
        addTooltips();
    }

    private void applyStyles() {
        // Style all buttons
        for (Button btn : new Button[]{
                runAlgosBtn, resetBtn, logOutBtn, newCitiesBtn, finishPathBtn,
                clearPathBtn, startPathBtn, helpBtn, expandResultsBtn,
                showAllPathsBtn, showBestPathBtn, viewScoresBtn
        }) {
            if (btn != null) {
                btn.getStyleClass().add("modern-button");
                btn.setStyle("-fx-background-color: #313244; -fx-text-fill: #cdd6f4; " +
                        "-fx-background-radius: 5; -fx-border-color: #45475a; " +
                        "-fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 8 12;");
            }
        }

        // Special styling for primary actions
        runAlgosBtn.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e; " +
                "-fx-font-weight: bold; -fx-background-radius: 5; -fx-border-radius: 5; " +
                "-fx-padding: 10 15;");

        startPathBtn.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #1e1e2e; " +
                "-fx-font-weight: bold; -fx-background-radius: 5; -fx-border-radius: 5; " +
                "-fx-padding: 10 15;");

        // Style advanced toggle button
        advancedToggle.setStyle("-fx-background-color: #313244; -fx-text-fill: #cdd6f4; " +
                "-fx-background-radius: 5; -fx-border-color: #45475a; " +
                "-fx-border-radius: 5; -fx-border-width: 1;");

        // Style sliders
        for (Slider slider : new Slider[]{citiesSlider, iterationsSlider, populationSlider}) {
            if (slider != null) {
                slider.setStyle("-fx-control-inner-background: #313244; " +
                        "-fx-thumb-color: #89b4fa; -fx-track-color: #45475a;");
            }
        }

        // Style labels
        for (Label label : new Label[]{
                userInstructLabel, userTimeLabel, antTimeLabel, beeTimeLabel, hybridTimeLabel,
                userPathLabel, antPathLabel, beePathLabel, hybridPathLabel,
                userStatsLabel, antStatsLabel, beeStatsLabel, hybridStatsLabel,
                citiesValueLabel, iterationsValueLabel, populationValueLabel
        }) {
            if (label != null) {
                label.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 13px;");
            }
        }

        // Style winner labels
        for (Label label : new Label[]{userWinnerLabel, antWinnerLabel, beeWinnerLabel, hybridWinnerLabel}) {
            if (label != null) {
                label.setStyle("-fx-font-size: 24px;");
            }
        }

        // Style containers
        advancedSection.setStyle("-fx-background-color: #313244; -fx-padding: 10; " +
                "-fx-background-radius: 5; -fx-border-radius: 5;");

        pathDetailsBox.setStyle("-fx-background-color: #313244; -fx-padding: 10; " +
                "-fx-background-radius: 5; -fx-border-radius: 5;");

        // Style progress indicators
        for (ProgressIndicator pi : new ProgressIndicator[]{antProgress, beeProgress, hybridProgress}) {
            if (pi != null) {
                pi.setStyle("-fx-progress-color: #89b4fa;");
            }
        }

        // Style user instructions
        userInstructLabel.setStyle("-fx-text-fill: #f5c2e7; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Main container background
        mainContainer.setStyle("-fx-background-color: #1e1e2e;");
    }

    private void setupSliders() {
        // Cities slider
        citiesSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            citiesValueLabel.setText(String.valueOf(newVal.intValue()));
        });

        // Iterations slider
        iterationsSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            iterationsValueLabel.setText(String.valueOf(newVal.intValue()));
            iterationsLabel.setText("Iterations: " + newVal.intValue());
        });

        // Population slider
        populationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            populationValueLabel.setText(String.valueOf(newVal.intValue()));
            populationLabel.setText("Population: " + newVal.intValue());
        });
    }

    private void addTooltips() {
        Tooltip.install(runAlgosBtn, new Tooltip("Run algorithms and compare results"));
        Tooltip.install(resetBtn, new Tooltip("Reset the canvas and all data"));
        Tooltip.install(newCitiesBtn, new Tooltip("Generate new random cities"));
        Tooltip.install(startPathBtn, new Tooltip("Begin creating your path solution"));
        Tooltip.install(finishPathBtn, new Tooltip("Complete your path and prepare for comparison"));
        Tooltip.install(clearPathBtn, new Tooltip("Clear your current path and start over"));
        Tooltip.install(viewScoresBtn, new Tooltip("View detailed performance scores"));
        Tooltip.install(helpBtn, new Tooltip("Display help information"));
        Tooltip.install(advancedToggle, new Tooltip("Show/hide advanced settings"));
        Tooltip.install(expandResultsBtn, new Tooltip("Expand/collapse detailed path information"));
        Tooltip.install(showAllPathsBtn, new Tooltip("Display all solution paths"));
        Tooltip.install(showBestPathBtn, new Tooltip("Display only the winning path"));
        Tooltip.install(citiesSlider, new Tooltip("Set number of cities to generate"));
        Tooltip.install(iterationsSlider, new Tooltip("Set algorithm iterations"));
        Tooltip.install(populationSlider, new Tooltip("Set algorithm population size"));
    }

    private void testDatabaseConnection() {
        TspDatabaseManager db = new TspDatabaseManager();
        boolean success = db.saveStructuredData(performanceTracker.getCurrentResults());

        if (!success) {
            System.out.println("Warning: Failed to save TSP session.");
        }

        if (db.testConnection()) {
            System.out.println("Successfully connected to database");

        } else {
            System.out.println("Failed to connect to database. Check your connection settings.");
        }
    }


    private void handleCanvasClick(MouseEvent e) {
        if (pathCreationMode) {
            // In path creation mode, user is selecting city sequence
            int selectedCity = findNearestCity(e.getX(), e.getY());
            if (selectedCity != -1) {
                if (!userPath.contains(selectedCity)) {
                    userPath.add(selectedCity);
                    drawUserPath();
                    userPathLabel.setText(formatPath(userPath));

                    // Enable finish button when all cities are added
                    if (userPath.size() == cityPoints.size()) {
                        finishPathBtn.setDisable(false);
                    }
                } else if (userPath.size() > 2 && selectedCity == userPath.get(0)) {
                    // User clicked on starting city to complete the circuit
                    finishUserPath();
                }
            }
        } else {
            // In city placement mode
            if (cityPoints.size() >= 20) return;
            cityPoints.add(new Point(e.getX(), e.getY()));
            drawCities();

            // Enable buttons when we have enough cities
            if (cityPoints.size() >= 3) {
                startPathBtn.setDisable(false);
            }
        }
    }

    public static void updateDreamProgress(double progress) {
        Platform.runLater(() -> {
            if (instance != null && instance.rdsProgress != null) {
                instance.rdsProgress.setProgress(progress);
            }
        });
    }



    private int findNearestCity(double x, double y) {
        int nearest = -1;
        double minDist = 15; // Threshold distance for selection

        for (int i = 0; i < cityPoints.size(); i++) {
            Point p = cityPoints.get(i);
            double dist = Math.sqrt(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2));
            if (dist < minDist) {
                minDist = dist;
                nearest = i;
            }
        }

        return nearest;
    }

    private void setupCanvas() {
        // Set initial canvas state
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Text for empty canvas
        gc.setFill(textColor);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 20));
        gc.fillText("Click on canvas to add cities", canvas.getWidth()/2 - 150, canvas.getHeight()/2);

        // Add a subtle grid for better visualization
        drawGrid();
    }

    private void drawGrid() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double gridSize = 40;

        gc.setStroke(Color.web("#313244"));
        gc.setLineWidth(0.5);

        // Draw horizontal grid lines
        for (double y = 0; y <= height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }

        // Draw vertical grid lines
        for (double x = 0; x <= width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }
    }

    @FXML
    private void handleLogOut() {
        System.out.println("Log out clicked");
    }

    @FXML
    private void handleRunAlgos() {
        int n = cityPoints.size();
        if (n < 3) {
            showAlert("Please place at least 3 cities.");
            return;
        }

        if (userPath.size() != cityPoints.size()) {
            showAlert("Please complete your path before running algorithms.");
            return;
        }

        // Show loading state
        statusOverlay.setText("Running algorithms...");
        algorithmProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        statusOverlay.setVisible(true);
        algorithmProgress.setVisible(true);

        // Create distance matrix
        int[][] distances = new int[n][n];
        List<Integer> cities = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            cities.add(i);
            for (int j = 0; j < n; j++) {
                distances[i][j] = (int) cityPoints.get(i).distanceTo(cityPoints.get(j));
            }
        }

        // Calculate user path length
        int userLen = calcLen(userPath, distances);
        long userDuration = userEndTime - userStartTime;
        userStatsLabel.setText("Length: " + userLen + " (" + cityPoints.size() + " cities)");
        userTimeLabel.setText(userDuration + "ms");

        // Record user performance
        // Assume you have userPath calculated here

        String username = "User"; // Default fallback
        if (UserSessionService.getInstance().isLoggedIn()) {
            username = UserSessionService.getInstance().getCurrentUser().getUsername();
        }

        // Record human user's solution
        performanceTracker.recordPerformance(username, userLen, userDuration, userPath, cityPoints.size());


        // Run the algorithms
        // Ant Solver
        antProgress.setVisible(true);
        long startTime = System.currentTimeMillis();
        TspSolver antSolver = new SantSolver();
        antSolver.initialize(distances, cities);
        List<Integer> antPath = antSolver.solve();
        long endTime = System.currentTimeMillis();
        antTime = endTime - startTime;
        antTimeLabel.setText(antTime + "ms");
        int antLen = calcLen(antPath, distances);
        antPathLabel.setText(formatPath(antPath));
        antStatsLabel.setText("Length: " + antLen + " (" + cityPoints.size() + " cities)");
        antProgress.setVisible(false);

        // Record Ant Colony performance
        performanceTracker.recordPerformance("Ant", antLen, antTime, antPath, cityPoints.size());

        // Bee Solver
        beeProgress.setVisible(true);
        startTime = System.currentTimeMillis();
        TspSolver beeSolver = new SbeeSolver();
        beeSolver.initialize(distances, cities);
        List<Integer> beePath = beeSolver.solve();
        endTime = System.currentTimeMillis();
        beeTime = endTime - startTime;
        beeTimeLabel.setText(beeTime + "ms");
        int beeLen = calcLen(beePath, distances);
        beePathLabel.setText(formatPath(beePath));
        beeStatsLabel.setText("Length: " + beeLen + " (" + cityPoints.size() + " cities)");
        beeProgress.setVisible(false);

        // Record Bee performance
        performanceTracker.recordPerformance("Bee", beeLen, beeTime, beePath, cityPoints.size());

        // Hybrid Solver
        hybridProgress.setVisible(true);
        startTime = System.currentTimeMillis();
        TspSolver hybridSolver = new HybridSolver();
        hybridSolver.initialize(distances, cities);
        List<Integer> hybridPath = hybridSolver.solve();
        endTime = System.currentTimeMillis();
        hybridTime = endTime - startTime;
        hybridTimeLabel.setText(hybridTime + "ms");
        int hybridLen = calcLen(hybridPath, distances);
        hybridPathLabel.setText(formatPath(hybridPath));
        hybridStatsLabel.setText("Length: " + hybridLen + " (" + cityPoints.size() + " cities)");
        hybridProgress.setVisible(false);

        // Record Hybrid performance
        performanceTracker.recordPerformance("Hybrid", hybridLen, hybridTime, hybridPath, cityPoints.size());

        // RDS Solver
        rdsProgress.setVisible(true);
        startTime = System.currentTimeMillis();
        TspSolver rdsSolver = new RdsSolver();
        rdsSolver.initialize(distances, cities);
        List<Integer> rdsPath = rdsSolver.solve();
        long rdsEndTime = System.currentTimeMillis();
        long rdsTime = rdsEndTime - startTime;
        int rdsLen = calcLen(rdsPath, distances);

// Update RDS GUI
        rdsTimeLabel.setText(rdsTime + "ms");
        rdsPathLabel.setText(formatPath(rdsPath));
        rdsStatsLabel.setText("Length: " + rdsLen + " (" + cityPoints.size() + " cities)");
        rdsProgress.setVisible(false);

// Record RDS performance
        performanceTracker.recordPerformance("RDS", rdsLen, rdsTime, rdsPath, cityPoints.size());




        // Redraw everything with paths
        drawCities();
        drawPath(userPath, userPathColor, 3);
        drawPath(antPath, antPathColor, 2);
        drawPath(beePath, beePathColor, 2);
        drawPath(hybridPath, hybridPathColor, 2);
        drawPath(rdsPath, Color.MEDIUMPURPLE, 2);


        // Hide loading state
        statusOverlay.setVisible(false);
        algorithmProgress.setVisible(false);

        // Calculate efficiency scores
        Map<String, Double> scores = performanceTracker.calculateEfficiencyScores();

        // Find the best solution based on our composite score
        String bestSolution = null;
        double maxScore = -1;
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestSolution = entry.getKey();
            }
        }

        // Clear previous winners
        userWinnerLabel.setText("");
        antWinnerLabel.setText("");
        beeWinnerLabel.setText("");
        hybridWinnerLabel.setText("");

        // Set winner trophies
        if (bestSolution != null) {
            switch (bestSolution) {
                case "User":
                    userWinnerLabel.setText("🏆");
                    break;
                case "Ant":
                    antWinnerLabel.setText("🏆");
                    break;
                case "Bee":
                    beeWinnerLabel.setText("🏆");
                    break;
                case "Hybrid":
                    hybridWinnerLabel.setText("🏆");
                    break;
                case "RDS":
                    rdsWinnerLabel.setText("🏆");
                    break;

            }
        }

        // Automatically save results to database
        try {
            boolean saved = performanceTracker.saveToDbAndFinalize();
            if (!saved) {
                System.out.println("Warning: Failed to save results to database");
            }
        } catch (Exception e) {
            System.out.println("Error saving to database: " + e.getMessage());
            e.printStackTrace();
        }

        // Enable view scores button
        viewScoresBtn.setDisable(false);

        // Enable path view buttons
        showAllPathsBtn.setDisable(false);
        showBestPathBtn.setDisable(false);

        // Store paths for scores page
        allPaths.clear();
        allPaths.put("User", new ArrayList<>(userPath));
        allPaths.put("Ant", new ArrayList<>(antPath));
        allPaths.put("Bee", new ArrayList<>(beePath));
        allPaths.put("Hybrid", new ArrayList<>(hybridPath));

        // Show a success message
        updateUserInstructions("Algorithms complete! " + bestSolution + " was the winner");
    }

    private void openScoresPage() {
        try {
            // Get the current stage
            Stage currentStage = (Stage) canvas.getScene().getWindow();

            // Create the scores page view
            ScoresPage scoresPage = new ScoresPage(currentStage);

            // Show the scores page
            scoresPage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening scores page: " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        cityPoints.clear();
        userPath.clear();
        pathCreationMode = false;
        setupCanvas();
        updateUserInstructions("Click on canvas to add cities");

        // Reset result labels
        userTimeLabel.setText("-");
        antTimeLabel.setText("-");
        beeTimeLabel.setText("-");
        hybridTimeLabel.setText("-");
        userPathLabel.setText("-");
        antPathLabel.setText("-");
        beePathLabel.setText("-");
        hybridPathLabel.setText("-");
        userStatsLabel.setText("-");
        antStatsLabel.setText("-");
        beeStatsLabel.setText("-");
        hybridStatsLabel.setText("-");
        userWinnerLabel.setText("");
        antWinnerLabel.setText("");
        beeWinnerLabel.setText("");
        hybridWinnerLabel.setText("");

        // Reset buttons
        startPathBtn.setDisable(true);
        finishPathBtn.setDisable(true);
        clearPathBtn.setDisable(true);
        showAllPathsBtn.setDisable(true);
        showBestPathBtn.setDisable(true);

        // Hide progress indicators
        antProgress.setVisible(false);
        beeProgress.setVisible(false);
        hybridProgress.setVisible(false);

        // Also clear stored paths and scores
        allPaths.clear();
        allScores.clear();
        viewScoresBtn.setDisable(true);
    }

    @FXML
    private void handleNewCities() {
        generateRandomCities((int) citiesSlider.getValue());
        startPathBtn.setDisable(false);
    }

    @FXML
    private void handleStartPathCreation() {
        if (cityPoints.size() < 3) {
            showAlert("Please place at least 3 cities before creating a path.");
            return;
        }

        pathCreationMode = true;
        userPath.clear();
        updateUserInstructions("Click cities in sequence to create your path");
        clearPathBtn.setDisable(false);
        finishPathBtn.setDisable(true);
        userStartTime = System.currentTimeMillis();

        // Reset prior results
        userTimeLabel.setText("-");
        antTimeLabel.setText("-");
        beeTimeLabel.setText("-");
        hybridTimeLabel.setText("-");
        userPathLabel.setText("-");
        antPathLabel.setText("-");
        beePathLabel.setText("-");
        hybridPathLabel.setText("-");
        userStatsLabel.setText("-");
        antStatsLabel.setText("-");
        beeStatsLabel.setText("-");
        hybridStatsLabel.setText("-");
        userWinnerLabel.setText("");
        antWinnerLabel.setText("");
        beeWinnerLabel.setText("");
        hybridWinnerLabel.setText("");

        // Add visual indicator that we're in path creation mode
        drawCities();
        gc.setStroke(Color.web("#f5c2e7", 0.3));
        gc.setLineWidth(2);
        gc.strokeRect(10, 10, canvas.getWidth() - 20, canvas.getHeight() - 20);
        gc.setFill(textColor);
        gc.fillText("Path Creation Mode", 20, 30);
    }

    @FXML
    private void handleClearPath() {
        userPath.clear();
        drawCities();
        userPathLabel.setText("-");
        userStartTime = System.currentTimeMillis();
        finishPathBtn.setDisable(true);

        // Re-add the path creation mode visual indicator
        gc.setStroke(Color.web("#f5c2e7", 0.3));
        gc.setLineWidth(2);
        gc.strokeRect(10, 10, canvas.getWidth() - 20, canvas.getHeight() - 20);
        gc.setFill(textColor);
        gc.fillText("Path Creation Mode", 20, 30);
    }

    @FXML
    private void handleFinishPath() {
        if (userPath.size() < cityPoints.size()) {
            showAlert("Please visit all cities before finishing your path.");
            return;
        }
        finishUserPath();
    }

    private void finishUserPath() {
        pathCreationMode = false;
        userEndTime = System.currentTimeMillis();
        updateUserInstructions("Path completed! Run algorithms to compare");
        drawUserPath();
        runAlgosBtn.setDisable(false);

        // Add visual indicator that path is complete
        gc.setFill(Color.web("#a6e3a1", 0.7));
        gc.fillText("✓ Path Complete", canvas.getWidth() - 150, 30);
    }

    private void generateRandomCities(int count) {
        cityPoints.clear();
        userPath.clear();
        pathCreationMode = false;

        // Generate cities with padding from edges
        int padding = 40;
        int availableWidth = (int) canvas.getWidth() - (2 * padding);
        int availableHeight = (int) canvas.getHeight() - (2 * padding);
        Random rand = new Random(System.currentTimeMillis());

        for (int i = 0; i < count; i++) {
            double x = padding + rand.nextDouble() * availableWidth;
            double y = padding + rand.nextDouble() * availableHeight;
            cityPoints.add(new Point(x, y));
        }

        drawCities();
        updateUserInstructions("Click 'Start Path Creation' to begin your solution");
        finishPathBtn.setDisable(false);

        // Enable the view scores button
        runAlgosBtn.setDisable(true);
        showAllPathsBtn.setDisable(true);
        showBestPathBtn.setDisable(true);

        // Reset results
        userTimeLabel.setText("-");
        antTimeLabel.setText("-");
        beeTimeLabel.setText("-");
        hybridTimeLabel.setText("-");
        userPathLabel.setText("-");
        antPathLabel.setText("-");
        beePathLabel.setText("-");
        hybridPathLabel.setText("-");
        userStatsLabel.setText("-");
        antStatsLabel.setText("-");
        beeStatsLabel.setText("-");
        hybridStatsLabel.setText("-");
        userWinnerLabel.setText("");
        antWinnerLabel.setText("");
        beeWinnerLabel.setText("");
        hybridWinnerLabel.setText("");
    }

    private void drawCities() {
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw grid first
        drawGrid();

        gc.setFill(textColor);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("Cities: " + cityPoints.size(), 10, 20);

        for (int i = 0; i < cityPoints.size(); i++) {
            Point p = cityPoints.get(i);

            // Draw city halo
            gc.setFill(Color.web("#89b4fa", 0.3));
            gc.fillOval(p.x - 10, p.y - 10, 20, 20);

            // Draw city point
            gc.setFill(primaryColor);
            gc.fillOval(p.x - 5, p.y - 5, 10, 10);

            // Draw city label with a better contrast
            gc.setFill(Color.BLACK);
            gc.fillText("C" + i, p.x + 6, p.y - 6);
            gc.setFill(Color.WHITE);
            gc.fillText("C" + i, p.x + 5, p.y - 5);
        }
    }

    private void drawUserPath() {
        drawCities();
        drawPath(userPath, userPathColor, 3);
    }

    private void drawPath(List<Integer> path, Color color, double lineWidth) {
        if (path.isEmpty()) return;

        // Draw glowing effect for path
        gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.3));
        gc.setLineWidth(lineWidth + 3);

        for (int i = 0; i < path.size() - 1; i++) {
            Point a = cityPoints.get(path.get(i));
            Point b = cityPoints.get(path.get(i + 1));
            gc.strokeLine(a.x, a.y, b.x, b.y);
        }

        // Draw actual path
        gc.setStroke(color);
        gc.setLineWidth(lineWidth);

        for (int i = 0; i < path.size() - 1; i++) {
            Point a = cityPoints.get(path.get(i));
            Point b = cityPoints.get(path.get(i + 1));
            gc.strokeLine(a.x, a.y, b.x, b.y);
        }

        // Show path direction with elegant arrows
        for (int i = 0; i < path.size() - 1; i++) {
            Point a = cityPoints.get(path.get(i));
            Point b = cityPoints.get(path.get(i + 1));
            drawArrow(a, b, color);
        }

        // Close loop if path is complete
        if (path.size() == cityPoints.size()) {
            Point start = cityPoints.get(path.get(0));
            Point end = cityPoints.get(path.get(path.size() - 1));

            // Draw glow for closing line
            gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.3));
            gc.setLineWidth(lineWidth + 3);
            gc.strokeLine(end.x, end.y, start.x, start.y);

            // Draw actual closing line
            gc.setStroke(color);
            gc.setLineWidth(lineWidth);
            gc.strokeLine(end.x, end.y, start.x, start.y);

            drawArrow(end, start, color);
        }

        // Highlight first city with a special marker
        if (!path.isEmpty()) {
            Point firstCity = cityPoints.get(path.get(0));
            gc.setFill(color);
            gc.fillOval(firstCity.x - 7, firstCity.y - 7, 14, 14);
            gc.setFill(Color.WHITE);
            gc.fillOval(firstCity.x - 3, firstCity.y - 3, 6, 6);
        }
    }

    private void drawArrow(Point start, Point end, Color color) {
        double arrowSize = 6;
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double angle = Math.atan2(dy, dx);
        double length = Math.sqrt(dx * dx + dy * dy);

        // Calculate the position of the arrow (80% along the line)
        double arrowX = start.x + dx * 0.8;
        double arrowY = start.y + dy * 0.8;

        double x1 = arrowX - arrowSize * Math.cos(angle - Math.PI/6);
        double y1 = arrowY - arrowSize * Math.sin(angle - Math.PI/6);
        double x2 = arrowX - arrowSize * Math.cos(angle + Math.PI/6);
        double y2 = arrowY - arrowSize * Math.sin(angle + Math.PI/6);

        // Draw arrow glow
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.fillPolygon(
                new double[] {arrowX + 1, x1 + 1, x2 + 1},
                new double[] {arrowY + 1, y1 + 1, y2 + 1},
                3
        );

        // Draw arrow
        gc.setFill(color);
        gc.fillPolygon(
                new double[] {arrowX, x1, x2},
                new double[] {arrowY, y1, y2},
                3
        );
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
        // Close the loop
        len += dist[path.get(path.size() - 1)][path.get(0)];
        return len;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TSP Challenge");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #1e1e2e;" +
                        "-fx-text-fill: #cdd6f4;"
        );

        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 14px;");
        dialogPane.getButtonTypes().stream()
                .map(dialogPane::lookupButton)
                .forEach(button -> button.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e;"));

        alert.showAndWait();
    }

    @FXML
    private void toggleAdvancedOptions() {
        // Add animation for advanced options section
        if (advancedSection.isVisible()) {
            advancedSection.setVisible(false);
            advancedSection.setManaged(false);
            advancedToggle.setText("▼ Show Advanced Options");
        } else {
            advancedSection.setVisible(true);
            advancedSection.setManaged(true);
            advancedToggle.setText("▲ Hide Advanced Options");
        }
    }

    @FXML
    private void handleExpandResults() {
        // Add animation for path details expansion
        if (pathDetailsBox.isVisible()) {
            pathDetailsBox.setVisible(false);
            pathDetailsBox.setManaged(false);
            expandResultsBtn.setText("▼ Show Path Details");
        } else {
            pathDetailsBox.setVisible(true);
            pathDetailsBox.setManaged(true);
            expandResultsBtn.setText("▲ Hide Path Details");
        }
    }

    @FXML
    private void handleShowAllPaths() {
        // Redraw the canvas with all paths visible
        drawCities();

        // Add a legend
        drawPathLegend();

        // Draw all paths with different z-orders (user path on top)
        if (!hybridPathLabel.getText().equals("-")) {
            List<Integer> hybridPath = parsePathFromLabel(hybridPathLabel.getText());
            drawPath(hybridPath, hybridPathColor, 2);
        }

        if (!beePathLabel.getText().equals("-")) {
            List<Integer> beePath = parsePathFromLabel(beePathLabel.getText());
            drawPath(beePath, beePathColor, 2);
        }

        if (!antPathLabel.getText().equals("-")) {
            List<Integer> antPath = parsePathFromLabel(antPathLabel.getText());
            drawPath(antPath, antPathColor, 2);
        }

        drawPath(userPath, userPathColor, 3);

        // Update instruction
        updateUserInstructions("Showing all solution paths");
    }

    private void drawPathLegend() {
        double startX = 10;
        double startY = canvas.getHeight() - 90;
        double lineLength = 30;
        double spacing = 25;

        gc.setFont(Font.font("System", FontWeight.NORMAL, 12));

        // User path
        gc.setStroke(userPathColor);
        gc.setLineWidth(3);
        gc.strokeLine(startX, startY, startX + lineLength, startY);
        gc.setFill(textColor);
        gc.fillText("User Path", startX + lineLength + 5, startY + 4);

        // Ant path
        gc.setStroke(antPathColor);
        gc.setLineWidth(2);
        gc.strokeLine(startX, startY + spacing, startX + lineLength, startY + spacing);
        gc.setFill(textColor);
        gc.fillText("Ant Colony", startX + lineLength + 5, startY + spacing + 4);

        // Bee path
        gc.setStroke(beePathColor);
        gc.setLineWidth(2);
        gc.strokeLine(startX, startY + spacing * 2, startX + lineLength, startY + spacing * 2);
        gc.setFill(textColor);
        gc.fillText("Bee Colony", startX + lineLength + 5, startY + spacing * 2 + 4);

        // Hybrid path
        gc.setStroke(hybridPathColor);
        gc.setLineWidth(2);
        gc.strokeLine(startX, startY + spacing * 3, startX + lineLength, startY + spacing * 3);
        gc.setFill(textColor);
        gc.fillText("Hybrid", startX + lineLength + 5, startY + spacing * 3 + 4);
    }

    @FXML
    private void handleShowBestPath() {
        // Find which path is the best (has the trophy)
        drawCities();

        String winner = "None";

        if (!userWinnerLabel.getText().isEmpty()) {
            drawPath(userPath, userPathColor, 4);
            winner = "User";
        } else if (!antWinnerLabel.getText().isEmpty()) {
            List<Integer> antPath = parsePathFromLabel(antPathLabel.getText());
            drawPath(antPath, antPathColor, 4);
            winner = "Ant Colony";
        } else if (!beeWinnerLabel.getText().isEmpty()) {
            List<Integer> beePath = parsePathFromLabel(beePathLabel.getText());
            drawPath(beePath, beePathColor, 4);
            winner = "Bee Colony";
        } else if (!hybridWinnerLabel.getText().isEmpty()) {
            List<Integer> hybridPath = parsePathFromLabel(hybridPathLabel.getText());
            drawPath(hybridPath, hybridPathColor, 4);
            winner = "Hybrid";
        } else {
            // No winner, show user path
            drawPath(userPath, userPathColor, 4);
            winner = "User (no comparison)";
        }

        // Add a trophy indicator
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("System", FontWeight.BOLD, 24));
        gc.fillText("🏆", 15, 50);

        gc.setFill(textColor);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("Best Solution: " + winner, 45, 50);

        // Update instruction
        updateUserInstructions("Showing the winning path: " + winner);
    }

    @FXML
    private void handleHelp() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("TSP Challenge Help");
        helpAlert.setHeaderText("How to use the TSP Challenge");

        String helpText = "The Traveling Salesman Problem (TSP) Challenge lets you compete against algorithms.\n\n" +
                "Instructions:\n" +
                "1. Add cities by clicking on the canvas or use 'Generate Cities'\n" +
                "2. Click 'Start Path Creation' to begin creating your solution\n" +
                "3. Click on cities in sequence to create your path\n" +
                "4. Click 'Finish Path' when you've visited all cities\n" +
                "5. Click 'Run Algorithms' to see how your solution compares\n\n" +
                "The goal is to find the shortest possible path that visits each city exactly once and returns to the starting city.";

        // Style the help dialog
        DialogPane dialogPane = helpAlert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #1e1e2e;" +
                        "-fx-text-fill: #cdd6f4;"
        );

        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 14px;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #313244;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 16px; -fx-font-weight: bold;");

        dialogPane.getButtonTypes().stream()
                .map(dialogPane::lookupButton)
                .forEach(button -> button.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e;"));

        helpAlert.setContentText(helpText);
        helpAlert.showAndWait();
    }

    // Add a new method to handle displaying performance history
    @FXML
    private void displayPerformanceHistory() {
        try {
            // Create a new stage
            Stage historyStage = new Stage();
            historyStage.setTitle("TSP Performance History");

            // Create list view with styled cells
            ListView<String> historyList = new ListView<>();
            historyList.setStyle("-fx-background-color: #1e1e2e; -fx-control-inner-background: #1e1e2e;");

            // Get historical results
            List<Map<String, TspPerformanceTracker.PerformanceData>> history =
                    performanceTracker.getHistoricalResults();

            // Format and add items
            int round = 1;
            for (Map<String, TspPerformanceTracker.PerformanceData> roundResults : history) {
                historyList.getItems().add("Round " + round + ":");
                for (TspPerformanceTracker.PerformanceData data : roundResults.values()) {
                    historyList.getItems().add("  " + data.toString());
                }
                historyList.getItems().add("");
                round++;
            }

            // Style the cells
            historyList.setCellFactory(list -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-background-color: #1e1e2e;");
                    } else {
                        setText(item);

                        if (item.startsWith("Round")) {
                            setStyle("-fx-text-fill: #f5c2e7; -fx-font-weight: bold; -fx-background-color: #1e1e2e;");
                        } else if (item.contains("User")) {
                            setStyle("-fx-text-fill: #f38ba8; -fx-background-color: #1e1e2e;");
                        } else if (item.contains("Ant")) {
                            setStyle("-fx-text-fill: #89dceb; -fx-background-color: #1e1e2e;");
                        } else if (item.contains("Bee")) {
                            setStyle("-fx-text-fill: #fab387; -fx-background-color: #1e1e2e;");
                        } else if (item.contains("Hybrid")) {
                            setStyle("-fx-text-fill: #a6e3a1; -fx-background-color: #1e1e2e;");
                        } else {
                            setStyle("-fx-text-fill: #cdd6f4; -fx-background-color: #1e1e2e;");
                        }
                    }
                }
            });

            // Create scene and show
            BorderPane historyPane = new BorderPane();
            historyPane.setCenter(historyList);
            historyPane.setStyle("-fx-background-color: #1e1e2e; -fx-padding: 10;");

            // Add a header and close button
            Label headerLabel = new Label("Performance History");
            headerLabel.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 18px; -fx-font-weight: bold;");

            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e; -fx-background-radius: 5;");
            closeBtn.setOnAction(e -> historyStage.close());

            BorderPane headerPane = new BorderPane();
            headerPane.setLeft(headerLabel);
            headerPane.setRight(closeBtn);
            headerPane.setStyle("-fx-padding: 10; -fx-background-color: #313244;");

            historyPane.setTop(headerPane);

            Scene scene = new Scene(historyPane, 600, 400);
            historyStage.setScene(scene);
            historyStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error displaying performance history: " + e.getMessage());
        }
    }

    // Helper method to parse paths from labels
    private List<Integer> parsePathFromLabel(String pathText) {
        // This is a simple approach since we only use formatted paths in the UI
        List<Integer> result = new ArrayList<>();

        // If we have full path data
        if (pathText.contains(",...,")) {
            // Extract first two cities
            String firstPart = pathText.substring(1, pathText.indexOf(",..."));
            String[] firstCities = firstPart.split(",");
            for (String city : firstCities) {
                result.add(Integer.parseInt(city.trim()));
            }

            // For simplicity, we'll get all cities again from the original list
            // This isn't efficient but works for visualization
            for (int i = 0; i < cityPoints.size(); i++) {
                if (!result.contains(i)) {
                    result.add(i);
                }
            }
        } else if (pathText.startsWith("[") && pathText.endsWith("]")) {
            // Parse the full path
            String content = pathText.substring(1, pathText.length() - 1);
            String[] cities = content.split(",");
            for (String city : cities) {
                result.add(Integer.parseInt(city.trim()));
            }
        }

        return result;
    }

    private void updateUserInstructions(String message) {
        userInstructLabel.setText(message);

        // Add a subtle animation effect to draw attention
        userInstructLabel.setStyle("-fx-text-fill: #f5c2e7; -fx-font-size: 16px; -fx-font-weight: bold;");
    }
}