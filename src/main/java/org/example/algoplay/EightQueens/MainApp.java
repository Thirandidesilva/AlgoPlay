package org.example.algoplay.EightQueens;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


import org.example.algoplay.controllers.games.EightQueens.GameController;
import org.example.algoplay.database.DatabaseController;
import org.example.algoplay.database.DatabaseUtil;
import org.example.algoplay.database.CommonSolutionInserter;
import org.example.algoplay.games.EightQueens.User;
import org.example.algoplay.view.EightQueens.ChessBoardView;


import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class MainApp extends Application {

    private Stage primaryStage; // Store the primary stage
    private VBox mainMenu; // Main menu layout

    @Override

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Initialize the primary stage
        primaryStage.setTitle("Eight Queens Puzzle");
        // Create the main menu
        createMainMenu();
        // Set up the scene for the main menu
        Scene scene = new Scene(mainMenu, 600, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
        printMessages();

    }


    private void createMainMenu() {

        mainMenu = new VBox(20);
        mainMenu.setAlignment(Pos.CENTER);
        mainMenu.setPadding(new Insets(20));


//          Set image for bg
//        Image backgroundImage = new Image("file:///C:/Users/thiya/IdeaProjects/EightQueensPuzzle/src/main/resources/images/your_image.jpg"); // Replace 'your_image.jpg' with the actual image file name
//        BackgroundImage background = new BackgroundImage(backgroundImage,
//                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
//                BackgroundPosition.DEFAULT,
//                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false));


        // Set the background image to the main menu
//        mainMenu.setBackground(new Background(background));



        // Set a background color for the main menu

        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY);
        mainMenu.setBackground(new Background(backgroundFill));


        Label titleLabel = new Label("Welcome to Eight Queens Puzzle");
        titleLabel.setFont(new Font("Comic Sans", 28));
        titleLabel.setStyle("-fx-text-fill: black;");


        // Create buttons
        Button startButton = new Button("Start Game");
        Button exitButton = new Button("Exit");

        // Style buttons
        styleButton(startButton);
        styleButton(exitButton);


        // Set button actions
        startButton.setOnAction(event -> startGame());
        exitButton.setOnAction(event -> primaryStage.close());


        // Add components to the main menu
        mainMenu.getChildren().addAll(titleLabel, startButton, exitButton);

    }


    private void styleButton(Button button) {

        button.setFont(new Font("Brush Script MT", 18));
        button.setStyle("-fx-background-color: darkblue; " +

                "-fx-text-fill: white; " +
                "-fx-padding: 10px 20px; " + // Vertical and horizontal padding
                "-fx-background-radius: 5px; " +
                "-fx-border-color: white; " +
                "-fx-border-radius: 5px; " +
                "-fx-border-width: 2px;"); // Border width


        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: blue; " +

                "-fx-text-fill: white; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 5px; " +
                "-fx-border-color: white; " +
                "-fx-border-radius: 5px; " +
                "-fx-border-width: 2px;"));

        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: darkblue; " +

                "-fx-text-fill: white; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 5px; " +
                "-fx-bordercolor: white; " +
                "-fx-border-radius: 5px; " +
                "-fx-border-width: 2px;"));

    }
    private void startGame() {


        // Create the ChessBoardView
        ChessBoardView chessBoard = new ChessBoardView();

        // Game Title
        Label titleLabel = new Label("Eight Queens Puzzle");
        titleLabel.setFont(new Font("Arial", 28));
        titleLabel.setStyle("-fx-text-fill: black;");
        titleLabel.setAlignment(Pos.CENTER);

        // Player name input
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        // Create an instance of GameController
        GameController gameController = new GameController();
        gameController.setChessBoard(chessBoard); // Pass the chess board to the controller
        System.out.println("✅ GameController initialized.");

        Text resultText = new Text();
        gameController.setResultText(resultText);

        // Submit button
        Button submitButton = new Button("Submit Solution");
        submitButton.setOnAction(e -> handleSubmitButtonAction(nameField, chessBoard, gameController));

        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            chessBoard.clearBoard();
            chessBoard.showMessage("");
            System.out.println("🔄 Game reset.");
        });

        // Spacer to push right side content
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Controls box
        HBox controlsBox = new HBox(10);
        controlsBox.setPadding(new Insets(10));
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        controlsBox.getChildren().addAll(resetButton, spacer, nameField, submitButton);

        // Root layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().addAll(titleLabel, chessBoard, controlsBox);

        // Set up the scene
        Scene scene = new Scene(root, 600, 720);
        primaryStage.setTitle("Eight Queens Puzzle");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Print messages to ensure they show in the console
        printMessages();
    }


    private void handleSubmitButtonAction(TextField nameField, ChessBoardView chessBoard, GameController gameController) {
        try {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                chessBoard.showAlert("Please enter your name."); // Alert for empty name
                return;
            } else if (playerName.length() > 30) {
                chessBoard.showAlert("Name cannot be longer than 30 characters."); // Alert for name too long
                return;
            }
            if (chessBoard.countQueens() != 8) {
                chessBoard.showAlert("You must place exactly 8 queens on the board."); // Alert for incorrect number of queens
                return;
            }
            System.out.println("Submit button clicked. Player name: " + playerName); // Debug print

            if (chessBoard.isCurrentSolutionCorrect()) {
                int[] currentSolution = chessBoard.getCurrentPlayerSolution();

                String solutionString = java.util.Arrays.toString(currentSolution).replaceAll("[\\[\\] ]", ""); // Convert to string format
                solutionString = "[" + solutionString + "]";
                System.out.println("🟢 User is submitting: " + solutionString + " for player: " + playerName);

                // Check if the solution already exists
                int solutionId = DatabaseController.saveSolution(solutionString);
                if (solutionId == -1) {
                    // Show alert if the solution is recognized
                    chessBoard.showAlert("Solution already recognized This solution has already been recognized. Please find another answer.");
                    return; // Exit the method early
                }

                // Proceed to save the user solution with the recognized solution ID
                User user = new User(playerName, currentSolution);
                user.setSolutionId(solutionId); // Set the solution ID for the user
                DatabaseController.saveUserSolution(user); // Save the user solution

                boolean isCorrect = DatabaseUtil.isCorrectAnswer(solutionString);
                if (isCorrect) {
                    System.out.println("🟢 Submitting solution: " + solutionString + " for player: " + playerName);
                    System.out.println("Solution submitted successfully!");
                    chessBoard.showSuccessAlert("✅ Your Solution is correct");
                } else {
                    System.out.println("🔴 Invalid submission: Solution is incorrect."); // Debug print
                    chessBoard.showError("❌ Incorrect solution. Please try again.");
                }
            } else {
                System.out.println("Something went wrong! Please check"); // Debug print
                chessBoard.showError("Invalid submission: Solution is incorrect.");
            }
        } catch (IllegalArgumentException e) {
            // Handle specific exceptions related to illegal arguments
            e.printStackTrace();
            chessBoard.showAlert("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace for debugging
            chessBoard.showAlert("An error occurred while processing your submission. Please try again."); // Show alert to user
        }
    }

    private void printMessages() {
        Platform.runLater(() -> {
            System.out.println("Application started successfully!");
            System.out.flush(); // Ensure the output is flushed and displayed immediately
        });
    }

    public static void main(String[] args) {
//        DatabaseController.setTestMode();
        // Set up System.out to ensure the output goes to the correct stream
        DatabaseController.testConnection();

        long timeTaken = org.example.algoplay.games.EightQueens.SequentialEightQueensSolver.saveSequentialResults();
        long timeTaken2 = org.example.algoplay.games.EightQueens.ThreadedEightQueensSolver.saveThreadedResults();

        if (timeTaken < timeTaken2) {
            System.out.println(" Threaded solver is faster than sequential Solver");
        } else if (timeTaken2 < timeTaken) {
            System.out.println(" Sequential solver is faster than Threaded Solver");
        } else {
            System.out.println("⚖️ Both solvers took the same amount of time.");
        }

        CommonSolutionInserter.insertCommonSolutions();
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
//        resetAllTables();
        launch(args);
    }
}