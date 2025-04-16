package org.example.algoplay.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class MainMenuController {

    @FXML
    private HBox gameContainer;

    // Game data structure to hold all game information
    private final Map<String, GameInfo> games = new HashMap<>();

    public void initialize() {
        setupGames();
        createGameTiles();
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
        // Implement game launching logic here
        // This would typically load the appropriate FXML for the selected game
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
}