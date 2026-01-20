package com.vaultx.models;

import java.util.HashMap;
import java.util.Map;

public class Transaction {
    private String transactionId;
    private String fromAccountId;
    private String toAccountId;
    private double amount;
    private String type; // "DEPOSIT", "WITHDRAWAL", "TRANSFER"
    private long timestamp;
    private String status; // "COMPLETED", "FAILED", "PENDING"
    private String description;

    public Transaction() {}

    public Transaction(String transactionId, String fromAccountId, String toAccountId, double amount, String type, String status, String description) {
        this.transactionId = transactionId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.status = status;
        this.description = description;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public String getFromAccountId() { return fromAccountId; }
    public String getToAccountId() { return toAccountId; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionId", transactionId);
        map.put("fromAccountId", fromAccountId);
        map.put("toAccountId", toAccountId);
        map.put("amount", amount);
        map.put("type", type);
        map.put("timestamp", timestamp);
        map.put("status", status);
        map.put("description", description);
        return map;
    }
}
