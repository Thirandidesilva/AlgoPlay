package org.example.algoplay.games.tsp;/*package tsp;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class TutorialManager {

    private PvMScreen mainScreen;
    private StackPane overlayPane;
    private VBox tutorialBox;
    private Label messageLabel;
    private Button nextButton;
    private int currentStep = 0;
    private boolean tutorialActive = false;

    // Array of tutorial steps with messages and targeted nodes
    private TutorialStep[] tutorialSteps;

    public TutorialManager(PvMScreen mainScreen) {
        this.mainScreen = mainScreen;
        initializeTutorialComponents();
        setupTutorialSteps();
    }

    private void initializeTutorialComponents() {
        // Create overlay pane that will be added to the main scene
        overlayPane = new StackPane();
        overlayPane.setMouseTransparent(false);
        overlayPane.setVisible(false);

        // Semi-transparent overlay
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.rgb(0, 0, 0, 0.7));
        overlay.widthProperty().bind(overlayPane.widthProperty());
        overlay.heightProperty().bind(overlayPane.heightProperty());

        // Tutorial message box
        tutorialBox = new VBox(10);
        tutorialBox.setAlignment(Pos.CENTER);
        tutorialBox.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20; " +
                "-fx-background-radius: 10; -fx-border-color: #56eb61; " +
                "-fx-border-width: 2; -fx-border-radius: 10;");
        tutorialBox.setMaxWidth(400);
        tutorialBox.setMaxHeight(250);

        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        nextButton = new Button("Next");
        nextButton.setStyle("-fx-background-color: #56eb61; -fx-text-fill: black; " +
                "-fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 5;");
        nextButton.setOnAction(e -> nextTutorialStep());

        tutorialBox.getChildren().addAll(messageLabel, nextButton);

        overlayPane.getChildren().addAll(overlay, tutorialBox);
    }

    private void setupTutorialSteps() {
        tutorialSteps = new TutorialStep[] {
                new TutorialStep(
                        "Welcome to the TSP Challenge! This tutorial will guide you through the basics.",
                        null
                ),
                new TutorialStep(
                        "The TSP (Traveling Salesman Problem) is about finding the shortest route that visits all cities exactly once.",
                        null
                ),
                new TutorialStep(
                        "This is your puzzle canvas. It shows the cities you need to visit.",
                        mainScreen.getPuzzleCanvas()
                ),
                new TutorialStep(
                        "Click cities in order to create your solution path.",
                        mainScreen.getSolutionCanvas()
                ),
                new TutorialStep(
                        "Use the difficulty slider to change the number of cities in the puzzle.",
                        mainScreen.getDifficultySetting()
                ),
                new TutorialStep(
                        "After visiting all cities, click 'Solve' to compare your solution with algorithms.",
                        mainScreen.getSolveBtn()
                ),
                new TutorialStep(
                        "You'll see performance stats for different algorithms here.",
                        mainScreen.getResultsContainer()
                ),
                new TutorialStep(
                        "That's it! Click 'Next Puzzle' to start playing, or try again with 'Reset'.",
                        mainScreen.getNextPuzzleBtn()
                )
        };
    }

    public void startTutorial() {
        tutorialActive = true;
        currentStep = 0;
        mainScreen.setShowingTutorial(true);
        showTutorialStep(currentStep);
        overlayPane.setVisible(true);
    }

    public void endTutorial() {
        tutorialActive = false;
        mainScreen.setShowingTutorial(false);
        overlayPane.setVisible(false);

        // Reset any highlighted elements
        if (currentStep > 0 && currentStep <= tutorialSteps.length &&
                tutorialSteps[currentStep - 1].targetNode != null) {
            removeHighlight(tutorialSteps[currentStep - 1].targetNode);
        }
    }

    private void nextTutorialStep() {
        // Remove highlight from current step if needed
        if (currentStep < tutorialSteps.length && tutorialSteps[currentStep].targetNode != null) {
            removeHighlight(tutorialSteps[currentStep].targetNode);
        }

        currentStep++;

        if (currentStep >= tutorialSteps.length) {
            // End of tutorial
            endTutorial();
            return;
        }

        showTutorialStep(currentStep);
    }

    private void showTutorialStep(int stepIndex) {
        TutorialStep step = tutorialSteps[stepIndex];
        messageLabel.setText(step.message);

        // If this is the last step, change button text
        if (stepIndex == tutorialSteps.length - 1) {
            nextButton.setText("Finish");
        } else {
            nextButton.setText("Next");
        }

        // Highlight target element if specified
        if (step.targetNode != null) {
            highlightElement(step.targetNode);
        }
    }

    private void highlightElement(Node node) {
        // Create highlight effect
        node.setStyle(node.getStyle() + "; -fx-effect: dropshadow(gaussian, #56eb61, 15, 0.7, 0, 0);");

        // Add pulse animation
        Timeline pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> node.setOpacity(0.7)),
                new KeyFrame(Duration.millis(500), e -> node.setOpacity(1.0)),
                new KeyFrame(Duration.millis(1000), e -> node.setOpacity(0.7))
        );
        pulseTimeline.setCycleCount(Timeline.INDEFINITE);
        pulseTimeline.play();

        // Store the timeline in the node's properties to stop it later
        node.getProperties().put("tutorialAnimation", pulseTimeline);
    }

    private void removeHighlight(Node node) {
        // Remove the glow effect
        String style = node.getStyle();
        if (style != null) {
            style = style.replaceAll("-fx-effect: dropshadow\\(gaussian, #56eb61, 15, 0.7, 0, 0\\);", "");
            node.setStyle(style);
        }

        // Stop animation if there is one
        if (node.getProperties().containsKey("tutorialAnimation")) {
            Timeline timeline = (Timeline) node.getProperties().get("tutorialAnimation");
            timeline.stop();
            node.getProperties().remove("tutorialAnimation");
        }

        // Reset opacity
        node.setOpacity(1.0);
    }

    public StackPane getOverlayPane() {
        return overlayPane;
    }

    public boolean isTutorialActive() {
        return tutorialActive;
    }

    // Helper class to store tutorial step information
    private static class TutorialStep {
        String message;
        Node targetNode;

        TutorialStep(String message, Node targetNode) {
            this.message = message;
            this.targetNode = targetNode;
        }
    }
}*/