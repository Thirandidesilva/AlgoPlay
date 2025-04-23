package org.example.algoplay.games.tsp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/tsp_game_db";
    private static final String USER = "postgres"; // replace with your DB username
    private static final String PASSWORD = "G0tb1tf3v3rh1t"; // replace with your DB password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
