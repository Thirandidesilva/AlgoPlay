package org.example.algoplay.games.tsp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/tsp_game_db";
        String user = "postgres";
        String password = "G0tb1tf3v3rh1t";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✅ Connected to PostgreSQL successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Connection failed.");
            e.printStackTrace();
        }
    }
}
