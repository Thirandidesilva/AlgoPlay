package org.example.algoplay.database;

import javafx.application.Platform;
import javafx.scene.control.Alert;


import org.example.algoplay.games.EightQueens.ExecutionTime;
import org.example.algoplay.games.EightQueens.PuzzleSolution;
import org.example.algoplay.games.EightQueens.User;


import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseController {

    static String DB_URL = "jdbc:postgresql://localhost:5432/eight_queens_puzzle";
    static String DB_USER = "postgres";
    static String DB_PASSWORD = "Thiya@2003";

    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            if (connection != null) {
                Platform.runLater(() -> {
                    System.out.println("✅ Database connection established successfully.");
                    System.out.flush();
                });
            } else {
                Platform.runLater(() -> {
                    System.err.println("❌ Database connection failed.");
                    System.out.flush();
                });
            }
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Failed to connect to database.");
                e.printStackTrace();
                System.out.flush();
            });
        }
    }

    public static void setTestMode() {

        DB_URL = "jdbc:h2:mem:testdb"; // In-memory database

        DB_USER = "sa"; // Default user for H2

        DB_PASSWORD = ""; // No password for H2

        testConnection(); // Test the connection to the H2 database

    }


    public static void testConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Database connection is active and working.");
            } else {
                System.out.println("❌ Database connection is not active.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error testing database connection:");
//            e.printStackTrace();
        }
    }

    // Save sequential solutions from solver
    public static void saveSequentialSolution(String solution, long timeTakenMs) {
        String sql = "INSERT INTO sequential_solutions (positions, time_taken_ms) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, solution);
            stmt.setLong(2, timeTakenMs);
            int rows = stmt.executeUpdate();
            Platform.runLater(() -> {
                if (rows > 0) {
//                    System.out.println("✅ Saved sequential solution: " + solution);
                } else {
                    System.err.println("❌ Failed to save sequential solution: " + solution);
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Error saving sequential solution: " + solution);
                e.printStackTrace();
            });
        }
    }


    public static int saveSolution(String solution) {
        // First, check if the solution already exists
        String checkSql = "SELECT id, recognized FROM public.puzzle_solutions WHERE solution = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, solution);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                // Solution already exists
                boolean isRecognized = rs.getBoolean("recognized");
                if (isRecognized) {
                    // If the solution is recognized, return -1 to indicate it cannot be added again
                    return -2; // Indicate that the solution is recognized
                } else {
                    // If the solution exists but is not recognized, update it to recognized
                    int existingId = rs.getInt("id");
                    updateSolutionRecognition(existingId); // Update recognized field
                    System.out.println("Updated solution recognition for existing ID: " + existingId);
                    return existingId; // Return the existing id
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If the solution does not exist, insert it
        String insertSql = "INSERT INTO public.puzzle_solutions (solution) VALUES (?) RETURNING id";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setString(1, solution);
            ResultSet rs = insertStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id"); // Return the generated id
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if the insertion failed
    }



    private static void updateSolutionRecognition(int solutionId) {
        String updateSql = "UPDATE public.puzzle_solutions SET recognized = true WHERE id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            updateStmt.setInt(1, solutionId);
            updateStmt.executeUpdate();
            System.out.println("✅ Updated solution recognition for ID: " + solutionId);

            // Check if all solutions are recognized
            if (areAllSolutionsRecognized()) {
                resetRecognizedFlags(); // Reset all recognized flags
                showAllSolutionsFoundMessage(); // Show message to the user
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean areAllSolutionsRecognized() {
        String countSql = "SELECT COUNT(*) FROM public.puzzle_solutions WHERE recognized = true";
        try (PreparedStatement countStmt = connection.prepareStatement(countSql)) {
            ResultSet rs = countStmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count >= 92; // Check if the count of recognized solutions is 92
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void resetRecognizedFlags() {
        String resetSql = "UPDATE public.puzzle_solutions SET recognized = false";
        try (PreparedStatement resetStmt = connection.prepareStatement(resetSql)) {
            resetStmt.executeUpdate();
            System.out.println("✅ Reset all recognized flags to false.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showAllSolutionsFoundMessage() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("All Solutions Found");
            alert.setHeaderText(null);
            alert.setContentText("All 92 solutions have been found!");
            alert.showAndWait();
        });
    }

    public static void saveSolution(PuzzleSolution solution, long timeTaken) {

        String sql = "INSERT INTO solutions (positions, time_taken) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, solution.getPositions()); // Save the formatted 1D array

            stmt.setLong(2, timeTaken);

            stmt.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public static void saveUserSolution(User user) {
        // First, save the solution and get its ID
        String solutionString = Arrays.toString(user.getPositions()).replaceAll(" ", ""); // Convert to string format
        int solutionId = saveSolution(solutionString); // Save the solution and get the ID

        String sql = "INSERT INTO public.players (name, positions) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getName()); // Set the player's name
            stmt.setString(2, solutionString); // Set the formatted solution string
//            stmt.setObject(3, solutionId == -1 ? null : solutionId); // Set the solution_id, or null if insertion failed

            System.out.println("Attempting to save user solution: " + solutionString + " for player: " + user.getName());

            int rows = stmt.executeUpdate(); // Execute the insert statement

            // Create final copies of the variables to use in the lambda expression
            final String finalSolutionString = solutionString;
            final String finalPlayerName = user.getName();

            Platform.runLater(() -> {
                if (rows > 0) {
                    System.out.println("✅ Saved user solution: " + finalSolutionString + " for player: " + finalPlayerName);
                } else {
                    System.err.println("❌ Failed to save user solution for player: " + finalPlayerName);
                }
            });

        } catch (SQLException e) {
            // Create final copy of the player's name for use in the lambda
            final String finalPlayerName = user.getName();
            Platform.runLater(() -> {
                System.err.println("❌ Error saving user solution for player: " + finalPlayerName);
                e.printStackTrace();
            });
        }
    }


    public static List<String> getAllSolutions() {
        String sql = "SELECT solution FROM puzzle_solutions WHERE recognized = true";  // Retrieve recognized solutions
        List<String> solutions = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String solution = rs.getString("solution");
                solutions.add(solution);
            }

            Platform.runLater(() -> {
                if (solutions.isEmpty()) {
                    System.out.println("❌ No recognized solutions found.");
                } else {
                    System.out.println("✅ Found " + solutions.size() + " recognized solutions.");
                }
            });

        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Error retrieving solutions.");
                e.printStackTrace();
            });
        }

        return solutions;
    }


    // Save threaded solutions
    public static void saveThreadedSolution(String solution, long timeTakenMs) {
        String sql = "INSERT INTO threaded_solutions (positions, time_taken_ms) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, solution);
            stmt.setLong(2, timeTakenMs);
            int rows = stmt.executeUpdate();
            Platform.runLater(() -> {
                if (rows > 0) {
//                    System.out.println("✅ Saved threaded solution: " + solution);
                } else {
                    System.err.println("❌ Failed to save threaded solution: " + solution);
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Error saving threaded solution: " + solution);
                e.printStackTrace();
            });
        }
    }
    public static void resetAllSolutionFlags() {
        String sql = "UPDATE puzzle_solutions SET recognized = false"; // Resetting the 'recognized' flag to false
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int rowsUpdated = stmt.executeUpdate();
            Platform.runLater(() -> {
                if (rowsUpdated > 0) {
                    System.out.println("✅ Successfully reset solution flags. Rows affected: " + rowsUpdated);
                } else {
                    System.err.println("❌ No solutions found to reset flags.");
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Error resetting solution flags.");
                e.printStackTrace();
            });
        }
    }


    // Check if a user-entered solution is correct
    public static boolean isCorrectAnswer(String userAnswer) {
        String sql = "SELECT COUNT(*) FROM puzzle_solutions WHERE solution = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userAnswer);
            ResultSet rs = stmt.executeQuery();
            boolean result = rs.next() && rs.getInt(1) > 0;
            Platform.runLater(() -> {
                System.out.println("🔍 Checked answer: " + userAnswer + " | ✅ Correct: " + result);
                System.out.flush();
            });
            return result;
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Error checking answer: " + userAnswer);
                e.printStackTrace();
                System.out.flush();
            });
            return false;
        }
    }

    public static void saveExecutionTime(ExecutionTime time) {
        String sql = "INSERT INTO execution_times (solution_id, solver_type, execution_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, time.getSolutionId());
            stmt.setString(2, time.getSolverType());
            stmt.setDouble(3, time.getExecutionTime());
            int rows = stmt.executeUpdate();
            Platform.runLater(() -> {
                if (rows > 0) {
                    System.out.println("✅ Saved execution time for solution ID " + time.getSolutionId());
                } else {
                    System.err.println("❌ Failed to save execution time.");
                }
                System.out.flush();
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Error saving execution time.");
                e.printStackTrace();
                System.out.flush();
            });
        }
    }

    public static void markSolutionAsRecognized(String solution) {
        String sql = "UPDATE puzzle_solutions SET recognized = true WHERE solution = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, solution);
            int rows = stmt.executeUpdate();
            Platform.runLater(() -> {
                if (rows > 0) {
                    System.out.println("✅ Marked solution as recognized: " + solution);
                } else {
                    System.err.println("❌ Failed to mark solution as recognized: " + solution);
                }
                System.out.flush();
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Error marking solution as recognized: " + solution);
                e.printStackTrace();
                System.out.flush();
            });
        }
    }

    public static void resetSolutionsRecognition() {
        String sql = "UPDATE puzzle_solutions SET recognized = false";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int rows = stmt.executeUpdate();
            Platform.runLater(() -> {
                System.out.println("🔄 Reset all recognized flags | Rows affected: " + rows);
                System.out.flush();
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Error resetting recognized flags.");
                e.printStackTrace();
                System.out.flush();
            });
        }
    }

    // Save time for both sequential and threaded solvers
    public static void saveTiming(String algorithmType, long timeTakenMs) {
        String sql = "INSERT INTO timings (algorithm_type, time_taken_ms) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, algorithmType);
            stmt.setLong(2, timeTakenMs);
            int rows = stmt.executeUpdate();
            Platform.runLater(() -> {
                if (rows > 0) {
                    System.out.println("✅ Saved timing for " + algorithmType + " algorithm.");
                } else {
                    System.err.println("❌ Failed to save timing for " + algorithmType + " algorithm.");
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.err.println("❌ Error saving timing for " + algorithmType + " algorithm.");
                e.printStackTrace();
            });
        }
    }

    public static void resetAllTables() {
        String sql = "TRUNCATE TABLE puzzle_solutions, sequential_solutions, threaded_solutions RESTART IDENTITY CASCADE"; // Add your table names here
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("✅ All tables have been reset and IDs are set to 1.");
        } catch (SQLException e) {
            System.err.println("❌ Error resetting tables:");
            e.printStackTrace();
        }
    }


    public static Connection getConnection() {
        return connection;
    }
}


