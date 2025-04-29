package org.example.algoplay.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeScreen extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tsp/HomeScreen.fxml"));
        Parent root = loader.load();

        // Create scene
        Scene scene = new Scene(root, 1200, 800);

        // Configure stage
        primaryStage.setTitle("AlgoPlay - Traveling Salesman Problem");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(600);

        // Show stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}