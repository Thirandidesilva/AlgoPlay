package org.example.algoplay.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.algoplay.EightQueens.MainApp;
import org.example.algoplay.controllers.games.TohController;
import org.example.algoplay.models.User;
import org.example.algoplay.view.HomeScreen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainMenuController {

    @FXML
    private HBox gameContainer;

    @FXML
    private Label profileText;

    @FXML
    private Button logoutButton;

    @FXML
    private StackPane profileIcon;

    // Game data structure to hold all game information
    private final Map<String, GameInfo> games = new HashMap<>();

    // Current logged-in user
    private User currentUser;

    public void initialize() {
        setupGames();
        createGameTiles();

        // Set up profile icon click
        profileIcon.setOnMouseClicked(event -> navigateToUserProfile());

        // Initially hide logout button if no user is logged in
        updateUserDisplay();
    }

    private void updateUserDisplay() {
        if (currentUser != null) {
            // User is logged in
            String username = currentUser.getUsername();
            profileText.setText(username.substring(0, 1).toUpperCase());
            logoutButton.setVisible(true);
        } else {
            // No user logged in
            profileText.setText("?");
            logoutButton.setVisible(false);
        }
    }

    @FXML
    private void handleLogout() {
        currentUser = null;
        updateUserDisplay();
    }

    @FXML
    private void navigateToUserProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/User.fxml"));
            Parent root = loader.load();

            // Get controller and set current user (if any)
            UserController userController = loader.getController();
            if (currentUser != null) {
                userController.setCurrentUser(currentUser);
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/user.css").toExternalForm());

            Stage stage = (Stage) gameContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AlgoPlay - User Profile");

        } catch (IOException e) {
            System.err.println("Error loading user profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupGames() {
        // TicTacToe
        games.put("tictactoe", new GameInfo(
                "TicTacToe",
                "High Score: 3020",
                "A classic game of X's and O's",
                "game-bg-tictactoe",
                event -> launchGame("tictactoe")
        ));

        // Tower of Hanoi
        games.put("toh", new GameInfo(
                "Tower Of Hanoi",
                "Best Time: 42s",
                "Solve the disk stacking puzzle",
                "game-bg-toh",
                event -> launchGame("toh")
        ));

        // 8 Queens Puzzle
        games.put("queens", new GameInfo(
                "8 Queens Puzzle",
                "Best Score: 95",
                "Place 8 queens without attacking each other",
                "game-bg-queens",
                event -> launchGame("queens")
        ));

        // Knights Tour
        games.put("knights", new GameInfo(
                "Knights Tour",
                "Best Path: 64 moves",
                "Visit every square once with a knight",
                "game-bg-knights",
                event -> launchGame("knights")
        ));

        // Traveling Salesman
        games.put("tsp", new GameInfo(
                "Traveling Salesman",
                "Best Route: 120km",
                "Find the shortest path visiting all cities",
                "game-bg-tsp",
                event -> launchGame("tsp")
        ));
    }

    private void createGameTiles() {
        games.forEach((key, game) -> {
            StackPane gameTile = createGameTile(game);
            gameContainer.getChildren().add(gameTile);
        });
    }

    private StackPane createGameTile(GameInfo game) {
        // Create the main tile
        StackPane tile = new StackPane();
        tile.getStyleClass().add("game-tile");
        tile.getStyleClass().add(game.backgroundStyle);

        // Create basic game info that's always visible
        VBox basicInfo = new VBox(5);
        basicInfo.setAlignment(Pos.BOTTOM_LEFT);

        Label titleLabel = new Label(game.title);
        titleLabel.getStyleClass().add("game-title");

        Label subtitleLabel = new Label(game.subtitle);
        subtitleLabel.getStyleClass().add("game-subtitle");

        basicInfo.getChildren().addAll(titleLabel, subtitleLabel);

        // Create hover overlay with more info and play button
        VBox hoverOverlay = new VBox(10);
        hoverOverlay.getStyleClass().add("game-info-overlay");
        hoverOverlay.setAlignment(Pos.CENTER);

        Label descriptionLabel = new Label(game.description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("game-subtitle");

        Button playButton = new Button("Play");
        playButton.getStyleClass().add("play-button");
        playButton.setOnAction(game.playAction);

        hoverOverlay.getChildren().addAll(descriptionLabel, playButton);

        // Add both elements to the tile
        tile.getChildren().addAll(basicInfo, hoverOverlay);

        // Add hover effect for the green border
        tile.setOnMouseEntered(event -> tile.getStyleClass().add("game-tile-selected"));
        tile.setOnMouseExited(event -> tile.getStyleClass().remove("game-tile-selected"));

        return tile;
    }

    private void launchGame(String gameId) {
        System.out.println("Launching game: " + gameId);
        try {
            FXMLLoader loader = null;
            String fxmlPath = null;

            // Determine which game to load based on the gameId
            switch (gameId) {
                case "tictactoe":
                    // This would be implemented for TicTacToe
                    fxmlPath = "/fxml/TicTacToe.fxml";
                    break;
                case "toh":
                    fxmlPath = "/fxml/toh.fxml";
                    break;
                case "queens":
                    // Launch the Eight Queens Application
                    new Thread(() -> {
                        // If MainApp.main() has already been called once in your application:
                        Platform.runLater(() -> {
                            try {
                                MainApp queensApp = new MainApp();
                                Stage newStage = new Stage();
                                queensApp.start(newStage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                        // OR if MainApp hasn't been launched yet:
                        // MainApp.main(null);
                    }).start();
                    break;
                case "knights":
                    fxmlPath = "/fxml/KTView.fxml";
                    break;
                case "tsp":
                    HomeScreen tspApp = new HomeScreen();
                    tspApp.start(new Stage());
                    break;

                default:
                    System.err.println("Unknown game ID: " + gameId);
                    return;
            }

            // Load the game FXML
            loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent gameRoot = loader.load();

            // Get controller for game-specific setup if needed
            if (gameId.equals("toh")) {
                TohController controller = loader.getController();
                // Pass the current user to the game controller
                if (currentUser != null) {
                    controller.setCurrentUser(currentUser);
                }
            }

            // Get the current stage from any control in the scene
            Stage stage = (Stage) gameContainer.getScene().getWindow();

            // Create new scene with the game content
            Scene gameScene = new Scene(gameRoot);
            gameScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            // If the game has specific stylesheets, add them
            if (gameId.equals("toh")) {
                gameScene.getStylesheets().add(getClass().getResource("/css/toh.css").toExternalForm());
            }

            // Set the new scene on the stage
            stage.setScene(gameScene);
            stage.setTitle("AlgoPlay - " + games.get(gameId).title);

        } catch (Exception e) {
            System.err.println("Error launching game: " + gameId);
            e.printStackTrace();
        }
    }

    // Inner class to hold game information
    private static class GameInfo {
        final String title;
        final String subtitle;
        final String description;
        final String backgroundStyle;
        final javafx.event.EventHandler<javafx.event.ActionEvent> playAction;

        public GameInfo(String title, String subtitle, String description,
                        String backgroundStyle,
                        javafx.event.EventHandler<javafx.event.ActionEvent> playAction) {
            this.title = title;
            this.subtitle = subtitle;
            this.description = description;
            this.backgroundStyle = backgroundStyle;
            this.playAction = playAction;
        }
    }

    // Method to set current user
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserDisplay();
    }
}