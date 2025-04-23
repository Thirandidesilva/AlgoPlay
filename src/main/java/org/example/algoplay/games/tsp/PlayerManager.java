package org.example.algoplay.games.tsp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerManager {

    public static int createPlayer(String username) {
        String sql = "INSERT INTO players (username) VALUES (?) RETURNING id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("✅ Player created with ID: " + rs.getInt("id"));
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Unique violation
                System.out.println("⚠️ Username already exists.");
            } else {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static int getPlayerId(String username) {
        String sql = "SELECT id FROM players WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
