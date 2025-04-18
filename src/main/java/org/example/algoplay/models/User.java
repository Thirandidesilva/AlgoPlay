package org.example.algoplay.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.algoplay.services.DatabaseService;

public class User {
    private int userId;
    private String username;
    private String password;

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword(){return password;}
    public  void  setPassword(String passwaord){this.password = password;}

    public static User createUser(String username) {
        DatabaseService db = DatabaseService.getInstance();
        String sql = "INSERT INTO users (username) VALUES (?) RETURNING user_id";

        try (ResultSet rs = db.executeQuery(sql, username)) {
            if (rs != null && rs.next()) {
                int userId = rs.getInt("user_id");
                return new User(userId, username);
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
        return null;
    }

    public static User findByUsername(String username) {
        DatabaseService db = DatabaseService.getInstance();
        String sql = "SELECT * FROM users WHERE username = ?";

        try (ResultSet rs = db.executeQuery(sql, username)) {
            if (rs != null && rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }
}