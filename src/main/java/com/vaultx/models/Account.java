package com.vaultx.models;

import java.util.HashMap;
import java.util.Map;

public class Account {
    private String accountId; // 6-digit auto-generated
    private String userId;
    private double balance;
    private String accountType; // "SAVINGS", "CURRENT", "BUSINESS"
    private String status; // "ACTIVE", "CLOSED", "FROZEN"
    private long lastUpdated;

    public Account() {}

    public Account(String accountId, String userId, double balance, String accountType, String status) {
        this.accountId = accountId;
        this.userId = userId;
        this.balance = balance;
        this.accountType = accountType;
        this.status = status;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("accountId", accountId);
        map.put("userId", userId);
        map.put("balance", balance);
        map.put("accountType", accountType);
        map.put("status", status);
        map.put("lastUpdated", lastUpdated);
        return map;
    }
}
