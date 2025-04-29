package org.example.algoplay.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseService {
    private static final String URL = "jdbc:postgresql://localhost:5432/AlgoPlay";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    private Connection connection;

    // Singleton pattern
    private static DatabaseService instance;

    private DatabaseService() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established successfully");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // Helper method to execute queries that return nothing
    public boolean executeUpdate(String sql, Object... params) {
        try {
            // Check if connection is valid and reconnect if needed
            if (connection == null || connection.isClosed()) {
                System.out.println("Reconnecting to database...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }

            try (PreparedStatement statement = prepareStatement(sql, params)) {
                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for better debugging
            return false;
        }
    }

    // Helper method to execute queries that return a result
    public ResultSet executeQuery(String sql, Object... params) {
        try {
            // Check if connection is valid and reconnect if needed
            if (connection == null || connection.isClosed()) {
                System.out.println("Reconnecting to database...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }

            PreparedStatement statement = prepareStatement(sql, params);
            return statement.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for better debugging
            return null;
        }
    }

    // Helper method to prepare statements with parameters
    private PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}