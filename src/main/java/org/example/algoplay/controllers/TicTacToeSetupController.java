package org.example.algoplay.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.algoplay.models.User;
import org.example.algoplay.services.UserSessionService;

import java.io.IOException;

public class TicTacToeSetupController {

    @FXML
    private TextField nameField;

    @FXML
    private Button easyButton, mediumButton, hardButton;

    private String selectedDifficulty = "Medium"; // Default difficulty

    @FXML
    private void selectEasy() {
        selectedDifficulty = "EASY";
        updateButtonStyles(easyButton, mediumButton, hardButton);
    }

    @FXML
    private void selectMedium() {
        selectedDifficulty = "MEDIUM";
        updateButtonStyles(mediumButton, easyButton, hardButton);
    }

    @FXML
    private void selectHard() {
        selectedDifficulty = "HARD";
        updateButtonStyles(hardButton, easyButton, mediumButton);
    }

    private void updateButtonStyles(Button selected, Button... others) {
        // Apply selected style to the chosen button
        selected.getStyleClass().add("selected-button");

        // Remove selected style from other buttons
        for (Button btn : others) {
            btn.getStyleClass().remove("selected-button");
        }
    }

    @FXML
    private void startGame() {
        String playerName = nameField.getText().trim();

        // Use default name if none provided
        if (playerName.isEmpty()) {
            playerName = "Guest";
        }

        // Create a user object and store in session service
        User currentUser = new User();
        currentUser.setUsername(playerName);
        UserSessionService.getInstance().setCurrentUser(currentUser);

        try {
            // Load the game screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/TicTacToe.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the player name and difficulty
            TicTacToeController controller = loader.getController();
            controller.setPlayerName(playerName);
            controller.setDifficulty(selectedDifficulty);

            // Set up the new scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Tic Tac Toe - " + playerName);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Failed to start game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Game Start Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}