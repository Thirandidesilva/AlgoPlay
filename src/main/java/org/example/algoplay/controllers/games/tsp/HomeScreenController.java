package org.example.algoplay.controllers.games.tsp;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.algoplay.view.MvMPage;
import org.example.algoplay.view.PvMPage;


import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;


public class HomeScreenController implements Initializable {

    private static final Color PRIMARY_COLOR = Color.rgb(73, 255, 73); // Bright green
    private static final Color SECONDARY_COLOR = Color.rgb(30, 144, 255); // Dodger blue

    @FXML private StackPane root;
    @FXML private BorderPane mainContent;
    @FXML private Label logoLabel;
    @FXML private Label subtitleLabel;
    @FXML private Button homeButton;
    @FXML private Button algorithmsButton;
    @FXML private Button aboutButton;
    @FXML private Circle profileIcon;
    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;
    @FXML private Text titleLabel;
    @FXML private Text subtitleText;
    @FXML private VBox pvmCard;
    @FXML private Button pvmStartButton;
    @FXML private VBox mvmCard;
    @FXML private Button mvmStartButton;
    @FXML private Label infoTitle;
    @FXML private Text infoText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set background for the entire scene
        root.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 10), CornerRadii.EMPTY, Insets.EMPTY)));

        // Add stars to the background with twinkling effect
        Pane starLayer = createStarryBackground(1200, 800);
        root.getChildren().add(0, starLayer); // Insert at index 0 to be behind mainContent

        // Apply styling to elements
        applyStyles();

        // Add fadeIn animation when the application starts
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), mainContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void applyStyles() {
        // Style top bar
        HBox topBar = (HBox) mainContent.getTop();
        topBar.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 20, 0.7), new CornerRadii(0, 0, 15, 15, false), Insets.EMPTY)));

        // Style logo
        logoLabel.setTextFill(PRIMARY_COLOR);
        Glow glow = new Glow();
        glow.setLevel(0.4);
        logoLabel.setEffect(glow);
        subtitleLabel.setTextFill(Color.LIGHTGRAY);

        // Style navigation buttons
        styleNavButton(homeButton);
        styleNavButton(algorithmsButton);
        styleNavButton(aboutButton);

        // Style profile section
        profileIcon.setFill(Color.LIGHTGRAY);
        usernameLabel.setTextFill(Color.WHITE);

        // Style action buttons
        styleActionButton(logoutButton);
        styleActionButton(pvmStartButton);
        styleActionButton(mvmStartButton);

        // Style title
        titleLabel.setFill(PRIMARY_COLOR);
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(73, 255, 73, 0.5));
        dropShadow.setRadius(15);
        titleLabel.setEffect(dropShadow);

        // Style subtitle
        subtitleText.setFill(Color.LIGHTGRAY);

        // Style cards
        styleModeCard(pvmCard);
        styleModeCard(mvmCard);

        // Style info panel
        VBox infoPanel = (VBox) mainContent.getBottom();
        infoPanel.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 20, 0.7), new CornerRadii(15, 15, 0, 0, false), Insets.EMPTY)));

        infoTitle.setTextFill(PRIMARY_COLOR);
        infoText.setFill(Color.LIGHTGRAY);
    }

    private void styleNavButton(Button button) {
        button.setTextFill(Color.LIGHTGRAY);
        button.setBackground(Background.EMPTY);

        // Hover effect
        button.setOnMouseEntered(e -> button.setTextFill(PRIMARY_COLOR));
        button.setOnMouseExited(e -> button.setTextFill(Color.LIGHTGRAY));
    }

    private void styleActionButton(Button button) {
        button.setPrefWidth(150);
        button.setPrefHeight(40);

        // Set background
        button.setStyle("-fx-background-color: rgba(73, 255, 73, 0.2); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: #49ff49; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 20; " +
                "-fx-text-fill: #49ff49;");

        // Add hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: rgba(73, 255, 73, 0.4); " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #49ff49; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 20; " +
                    "-fx-text-fill: white;");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: rgba(73, 255, 73, 0.2); " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #49ff49; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 20; " +
                    "-fx-text-fill: #49ff49;");
        });

        // Add press effect
        button.setOnMousePressed(e -> {
            button.setStyle("-fx-background-color: rgba(73, 255, 73, 0.6); " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #49ff49; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 20; " +
                    "-fx-text-fill: white;");
        });

        button.setOnMouseReleased(e -> {
            button.setStyle("-fx-background-color: rgba(73, 255, 73, 0.4); " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #49ff49; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 20; " +
                    "-fx-text-fill: white;");
        });
    }

    private void styleModeCard(VBox card) {
        card.setBackground(new Background(new BackgroundFill(
                Color.rgb(20, 20, 40, 0.7), new CornerRadii(15), Insets.EMPTY)));

        // Add border
        card.setBorder(new Border(new BorderStroke(
                Color.rgb(73, 255, 73, 0.3), BorderStrokeStyle.SOLID,
                new CornerRadii(15), new BorderWidths(1))));

        // Get labels from cards and style them
        Label titleLabel = (Label) card.getChildren().get(0);
        Label descLabel = (Label) card.getChildren().get(1);

        titleLabel.setTextFill(Color.WHITE);
        descLabel.setTextFill(Color.LIGHTGRAY);

        // Add hover effect for the card
        DropShadow hover = new DropShadow();
        hover.setColor(PRIMARY_COLOR);
        hover.setRadius(20);

        card.setOnMouseEntered(e -> {
            card.setEffect(hover);
            card.setBackground(new Background(new BackgroundFill(
                    Color.rgb(30, 30, 60, 0.8), new CornerRadii(15), Insets.EMPTY)));
        });

        card.setOnMouseExited(e -> {
            card.setEffect(null);
            card.setBackground(new Background(new BackgroundFill(
                    Color.rgb(20, 20, 40, 0.7), new CornerRadii(15), Insets.EMPTY)));
        });
    }

    private Pane createStarryBackground(double width, double height) {
        Pane background = new Pane();
        background.setPrefSize(width, height);

        // Add stars with twinkling effect
        Random random = new Random();

        // Create different sized stars
        for (int i = 0; i < 500; i++) { // Reduced number of stars to improve performance
            double size = random.nextDouble() * 2.5 + 0.5;
            double opacity = random.nextDouble() * 0.5 + 0.3;

            // Create a star
            Rectangle star = new Rectangle(size, size);
            star.setFill(Color.WHITE);

            // Random position
            double x = random.nextDouble() * width;
            double y = random.nextDouble() * height;

            star.setLayoutX(x);
            star.setLayoutY(y);
            star.setOpacity(opacity);

            background.getChildren().add(star);

            // Add twinkling effect to some stars
            if (random.nextDouble() > 0.7) {
                Timeline twinkle = new Timeline(
                        new KeyFrame(Duration.ZERO, e -> {}),
                        new KeyFrame(Duration.seconds(random.nextDouble() * 2 + 1), e -> {
                            star.setOpacity(random.nextDouble() * 0.5 + 0.3);
                        })
                );
                twinkle.setCycleCount(Timeline.INDEFINITE);
                twinkle.play();
            }
        }

        return background;
    }

    @FXML
    private void handleHomeButton() {
        System.out.println("Home button clicked");
    }

    @FXML
    private void handleAlgorithmsButton() {
        System.out.println("Algorithms button clicked");
    }

    @FXML
    private void handleAboutButton() {
        System.out.println("About button clicked");
    }

    @FXML
    private void handleLogoutButton() {
        System.out.println("Logout button clicked");
    }

    @FXML
    private void launchPvMScreen() {
        try {
            Stage stage = new Stage();
            PvMPage pvmPage = new PvMPage(stage);
            pvmPage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void launchMvMScreen() {
        try {
            Stage currentStage = (Stage) root.getScene().getWindow();

            // Instead of creating a new MvMPage instance, directly create the MvMPageView
            Stage newStage = new Stage();
            MvMPage view = new MvMPage(newStage);
            view.show();

            currentStage.close();
        } catch (Exception ex) {
            System.err.println("Failed to load Machine vs Machine screen");
            ex.printStackTrace();
        }
    }
}