package com.lighthouse.service;

import com.lighthouse.dao.UserDAO;
import com.lighthouse.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

/**
 * TIER 2 — SERVICE
 * Handles authentication business logic: login validation, registration,
 * and BCrypt password hashing/verification.
 *
 * DATA PRIVACY NOTE (for examiner):
 *   Passwords are NEVER stored as plain text. BCrypt generates a unique salt
 *   per password and applies a work factor (cost=12) making brute-force attacks
 *   computationally expensive. Even if the database is compromised, original
 *   passwords cannot be recovered from the hashes.
 */
public class AuthService {

    private final UserDAO userDAO;

    // Holds the currently authenticated user during the session
    private static User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    // ── Core Auth ─────────────────────────────────────────────────────────────

    /**
     * Attempts login. Returns the authenticated User on success.
     * @throws AuthException on invalid credentials
     * @throws SQLException  on database error
     */
    public User login(String username, String plainPassword) throws AuthException, SQLException {
        if (username == null || username.isBlank() || plainPassword == null || plainPassword.isBlank())
            throw new AuthException("Username and password are required.");

        Optional<User> opt = userDAO.findByUsername(username.trim());
        if (opt.isEmpty())
            throw new AuthException("Invalid username or password.");

        User user = opt.get();

        // BCrypt verification: compares plain-text against stored hash
        if (!BCrypt.checkpw(plainPassword, user.getPasswordHash()))
            throw new AuthException("Invalid username or password.");

        currentUser = user;
        System.out.println("[Auth] Logged in: " + user);
        return user;
    }

    /** Logs out the current session. */
    public void logout() {
        System.out.println("[Auth] Logged out: " + currentUser);
        currentUser = null;
    }

    /** Returns the currently logged-in user (null if not logged in). */
    public static User getCurrentUser() { return currentUser; }

    /** Convenience: checks if current user is ADMIN. */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /**
     * Registers a new user. Hashes the password with BCrypt before storing.
     * Work factor of 12 is the recommended minimum for modern hardware.
     */
    public User register(String username, String plainPassword, User.Role role)
            throws AuthException, SQLException {

        if (username == null || username.isBlank())
            throw new AuthException("Username cannot be empty.");
        if (plainPassword == null || plainPassword.length() < 6)
            throw new AuthException("Password must be at least 6 characters.");

        // Check uniqueness
        if (userDAO.findByUsername(username.trim()).isPresent())
            throw new AuthException("Username '" + username + "' is already taken.");

        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        User newUser = new User(0, username.trim(), hash, role);
        int id = userDAO.insert(newUser);
        newUser.setUserId(id);
        return newUser;
    }

    // ── Custom Exception ─────────────────────────────────────────────────────

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }
}