package org.example.algoplay.controllers.games.EightQueens;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.example.algoplay.database.DatabaseController;
import org.example.algoplay.database.DatabaseUtil;
import org.example.algoplay.games.EightQueens.PuzzleSolution;
import org.example.algoplay.games.EightQueens.User;
import org.example.algoplay.services.EightQueens.SequentialSolver;
import org.example.algoplay.services.EightQueens.ThreadedSolver;
import org.example.algoplay.view.EightQueens.ChessBoardView;


import java.util.List;

public class GameController {

    private Button startSequentialButton, startThreadedButton, submitAnswerButton;
    private TextField userAnswerField;
    private Text resultText;
    private ChessBoardView chessBoard;

    // Method to set the chessboard
    public void setChessBoard(ChessBoardView chessBoard) {
        this.chessBoard = chessBoard;
    }

    // Method to set the result text
    public void setResultText(Text resultText) {
        this.resultText = resultText;
    }


    // Method to set the user answer field


    // Start Sequential Solver
    public void startSequential() {
        SequentialSolver solver = new SequentialSolver();
        resultText.setText("Solving sequentially... Please wait.");
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            solver.solve();
            long endTime = System.currentTimeMillis();
            recordSolution(solver, startTime, endTime);
        }).start();
    }

    // Start Threaded Solver
    public void startThreaded() {
        ThreadedSolver solver = new ThreadedSolver();
        resultText.setText("Solving with threads... Please wait.");
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            solver.solve();
            long endTime = System.currentTimeMillis();
            recordSolution(solver, startTime, endTime);
        }).start();
    }

    // Record the solutions and time taken in the database
    private void recordSolution(Object solver, long startTime, long endTime) {
        long timeTaken = endTime - startTime;
        Platform.runLater(() -> {
            if (solver instanceof SequentialSolver) {
                SequentialSolver sequentialSolver = (SequentialSolver) solver;
                for (PuzzleSolution solution : sequentialSolver.getSolutions()) {
                    DatabaseController.saveSolution(solution, timeTaken); // Save solution with time
                }
                resultText.setText("Sequential Solutions saved: " + sequentialSolver.getSolutions().size() + " Time taken: " + timeTaken / 1000.0 + " seconds");
            } else if (solver instanceof ThreadedSolver) {
                ThreadedSolver threadedSolver = (ThreadedSolver) solver;
                for (PuzzleSolution solution : threadedSolver.getSolutions()) {
                    DatabaseController.saveSolution(solution, timeTaken); // Save solution with time
                }
                resultText.setText("Threaded Solutions saved: " + threadedSolver.getSolutions().size() + " Time taken: " + timeTaken / 1000.0 + " seconds");
            }
        });
    }

    // Submit player's answer and validate it
    // Submit player's answer and validate it

    public void submitAnswer(String playerName, int[] currentPlayerSolution) {
        if (userAnswerField == null || userAnswerField.getText().isEmpty()) {
            resultText.setText("User  answer cannot be empty.");
            return;
        }

        String userAnswer = userAnswerField.getText().trim();

        // Validate player name
        if (playerName.isEmpty()) {
            resultText.setText("Player name cannot be empty.");
            return;
        }
        // Print the solution being submitted
        System.out.println("🟢 Submitting solution: " + userAnswer + " for player: " + playerName);
        // Validate answer correctness
        if (DatabaseUtil.isCorrectAnswer(userAnswer)) {
            User user = new User(playerName, currentPlayerSolution); // Pass the currentPlayerSolution
            DatabaseController.saveUserSolution(user); // Save the user solution
            resultText.setText("Correct! Well done, " + playerName);
            System.out.println("✅ Solution submitted successfully: " + user.getPositions());
        } else {
            resultText.setText("Incorrect answer. Try again.");
            System.out.println("🔴 Incorrect solution submitted: " + userAnswer);
        }
    }


    // Reset the game
    public void resetGame() {
        if (userAnswerField != null) {
            userAnswerField.clear();
        }
        if (resultText != null) {
            resultText.setText("Game reset. Try again !");
        }

        // Reset solution flags in the database
        DatabaseController.resetAllSolutionFlags();
    }

    // Fetch all solutions from the database
    public void fetchSolutions() {
        List<String> solutions = DatabaseController.getAllSolutions();
        if (solutions.isEmpty()) {
            resultText.setText("No recognized solutions found.");
        } else {
            resultText.setText("Found " + solutions.size() + " recognized solutions.");
        }
    }
}