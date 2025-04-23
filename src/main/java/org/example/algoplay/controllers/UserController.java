package org.example.algoplay.controllers;



import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.algoplay.models.User;
import org.example.algoplay.services.DatabaseService;
import org.example.algoplay.services.UserSessionService;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserController {

    @FXML private VBox loginPanel;
    @FXML private VBox registerPanel;
    @FXML private VBox profilePanel;

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginMessage;

    @FXML private TextField registerUsername;
    @FXML private PasswordField registerPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Label registerMessage;

    @FXML private Label profileInitials;
    @FXML private Label usernameLabel;
    @FXML private Label ticTacToeScore;
    @FXML private Label tohTime;
    @FXML private Label queensScore;
    @FXML private Label knightsPath;
    @FXML private Label tspRoute;
    @FXML private Button backButton;



    private User currentUser;

    public void initialize() {
        // Start with login panel visible
        showLoginPanel();
    }

    @FXML
    private void showLoginPanel() {
        loginPanel.setVisible(true);
        registerPanel.setVisible(false);
        profilePanel.setVisible(false);

        // Clear any messages
        loginMessage.setText("");
    }

    @FXML
    private void showRegisterPanel() {
        loginPanel.setVisible(false);
        registerPanel.setVisible(true);
        profilePanel.setVisible(false);

        // Clear any messages
        registerMessage.setText("");
    }

    private void showProfilePanel() {
        loginPanel.setVisible(false);
        registerPanel.setVisible(false);
        profilePanel.setVisible(true);

        // Update profile info
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());

            // Set profile initials
            String username = currentUser.getUsername();
            if (username != null && !username.isEmpty()) {
                profileInitials.setText(username.substring(0, 1).toUpperCase());
            } else {
                profileInitials.setText("?");
            }

            // Load user statistics from database
            loadUserStatistics();
        }
    }

    @FXML
    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            loginMessage.setText("Please enter both username and password");
            return;
        }

        // Verify login credentials
        DatabaseService db = DatabaseService.getInstance();
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (ResultSet rs = db.executeQuery(sql, username, password)) {
            if (rs != null && rs.next()) {
                // Login successful
                currentUser = new User(rs.getInt("user_id"), rs.getString("username"));
                // Set user in the session service
                UserSessionService.getInstance().setCurrentUser(currentUser);
                showProfilePanel();
            } else {
                loginMessage.setText("Invalid username or password");
            }
        } catch (SQLException e) {
            loginMessage.setText("Error during login: " + e.getMessage());
            System.err.println("Login error: " + e.getMessage());
        }
    }


    @FXML
    private void handleRegister() {
        String username = registerUsername.getText().trim();
        String password = registerPassword.getText().trim();
        String confirm = confirmPassword.getText().trim();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            registerMessage.setText("Please fill in all fields");
            return;
        }

        if (!password.equals(confirm)) {
            registerMessage.setText("Passwords do not match");
            return;
        }

        // Check if username already exists
        User existingUser = User.findByUsername(username);
        if (existingUser != null) {
            registerMessage.setText("Username already exists");
            return;
        }

        // Register new user
        DatabaseService db = DatabaseService.getInstance();
        String sql = "INSERT INTO users (username, password) VALUES (?, ?) RETURNING user_id";

        try (ResultSet rs = db.executeQuery(sql, username, password)) {
            if (rs != null && rs.next()) {
                int userId = rs.getInt("user_id");
                currentUser = new User(userId, username);
                // Set user in the session service
                UserSessionService.getInstance().setCurrentUser(currentUser);
                registerMessage.setText("Registration successful!");

                // Switch to profile panel after successful registration
                showProfilePanel();
            }
        } catch (SQLException e) {
            registerMessage.setText("Error during registration: " + e.getMessage());
            System.err.println("Registration error: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        currentUser = null;
        // Clear the user session
        UserSessionService.getInstance().logout();
        showLoginPanel();

        // Clear login fields
        loginUsername.clear();
        loginPassword.clear();
    }

    @FXML
    private void goBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            Parent root = loader.load();

            // Get the controller but don't set the user - it will get it from the session
            MainMenuController mainMenuController = loader.getController();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AlgoPlay");

        } catch (IOException e) {
            System.err.println("Error returning to main menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUserStatistics() {
        if (currentUser == null) return;

        DatabaseService db = DatabaseService.getInstance();
        int userId = currentUser.getUserId();

        // Load game statistics for the user
        try {
            // TicTacToe
            String sql = "SELECT high_score FROM game_stats WHERE user_id = ? AND game_name = 'tictactoe'";
            ResultSet rs = db.executeQuery(sql, userId);
            if (rs != null && rs.next()) {
                ticTacToeScore.setText(rs.getString("high_score"));
            }

            // Tower of Hanoi
            sql = "SELECT best_time FROM game_stats WHERE user_id = ? AND game_name = 'toh'";
            rs = db.executeQuery(sql, userId);
            if (rs != null && rs.next()) {
                String time = rs.getString("best_time");
                tohTime.setText(time + "s");
            }

            // 8 Queens
            sql = "SELECT best_score FROM game_stats WHERE user_id = ? AND game_name = 'queens'";
            rs = db.executeQuery(sql, userId);
            if (rs != null && rs.next()) {
                queensScore.setText(rs.getString("best_score"));
            }

            // Knights Tour
            sql = "SELECT best_moves FROM game_stats WHERE user_id = ? AND game_name = 'knights'";
            rs = db.executeQuery(sql, userId);
            if (rs != null && rs.next()) {
                knightsPath.setText(rs.getString("best_moves") + " moves");
            }

            // TSP
            sql = "SELECT best_route FROM game_stats WHERE user_id = ? AND game_name = 'tsp'";
            rs = db.executeQuery(sql, userId);
            if (rs != null && rs.next()) {
                tspRoute.setText(rs.getString("best_route") + "km");
            }

        } catch (SQLException e) {
            System.err.println("Error loading user statistics: " + e.getMessage());
        }
    }

    // Method to set user from outside (e.g., when navigating from main menu while logged in)
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            showProfilePanel();
        } else {
            showLoginPanel();
        }
    }

}