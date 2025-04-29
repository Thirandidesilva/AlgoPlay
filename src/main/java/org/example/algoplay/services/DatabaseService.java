package org.example.algoplay.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseService {
    private static final String URL = "jdbc:postgresql://localhost:5432/AlgoPlay";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Thiya@2003";

    private Connection connection;

    // Singleton pattern
    private static DatabaseService instance;

    private DatabaseService() {
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            // Close any existing connection first
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            // Establish new connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established successfully");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Check if connection is valid and reconnect if needed
            if (connection == null || connection.isClosed()) {
                System.out.println("Connection is null or closed. Reconnecting...");
                connectToDatabase();
            } else if (!connection.isValid(5)) { // 5 seconds timeout
                System.out.println("Connection is invalid. Reconnecting...");
                connectToDatabase();
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection: " + e.getMessage());
            connectToDatabase();
        }
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
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = getConnection(); // This ensures we have a valid connection

            statement = prepareStatement(conn, sql, params);
            int result = statement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Close statement but not connection (connection is maintained by the service)
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    System.err.println("Error closing statement: " + e.getMessage());
                }
            }
        }
    }

    // Helper method to execute queries that return a result WITH auto-closeable resources
    public ResultSet executeQuery(String sql, Object... params) {
        try {
            Connection conn = getConnection(); // This ensures we have a valid connection

            // Using Statement.RETURN_GENERATED_KEYS to ensure we can get generated keys
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                return statement.executeQuery();
            } else {
                // For INSERT statements that need to return values
                statement.executeUpdate();
                return statement.getGeneratedKeys();
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Alternative method for inserts that need to return a generated ID
    public int executeInsert(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            conn = getConnection(); // This ensures we have a valid connection
            statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Creating record failed, no rows affected.");
                return -1;
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                System.err.println("Creating record failed, no ID obtained.");
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Error executing insert: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            // Close resources but not connection
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    System.err.println("Error closing result set: " + e.getMessage());
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    System.err.println("Error closing statement: " + e.getMessage());
                }
            }
        }
    }

    // Helper method to prepare statements with parameters
    private PreparedStatement prepareStatement(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
}
}