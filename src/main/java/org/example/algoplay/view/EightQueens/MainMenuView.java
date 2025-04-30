package org.example.algoplay.view.EightQueens;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenuView extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Eight Queens Puzzle");

        // Create buttons
        Button startButton = new Button("Start Game");
        Button exitButton = new Button("Exit");

        // Set button styles
        startButton.setStyle("-fx-font-size: 20px; -fx-padding: 10px;");
        exitButton.setStyle("-fx-font-size: 20px; -fx-padding: 10px;");

        // Set button actions
        startButton.setOnAction(event -> {
            ChessBoardView chessBoardView = new ChessBoardView();
            Scene chessScene = new Scene(chessBoardView, 480, 480); // Set the size of the chess board
            primaryStage.setScene(chessScene);
        });

        exitButton.setOnAction(event -> {
            primaryStage.close(); // Close the application
        });

        // Create layout
        VBox layout = new VBox(20); // Spacing of 20 pixels
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(startButton, exitButton);

        // Create scene and set it to the stage
        Scene mainMenuScene = new Scene(layout, 480, 480);
        primaryStage.setScene(mainMenuScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}