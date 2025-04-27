    package org.example.algoplay.controllers.games.tsp;

    import javafx.fxml.FXML;
    import javafx.fxml.Initializable;
    import javafx.scene.canvas.Canvas;
    import javafx.scene.canvas.GraphicsContext;
    import javafx.scene.control.*;
    import javafx.scene.paint.Color;
    import javafx.scene.text.Font;

    import org.example.algoplay.games.tsp.*;

    import java.net.URL;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Random;
    import java.util.ResourceBundle;

    /**
     * Controller for the MvM Page view
     * Handles all user interactions and connects the model with the view
     */
    public class MvMPageController implements Initializable {

        @FXML private Canvas canvas;
        @FXML private Button runRaceBtn;
        @FXML private Button resetBtn;
        @FXML private Button logOutBtn;
        @FXML private Button newCitiesBtn;
        @FXML private Slider citiesSlider;
        @FXML private Slider iterationsSlider;
        @FXML private Slider populationSlider;
        @FXML private Label iterationsLabel;
        @FXML private Label populationLabel;

        // Result labels
        @FXML private Label antTimeLabel;
        @FXML private Label beeTimeLabel;
        @FXML private Label hybridTimeLabel;
        @FXML private Label antPathLabel;
        @FXML private Label beePathLabel;
        @FXML private Label hybridPathLabel;
        @FXML private Label antStatsLabel;
        @FXML private Label beeStatsLabel;
        @FXML private Label hybridStatsLabel;
        @FXML private Label antWinnerLabel;
        @FXML private Label beeWinnerLabel;
        @FXML private Label hybridWinnerLabel;

        @FXML private ProgressIndicator antProgress;
        @FXML private ProgressIndicator beeProgress;
        @FXML private ProgressIndicator hybridProgress;

        private GraphicsContext gc;
        private final List<Point> cityPoints = new ArrayList<>();

        @Override
        public void initialize(URL location, ResourceBundle resources) {
            // Initialize graphics context
            gc = canvas.getGraphicsContext2D();

            // Setup canvas event
            canvas.setOnMouseClicked(e -> {
                if (cityPoints.size() >= 20) return;
                cityPoints.add(new Point(e.getX(), e.getY()));
                drawCities();
            });

            // Setup sliders
            iterationsSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                iterationsLabel.setText("Iterations: " + newVal.intValue());
            });

            populationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                populationLabel.setText("Population: " + newVal.intValue());
            });

            // Initial canvas setup
            setupCanvas();
        }

        private void setupCanvas() {
            // Set initial canvas state
            gc.setFill(Color.web("#2c2c2c"));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Text for empty canvas
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("Monospace", 20));
            gc.fillText("Click on canvas to add cities", canvas.getWidth()/2 - 150, canvas.getHeight()/2);
        }

        @FXML
        private void handleLogOut() {
            System.out.println("Log out clicked");
        }

        @FXML
        private void handleRunRace() {
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

        @FXML
        private void handleReset() {
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

        @FXML
        private void handleNewCities() {
            generateRandomCities((int) citiesSlider.getValue());
        }

        private void generateRandomCities(int count) {
            cityPoints.clear();

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

        private void drawCities() {
            gc.setFill(Color.web("#2c2c2c"));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

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

        private void showAlert(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }