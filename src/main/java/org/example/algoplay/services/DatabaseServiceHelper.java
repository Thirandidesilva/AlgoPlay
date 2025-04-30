package org.example.algoplay.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper class to extend DatabaseService functionality without modifying the original class.
 * This class provides the missing executeInsert method used in the TicTacToeController.
 */
public class DatabaseServiceHelper {

    private static DatabaseServiceHelper instance;
    private final DatabaseService databaseService;

    private DatabaseServiceHelper() {
        // Use the existing DatabaseService singleton
        this.databaseService = DatabaseService.getInstance();
    }

    public static DatabaseServiceHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseServiceHelper();
        }
        return instance;
    }

    /**
     * Executes an INSERT statement and returns the generated ID.
     * This method is specifically designed for PostgreSQL INSERT statements that include "RETURNING id_column".
     *
     * @param sql SQL INSERT statement that includes "RETURNING id_column"
     * @param params Parameters for the prepared statement
     * @return The generated ID, or -1 if the operation failed
     */
    public int executeInsert(String sql, Object... params) {
        try {
            // Get the connection from the DatabaseService
            if (databaseService.getConnection() == null || databaseService.getConnection().isClosed()) {
                System.out.println("Connection is closed, cannot execute insert");
                return -1;
            }

            // Create a prepared statement
            PreparedStatement statement = databaseService.getConnection().prepareStatement(sql);

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            // For PostgreSQL with RETURNING clause, we need to use executeQuery instead of executeUpdate
            // when the SQL statement includes a RETURNING clause
            if (sql.toUpperCase().contains("RETURNING")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // The first column should be the ID
                        return resultSet.getInt(1);
                    } else {
                        System.err.println("Insert successful but failed to get generated ID");
                        return -1;
                    }
                }
            } else {
                // For regular insert statements without RETURNING clause
                int affectedRows = statement.executeUpdate();

                if (affectedRows == 0) {
                    System.err.println("Insert failed, no rows affected");
                    return -1;
                }

                // Get the generated ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys != null && generatedKeys.next()) {
                        // The first column should be the ID
                        return generatedKeys.getInt(1);
                    } else {
                        System.err.println("Insert successful but failed to get generated ID");
                        return -1;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing insert: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Access to the underlying DatabaseService methods
     */
    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    /**
     * Execute a query that returns nothing, delegating to DatabaseService
     */
    public boolean executeUpdate(String sql, Object... params) {
        return databaseService.executeUpdate(sql, params);
    }

    /**
     * Execute a query that returns results, delegating to DatabaseService
     */
    public ResultSet executeQuery(String sql, Object... params) {
        return databaseService.executeQuery(sql, params);
    }
}