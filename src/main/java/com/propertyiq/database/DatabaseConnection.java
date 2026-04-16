package com.propertyiq.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * TIER 4 — DATABASE
 * Singleton class managing a single shared JDBC connection.
 * Ensures only one connection instance exists across the application
 * (thread-safe via double-checked locking).
 */
public class DatabaseConnection {

    // ── Configuration ──────────────────────────────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/propertyiq_db"
                                         + "?useSSL=false&allowPublicKeyRetrieval=true"
                                         + "&serverTimezone=UTC";
    private static final String USERNAME = "root";          // ← Change to your MySQL username
    private static final String PASSWORD = "your_password"; // ← Change to your MySQL password

    // ── Singleton instance ─────────────────────────────────────────────────────
    private static volatile DatabaseConnection instance;
    private Connection connection;

    /** Private constructor — loads the MySQL JDBC driver and opens a connection. */
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("[DB] Connection established successfully.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("[DB] MySQL JDBC Driver not found. Check pom.xml.", e);
        } catch (SQLException e) {
            throw new RuntimeException("[DB] Failed to connect. Check credentials and that MySQL is running.", e);
        }
    }

    /**
     * Returns the singleton instance (thread-safe).
     * Double-checked locking prevents race conditions on startup.
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the active JDBC connection.
     * Automatically reconnects if the connection has been closed or timed out.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DB] Connection lost — reconnecting...");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("[DB] Reconnection failed.", e);
        }
        return connection;
    }

    /** Closes the connection gracefully. Call on application shutdown. */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                instance = null;
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}