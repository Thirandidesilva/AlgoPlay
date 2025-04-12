package org.example.algoplay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.algoplay.services.DatabaseService;

public class AlgoPlayApplication extends Application {

    private DatabaseService databaseService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the main menu FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainMenu.fxml"));

        // Set up the primary stage
        primaryStage.setTitle("AlgoPlay - Algorithm Games");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // Database initialization
    @Override
    public void init() throws Exception {
        // Initialize database connection
        databaseService = new DatabaseService();
        databaseService.initializeDatabase();
    }

    // Clean up resources
    @Override
    public void stop() throws Exception {
        // Close database connection
        if (databaseService != null) {
            databaseService.closeConnection();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}