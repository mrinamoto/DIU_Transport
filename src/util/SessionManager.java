package util;

import java.util.Objects;
import model.User;

/**
 * Manages user session throughout the application
 * Singleton pattern to ensure only one active session
 */
public class SessionManager {
    private static volatile User currentUser;

    /**
     * Get singleton instance
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if any user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Login user and create session
     */
    public static void login(User user) {
        Objects.requireNonNull(user, "user must not be null");
        currentUser = user;
    }

    /**
     * Logout current user
     */
    public static void logout() {
        currentUser = null;
    }
}