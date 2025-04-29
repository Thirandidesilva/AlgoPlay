package org.example.algoplay.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.example.algoplay.controllers.games.tsp.PvMPageController;


import java.io.IOException;

/**
 * View class that handles loading and managing the FXML UI for the PvM Page.
 * This follows the MVC pattern by separating the view management from the controller logic.
 * PvM represents Person vs Machine mode where users compete against algorithms.
 */
public class PvMPage {

    private Stage stage;
    private Parent root;
    private PvMPageController controller;

    /**
     * Constructor that loads the FXML and initializes the view
     *
     * @param stage The primary stage for this view
     * @throws IOException If the FXML file cannot be loaded
     */
    public PvMPage(Stage stage) throws IOException {
        this.stage = stage;

        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tsp/PvMPage.fxml"));
        root = loader.load();

        // Get the controller
        controller = loader.getController();

        // Configure the stage
        Scene scene = new Scene(root, 1200, 750);
        stage.setTitle("AlgoPlay - TSP Challenge (Person vs Machine)");
        stage.setScene(scene);
    }

    /**
     * Display the view
     */
    public void show() {
        stage.show();
    }

    /**
     * Get the controller associated with this view
     *
     * @return The PvMPageController instance
     */
    public PvMPageController getController() {
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