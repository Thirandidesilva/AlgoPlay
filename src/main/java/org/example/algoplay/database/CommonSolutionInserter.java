package org.example.algoplay.database;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class CommonSolutionInserter {

    // Method to insert common solutions into the "puzzle_solutions" table
    public static void insertCommonSolutions() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet sequentialResults = null;
        ResultSet threadedResults = null;

        try {
            conn = DriverManager.getConnection(DatabaseController.DB_URL, DatabaseController.DB_USER, DatabaseController.DB_PASSWORD);
            stmt = conn.createStatement();

            // Get solutions from sequential_solutions table
            sequentialResults = stmt.executeQuery("SELECT positions FROM sequential_solutions");
            Set<String> sequential = new HashSet<>();
            while (sequentialResults.next()) {
                sequential.add(sequentialResults.getString("positions").strip());
            }

            // Get solutions from threaded_solutions table
            threadedResults = stmt.executeQuery("SELECT positions FROM threaded_solutions");
            Set<String> threaded = new HashSet<>();
            while (threadedResults.next()) {
                threaded.add(threadedResults.getString("positions").strip());
            }

            // Find common solutions
            sequential.retainAll(threaded);
            Set<String> common = sequential; // now contains the common solutions

            // If the common solutions count is not 92, print a warning
            if (common.size() != 92) {
                System.out.println("Warning: Expected 92 common solutions, but found " + common.size() + ".");
                return;
            }

            // Insert common solutions into 'puzzle_solutions' table
            String insertSQL = "INSERT INTO puzzle_solutions (solution, recognized) VALUES (?, ?) ON CONFLICT (solution) DO NOTHING"; // "ON CONFLICT" for PostgreSQL
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                int insertedCount = 0;
                for (String posStr : common) {
                    pstmt.setString(1, posStr);
                    pstmt.setBoolean(2, false); // recognized = false
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        insertedCount++;
                    }
                }
                System.out.println(insertedCount + " new common solutions inserted into 'puzzle_solutions' table.");
            }

        } catch (SQLException e) {
            System.err.println("Error inserting common solutions: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (sequentialResults != null) sequentialResults.close();
                if (threadedResults != null) threadedResults.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        insertCommonSolutions();
    }
}
