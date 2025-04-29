package org.example.algoplay.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.example.algoplay.games.tsp.TspScoreCalculator;
import org.example.algoplay.games.tsp.Point;
import org.example.algoplay.controllers.games.tsp.ScoresPageController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * View class that handles loading and managing the FXML UI for the Scores Page.
 * This follows the MVC pattern by separating the view management from the controller logic.
 */
public class ScoresPage {

    private Stage stage;
    private Parent root;
    private ScoresPageController controller;

    /**
     * Constructor that loads the FXML and initializes the view
     *
     * @param parentStage The parent stage for modal display
     * @throws IOException If the FXML file cannot be loaded
     */
    public ScoresPage(Stage parentStage) throws IOException {
        // Create a new stage for the scores page
        this.stage = new Stage();

        // Set modality so it blocks input to the parent window
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(parentStage);

        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tsp/ScoresPage.fxml"));
        root = loader.load();

        // Get the controller
        controller = loader.getController();

        // Configure the stage
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("TSP Challenge Results");
        stage.setScene(scene);
    }

    /**
     * Set the data to be displayed in the scores page
     *
     * @param cityPoints The list of city points
     * @param paths Map of solver names to their paths
     * @param scores Map of solver names to their scores
     * @param numCities Number of cities in the problem
     */
//    public void setData(List<Point> cityPoints, Map<String, List<Integer>> paths,
//                        Map<String, SolutionScore> scores, int numCities) {
//        controller.setData(cityPoints, paths, scores, numCities);
//    }

    /**
     * Display the view
     */
    public void show() {
        stage.show();
    }

    /**
     * Get the controller associated with this view
     *
     * @return The ScoresPageController instance
     */
    public ScoresPageController getController() {
        return controller;
    }

    /**
     * Get the root node of the view
     *
     * @return The root Parent node
     */
    public Parent getRoot() {
        return root;
    }
}