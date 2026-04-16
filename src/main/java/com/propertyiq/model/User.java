package com.propertyiq.model;

/**
 * MODEL — User
 * Represents a system user (Admin or Investor).
 */
public class User {

    public enum Role { ADMIN, INVESTOR }

    private int    userId;
    private String username;
    private String passwordHash; // BCrypt hash — never store plain-text
    private Role   role;

    // ── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(int userId, String username, String passwordHash, Role role) {
        this.userId       = userId;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getUserId()       { return userId; }
    public void   setUserId(int id) { this.userId = id; }

    public String getUsername()             { return username; }
    public void   setUsername(String u)     { this.username = u; }

    public String getPasswordHash()         { return passwordHash; }
    public void   setPasswordHash(String p) { this.passwordHash = p; }

    public Role getRole()          { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "', role=" + role + "}";
    }
}