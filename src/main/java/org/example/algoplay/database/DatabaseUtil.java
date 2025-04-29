package org.example.algoplay.database;

import java.sql.*;

public class DatabaseUtil {

    // Assuming you have a method to get the database connection
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseController.DB_URL, DatabaseController.DB_USER, DatabaseController.DB_PASSWORD);
    }

    // Check if a user-entered solution is correct
    public static boolean isCorrectAnswer(String userAnswer) {
        String sql = "SELECT COUNT(*) FROM puzzle_solutions WHERE solution = ?";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userAnswer);
            ResultSet rs = stmt.executeQuery();
            boolean result = rs.next() && rs.getInt(1) > 0; // Check if any rows were returned
            return result;
        } catch (SQLException e) {
            System.err.println("❌ Error checking answer: " + userAnswer);
            e.printStackTrace();
            return false; // Return false if there was an error
        }
    }
}