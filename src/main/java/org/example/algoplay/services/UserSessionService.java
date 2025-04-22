package org.example.algoplay.services;

import org.example.algoplay.models.User;

public class UserSessionService {
    private static UserSessionService instance;
    private User currentUser;

    private UserSessionService() {
        // Private constructor
    }

    public static UserSessionService getInstance() {
        if (instance == null) {
            instance = new UserSessionService();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
}