package org.example.algoplay.games.tsp;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class HomeScreen {

    private static final Color PRIMARY_COLOR = Color.rgb(73, 255, 73); // Bright green
    private static final Color SECONDARY_COLOR = Color.rgb(30, 144, 255); // Dodger blue
    private Scene scene;

    public HomeScreen() {
        // Create the root layout
        StackPane root = new StackPane();

        // Set background for the entire scene
        root.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 10), CornerRadii.EMPTY, Insets.EMPTY)));

        // Add stars to the background with twinkling effect
        Pane starLayer = createStarryBackground(1200, 800);
        root.getChildren().add(starLayer);

        // Create main content container
        BorderPane mainContent = new BorderPane();
        mainContent.setBackground(Background.EMPTY);

        // Create top navigation bar with glass effect
        HBox topBar = createTopBar();
        mainContent.setTop(topBar);

        // Create center content
        VBox centerContent = createCenterContent();
        mainContent.setCenter(centerContent);

        // Add the main content on top of the starry background
        root.getChildren().add(mainContent);

        // Add information panel at the bottom
        VBox infoPanel = createInfoPanel();
        mainContent.setBottom(infoPanel);

        // Scene setup
        scene = new Scene(root, 1200, 800);

        // Add fadeIn animation when the application starts
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), mainContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public Scene getScene() {
        return scene;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(15);

        // Semi-transparent background for the top bar
        topBar.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 20, 0.7), new CornerRadii(0, 0, 15, 15, false), Insets.EMPTY)));

        // Logo with glow effect
        Label logoLabel = new Label("AlgoPlay");
        logoLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 28));
        logoLabel.setTextFill(PRIMARY_COLOR);

        Glow glow = new Glow();
        glow.setLevel(0.4);
        logoLabel.setEffect(glow);

        // Create a subtitle
        Label subtitleLabel = new Label("Space Algorithms");
        subtitleLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);

        VBox logoBox = new VBox(3);
        logoBox.getChildren().addAll(logoLabel, subtitleLabel);

        // Create navigation buttons
        Button homeButton = createNavButton("Home");
        Button algorithmsButton = createNavButton("Algorithms");
        Button aboutButton = createNavButton("About");

        // Add space between logo and logout button
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Profile indicator
        Circle profileIcon = new Circle(18, Color.LIGHTGRAY);
        Label usernameLabel = new Label("User123");
        usernameLabel.setTextFill(Color.WHITE);

        HBox profileBox = new HBox(10);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.getChildren().addAll(profileIcon, usernameLabel);

        // Logout button with improved styling
        Button logoutButton = new Button("Log Out");
        styleActionButton(logoutButton);

        topBar.getChildren().addAll(logoBox, homeButton, algorithmsButton, aboutButton, spacer,
                profileBox, logoutButton);

        return topBar;
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setTextFill(Color.LIGHTGRAY);
        button.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        button.setBackground(Background.EMPTY);

        // Hover effect
        button.setOnMouseEntered(e -> button.setTextFill(PRIMARY_COLOR));
        button.setOnMouseExited(e -> button.setTextFill(Color.LIGHTGRAY));

        return button;
    }

    private VBox createCenterContent() {
        VBox centerContent = new VBox(50);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(50, 20, 50, 20));

        // Title with animation
        Text titleLabel = new Text("Traveling Salesman Problem");
        titleLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 44));
        titleLabel.setFill(PRIMARY_COLOR);

        // Add drop shadow for the title
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(73, 255, 73, 0.5));
        dropShadow.setRadius(15);
        titleLabel.setEffect(dropShadow);

        // Add subtitle with description
        Text subtitleText = new Text("Solve the classic optimization problem in different modes");
        subtitleText.setFont(Font.font("Monospace", FontWeight.NORMAL, 18));
        subtitleText.setFill(Color.LIGHTGRAY);
        subtitleText.setTextAlignment(TextAlignment.CENTER);

        // Create buttons container with better layout
        HBox buttonContainer = new HBox(30);
        buttonContainer.setAlignment(Pos.CENTER);

        // Create enhanced mode selection cards
        VBox pvmCard = createModeCard("Player vs Machine",
                "Test your skills against\ndifferent algorithms",
                e -> launchPvMScreen());

        VBox mvmCard = createModeCard("Machine vs Machine",
                "Compare algorithm\nperformance and efficiency",
                e -> launchMvMScreen());

        buttonContainer.getChildren().addAll(pvmCard, mvmCard);

        centerContent.getChildren().addAll(titleLabel, subtitleText, buttonContainer);

        return centerContent;
    }

    private VBox createModeCard(String title, String description, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setPrefWidth(300);
        card.setBackground(new Background(new BackgroundFill(
                Color.rgb(20, 20, 40, 0.7), new CornerRadii(15), Insets.EMPTY)));

        // Add border
        card.setBorder(new Border(new BorderStroke(
                Color.rgb(73, 255, 73, 0.3), BorderStrokeStyle.SOLID,
                new CornerRadii(15), new BorderWidths(1))));

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        // Description
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.LIGHTGRAY);
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);

        // Button
        Button startButton = new Button("Start");
        styleActionButton(startButton);
        startButton.setOnAction(action);

        card.getChildren().addAll(titleLabel, descLabel, startButton);

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

        return card;
    }

    private void styleActionButton(Button button) {
        button.setPrefWidth(150);
        button.setPrefHeight(40);
        button.setFont(Font.font("Monospace", FontWeight.BOLD, 16));

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

    private VBox createInfoPanel() {
        VBox infoPanel = new VBox(10);
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.setPadding(new Insets(15));
        infoPanel.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 20, 0.7), new CornerRadii(15, 15, 0, 0, false), Insets.EMPTY)));

        // TSP information
        Label infoTitle = new Label("What is the Traveling Salesman Problem?");
        infoTitle.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        infoTitle.setTextFill(PRIMARY_COLOR);

        Text infoText = new Text("The TSP asks the question: Given a list of cities and the distances between each pair of cities, " +
                "what is the shortest possible route that visits each city exactly once and returns to the origin city?");
        infoText.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        infoText.setFill(Color.LIGHTGRAY);
        infoText.setWrappingWidth(800);
        infoText.setTextAlignment(TextAlignment.CENTER);

        infoPanel.getChildren().addAll(infoTitle, infoText);

        return infoPanel;
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

    private void launchPvMScreen() {
        try {
            Stage currentStage = (Stage) scene.getWindow();
            PvMScreen pvmScreen = new PvMScreen();
            Stage newStage = new Stage();
            pvmScreen.start(newStage);
            currentStage.close();
        } catch (Exception ex) {
            System.err.println("Failed to load Player vs Machine screen");
            ex.printStackTrace();
        }
    }

    private void launchMvMScreen() {
        try {
            Stage currentStage = (Stage) scene.getWindow();
            MvMPage mvmPage = new MvMPage();
            Stage newStage = new Stage();
            mvmPage.start(newStage);
            currentStage.close();
        } catch (Exception ex) {
            System.err.println("Failed to load Machine vs Machine screen");
            ex.printStackTrace();
        }
    }
}