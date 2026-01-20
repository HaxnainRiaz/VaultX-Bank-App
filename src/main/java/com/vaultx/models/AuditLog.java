package com.vaultx.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuditLog {
    private String logId;
    private String userId;
    private String action; // "LOGIN", "TRANSFER", "FREEZE_ACCOUNT", etc.
    private String details;
    private long timestamp;
    private String ipAddress;

    public AuditLog() {}

    public AuditLog(String userId, String action, String details) {
        this.logId = UUID.randomUUID().toString().substring(0, 8);
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
        this.ipAddress = "127.0.0.1"; // Placeholder
    }

    // Getters and Setters
    public String getLogId() { return logId; }
    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public long getTimestamp() { return timestamp; }
    public String getIpAddress() { return ipAddress; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("logId", logId);
        map.put("userId", userId);
        map.put("action", action);
        map.put("details", details);
        map.put("timestamp", timestamp);
        map.put("ipAddress", ipAddress);
        return map;
    }
}
