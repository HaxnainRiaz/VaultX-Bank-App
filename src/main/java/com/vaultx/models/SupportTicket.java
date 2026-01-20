package com.vaultx.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SupportTicket {
    private String ticketId;
    private String userId;
    private String title;
    private String category; // "TECHNICAL", "BILLING", "FRAUD", "OTHER"
    private String description;
    private String status; // "OPEN", "IN_PROGRESS", "CLOSED"
    private long createdAt;
    private String adminReply;

    public SupportTicket() {}

    public SupportTicket(String userId, String title, String category, String description) {
        this.ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.description = description;
        this.status = "OPEN";
        this.createdAt = System.currentTimeMillis();
        this.adminReply = "";
    }

    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public String getAdminReply() { return adminReply; }

    public void setStatus(String status) { this.status = status; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ticketId", ticketId);
        map.put("userId", userId);
        map.put("title", title);
        map.put("category", category);
        map.put("description", description);
        map.put("status", status);
        map.put("createdAt", createdAt);
        map.put("adminReply", adminReply);
        return map;
    }
}
