package com.vaultx.models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String role; // "ADMIN" or "CUSTOMER"
    private String email;
    private String username;
    private String fullName;
    private String phone;
    private String passwordHash;
    private String status; // "ACTIVE", "FROZEN", "INACTIVE"
    private long createdAt;

    public User() {}

    public User(String userId, String role, String email, String username, String fullName, String phone, String passwordHash, String status) {
        this.userId = userId;
        this.role = role;
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("role", role);
        map.put("email", email);
        map.put("username", username);
        map.put("fullName", fullName);
        map.put("phone", phone);
        map.put("passwordHash", passwordHash);
        map.put("status", status);
        map.put("createdAt", createdAt);
        return map;
    }
}
