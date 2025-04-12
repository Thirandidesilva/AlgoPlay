package org.example.algoplay.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.Event;

import java.io.IOException;

public class MainMenuController {

    private void loadGame(String gameName, Event event) {
        try {
            String fxmlPath = "/fxml/" + gameName + ".fxml";
            Parent gameRoot = FXMLLoader.load(getClass().getResource(fxmlPath));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene gameScene = new Scene(gameRoot);

            stage.setScene(gameScene);
            stage.setTitle("AlgoPlay - " + gameName);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error dialog to user
        }
    }

    @FXML
    private void openTicTacToe(Event event) {
        loadGame("TicTacToe", event);
    }

    @FXML
    private void openTravelingSalesman(Event event) {
        loadGame("TravelingSalesman", event);
    }

    @FXML
    private void openTowerOfHanoi(Event event) {
        loadGame("TowerOfHanoi", event);
    }

    @FXML
    private void openEightQueens(Event event) {
        loadGame("EightQueens", event);
    }

    @FXML
    private void openKnightsTour(Event event) {
        loadGame("KnightsTour", event);
    }
}