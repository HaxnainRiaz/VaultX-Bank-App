package com.vaultx.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.vaultx.models.Account;
import com.vaultx.models.AuditLog;
import com.vaultx.models.SupportTicket;
import com.vaultx.models.User;
import com.vaultx.utils.SecurityUtils;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class BankingService {
    private final Firestore db;

    public BankingService() {
        this.db = FirebaseService.getInstance().getDb();
    }

    // --- AUTHENTICATION ---

    public User login(String identifier, String password) throws Exception {
        // Hardcoded Admin Override for Hasnain Riaz (Case Insensitive Email)
        if ("hasnainr0111@gmail.com".equalsIgnoreCase(identifier.trim()) && "Riaz@458".equals(password.trim())) {
            User admin = new User("ADMIN_1", "ADMIN", "hasnainr0111@gmail.com", "hasnain_riaz", "Hasnain Riaz", "Not Specified", "ENCRYPTED", "ACTIVE");
            logActivity("ADMIN_1", "ADMIN_LOGIN", "Admin logged in via hardcoded credentials");
            return admin;
        }

        if (db == null) throw new Exception("Database Connection Unavailable");

        User user = null;
        // Search by Email
        DocumentSnapshot doc = db.collection("users").document(identifier.trim()).get().get();
        if (doc.exists()) {
            User temp = doc.toObject(User.class);
            if (SecurityUtils.checkPassword(password, temp.getPasswordHash())) user = temp;
        }

        if (user == null) {
            // Search by Username
            Query q = db.collection("users").whereEqualTo("username", identifier.trim());
            List<QueryDocumentSnapshot> qr = q.get().get().getDocuments();
            if (!qr.isEmpty()) {
                User temp = qr.get(0).toObject(User.class);
                if (SecurityUtils.checkPassword(password, temp.getPasswordHash())) user = temp;
            }
        }

        if (user != null) {
            String status = user.getStatus();
            if ("FROZEN".equalsIgnoreCase(status)) throw new Exception("Your profile is FROZEN. Contact admin.");
            if ("DEACTIVATED".equalsIgnoreCase(status)) throw new Exception("This account has been DEACTIVATED.");
            
            // System Maintenance Check
            if (!"ADMIN".equals(user.getRole())) {
                if (getSystemSettingBool("maintenanceMode", false)) {
                    throw new Exception("System Under Maintenance. Please try again later.");
                }
            }

            // Check associated account
            Account acc = getAccountByUserId(user.getEmail());
            if (acc != null && "FROZEN".equalsIgnoreCase(acc.getStatus())) {
                throw new Exception("Security Alert: Your bank account is FROZEN.");
            }

            logActivity(user.getEmail(), "USER_LOGIN", "User logged in successfully");
            return user;
        }

        throw new Exception("Invalid credentials or account not found");
    }

    // --- CUSTOMER MANAGEMENT ---

    public boolean registerUser(User user, String initialAccountType) throws Exception {
        if (db == null) return false;

        // Check if user exists
        if (db.collection("users").document(user.getEmail()).get().get().exists()) {
            throw new Exception("User with this email already exists");
        }

        db.collection("users").document(user.getEmail()).set(user).get();

        String accountId = SecurityUtils.generateAccountId();
        Account account = new Account(accountId, user.getEmail(), 1000.0, initialAccountType, "ACTIVE");
        db.collection("accounts").document(accountId).set(account).get();

        logActivity(user.getEmail(), "REGISTRATION", "User registered and account " + accountId + " created");
        return true;
    }

    public void updateUserStatus(String email, String status) throws Exception {
        if (db == null) return;
        db.collection("users").document(email).update("status", status).get();
        logActivity("ADMIN", "USER_STATUS_CHANGE", "User " + email + " set to " + status);
    }

    public void resetUserPassword(String email, String newPassword) throws Exception {
        if (db == null) return;
        String hashed = SecurityUtils.hashPassword(newPassword);
        db.collection("users").document(email).update("passwordHash", hashed).get();
        logActivity("ADMIN", "USER_PASSWORD_RESET", "Password reset for " + email);
    }

    public List<User> getAllUsers() throws Exception {
        List<User> list = new ArrayList<>();
        if (db == null) return list;
        for (DocumentSnapshot d : db.collection("users").get().get().getDocuments()) {
            list.add(d.toObject(User.class));
        }
        return list;
    }

    // --- ACCOUNT OPERATIONS ---

    public Account getAccountByUserId(String userId) throws Exception {
        if (db == null) return null;
        Query q = db.collection("accounts").whereEqualTo("userId", userId);
        List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
        return docs.isEmpty() ? null : docs.get(0).toObject(Account.class);
    }

    public Account getAccountById(String accountId) throws Exception {
        if (db == null) return null;
        DocumentSnapshot doc = db.collection("accounts").document(accountId).get().get();
        return doc.exists() ? doc.toObject(Account.class) : null;
    }

    public List<Account> getAllAccounts() throws Exception {
        List<Account> list = new ArrayList<>();
        if (db == null) return list;
        for (DocumentSnapshot d : db.collection("accounts").get().get().getDocuments()) {
            list.add(d.toObject(Account.class));
        }
        return list;
    }

    public void updateAccountStatus(String accountId, String status) throws Exception {
        if (db == null) return;
        db.collection("accounts").document(accountId).update("status", status).get();
        logActivity("ADMIN", "ACCOUNT_STATUS_CHANGE", "Account " + accountId + " set to " + status);
    }

    public void activateAccount(String email) throws Exception {
        if (db == null) return;
        db.collection("users").document(email).update("status", "ACTIVE").get();
        Account acc = getAccountByUserId(email);
        if (acc != null) {
            db.collection("accounts").document(acc.getAccountId()).update("status", "ACTIVE").get();
        }
        logActivity(email, "ACCOUNT_ACTIVATION", "Account activated via OTP");
    }

    public void deleteTransaction(String transactionId) throws Exception {
        if (db == null) return;
        db.collection("transactions").document(transactionId).delete().get();
        logActivity("ADMIN", "TRANSACTION_REVERSED", "Transaction ID: " + transactionId);
    }

    public void performDeposit(String accountId, double amount, boolean isAdmin) throws Exception {
        if (db == null) return;
        
        double limit = getSystemSetting("maxDepositLimit", 5000.0);
        
        if (!isAdmin && amount > limit) {
            String tid = UUID.randomUUID().toString().substring(0, 8);
            com.vaultx.models.Transaction t = new com.vaultx.models.Transaction(tid, "EXTERNAL", accountId, amount, "DEPOSIT_PENDING", "PENDING", "High Value Deposit (Awaiting Approval)");
            db.collection("transactions").document(tid).set(t);
            logActivity(accountId, "DEPOSIT_HELD", "Deposit of Rs. " + amount + " held for admin approval");
            throw new Exception("Amount exceeds limit (Rs. " + limit + "). Awaiting Admin Approval.");
        }

        DocumentReference ref = db.collection("accounts").document(accountId);
        db.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(ref).get();
            double bal = snap.getDouble("balance") + amount;
            tx.update(ref, "balance", bal);
            saveTransaction("EXTERNAL", accountId, amount, "DEPOSIT", "Cash Deposit");
            return null;
        }).get();
        logActivity(accountId, "DEPOSIT", "Deposited Rs. " + amount);
    }

    public void performWithdrawal(String accountId, double amount) throws Exception {
        if (db == null) return;
        DocumentReference ref = db.collection("accounts").document(accountId);
        db.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(ref).get();
            double bal = snap.getDouble("balance");
            if (bal < amount) throw new Exception("Insufficient funds");
            tx.update(ref, "balance", bal - amount);
            saveTransaction(accountId, "ATM", amount, "WITHDRAWAL", "Cash Withdrawal");
            return null;
        }).get();
        logActivity(accountId, "WITHDRAWAL", "Withdrew Rs. " + amount);
    }

    public void performTransfer(String from, String to, double amount, String desc) throws Exception {
        if (db == null) throw new Exception("Database not connected");
        DocumentReference fRef = db.collection("accounts").document(from);
        DocumentReference tRef = db.collection("accounts").document(to);

        db.runTransaction(tx -> {
            DocumentSnapshot fSnap = tx.get(fRef).get();
            DocumentSnapshot tSnap = tx.get(tRef).get();

            if (!tSnap.exists()) throw new Exception("Recipient account not found");
            double fBal = fSnap.getDouble("balance");
            if (fBal < amount) throw new Exception("Insufficient funds");

            tx.update(fRef, "balance", fBal - amount);
            tx.update(tRef, "balance", tSnap.getDouble("balance") + amount);

            saveTransaction(from, to, amount, "TRANSFER", desc);
            return null;
        }).get();
        logActivity(from, "TRANSFER", "Transferred Rs. " + amount + " to " + to);
    }

    // --- SUPPORT TICKETS ---

    public void createTicket(SupportTicket ticket) throws Exception {
        if (db == null) return;
        db.collection("tickets").document(ticket.getTicketId()).set(ticket).get();
        logActivity(ticket.getUserId(), "SUPPORT_TICKET_CREATED", "Ticket ID: " + ticket.getTicketId());
    }

    public List<SupportTicket> getTicketsForUser(String userId) throws Exception {
        List<SupportTicket> list = new ArrayList<>();
        if (db == null) return list;
        Query q = db.collection("tickets").whereEqualTo("userId", userId);
        for (DocumentSnapshot d : q.get().get().getDocuments()) {
            list.add(d.toObject(SupportTicket.class));
        }
        return list;
    }

    public List<SupportTicket> getAllTickets() throws Exception {
        List<SupportTicket> list = new ArrayList<>();
        if (db == null) return list;
        for (DocumentSnapshot d : db.collection("tickets").get().get().getDocuments()) {
            list.add(d.toObject(SupportTicket.class));
        }
        return list;
    }

    public void resolveTicket(String ticketId, String reply) throws Exception {
        if (db == null) return;
        db.collection("tickets").document(ticketId).update("status", "CLOSED", "adminReply", reply).get();
        logActivity("ADMIN", "SUPPORT_TICKET_RESOLVED", "Ticket ID: " + ticketId);
    }

    public void approveTransaction(String transactionId) throws Exception {
        if (db == null) return;
        DocumentSnapshot txDoc = db.collection("transactions").document(transactionId).get().get();
        if (!txDoc.exists()) throw new Exception("Transaction not found");
        
        com.vaultx.models.Transaction txData = txDoc.toObject(com.vaultx.models.Transaction.class);
        if (!"PENDING".equals(txData.getStatus())) throw new Exception("This transaction is already processed (" + txData.getStatus() + ")");

        String accountId = txData.getToAccountId();
        double amount = txData.getAmount();

        DocumentReference accRef = db.collection("accounts").document(accountId);
        db.runTransaction(txn -> {
            DocumentSnapshot accSnap = txn.get(accRef).get();
            double newBal = accSnap.getDouble("balance") + amount;
            txn.update(accRef, "balance", newBal);
            txn.update(db.collection("transactions").document(transactionId), "status", "COMPLETED", "type", "DEPOSIT", "description", "Deposit Approved by Admin");
            return null;
        }).get();
        
        logActivity("ADMIN", "TX_APPROVED", "Approved deposit " + transactionId + " for " + accountId);
    }

    public void setSystemSetting(String key, Object value) throws Exception {
        if (db == null) return;
        DocumentReference docRef = db.collection("settings").document("config");
        docRef.set(Collections.singletonMap(key, value), SetOptions.merge()).get();
    }

    public double getSystemSetting(String key, double defaultValue) {
        try {
            DocumentSnapshot doc = db.collection("settings").document("config").get().get();
            if (doc.exists() && doc.contains(key)) return doc.getDouble(key);
        } catch (Exception e) {}
        return defaultValue;
    }

    public boolean getSystemSettingBool(String key, boolean defaultValue) {
        try {
            DocumentSnapshot doc = db.collection("settings").document("config").get().get();
            if (doc.exists() && doc.contains(key)) return doc.getBoolean(key);
        } catch (Exception e) {}
        return defaultValue;
    }

    // --- LOGS & STATS ---

    private void logActivity(String userId, String action, String details) {
        if (db == null) return;
        AuditLog log = new AuditLog(userId, action, details);
        db.collection("audit_logs").document(log.getLogId()).set(log);
    }

    private void saveTransaction(String from, String to, double amount, String type, String desc) {
        if (db == null) return;
        String tid = UUID.randomUUID().toString().substring(0, 8);
        com.vaultx.models.Transaction t = new com.vaultx.models.Transaction(tid, from, to, amount, type, "COMPLETED", desc);
        db.collection("transactions").document(tid).set(t);
    }

    public List<AuditLog> getAllAuditLogs() throws Exception {
        List<AuditLog> list = new ArrayList<>();
        if (db == null) return list;
        Query q = db.collection("audit_logs").orderBy("timestamp", Query.Direction.DESCENDING).limit(100);
        for (DocumentSnapshot d : q.get().get().getDocuments()) {
            list.add(d.toObject(AuditLog.class));
        }
        return list;
    }

    public List<com.vaultx.models.Transaction> getAllTransactions() throws Exception {
        List<com.vaultx.models.Transaction> list = new ArrayList<>();
        if (db == null) return list;
        for (DocumentSnapshot d : db.collection("transactions").orderBy("timestamp", Query.Direction.DESCENDING).get().get().getDocuments()) {
            list.add(d.toObject(com.vaultx.models.Transaction.class));
        }
        return list;
    }

    public List<com.vaultx.models.Transaction> getTransactions(String accountId) throws Exception {
        List<com.vaultx.models.Transaction> list = new ArrayList<>();
        if (db == null) return list;
        // Fetch where from = accountId
        Query q1 = db.collection("transactions").whereEqualTo("fromAccountId", accountId);
        for (DocumentSnapshot d : q1.get().get().getDocuments()) list.add(d.toObject(com.vaultx.models.Transaction.class));
        
        // Fetch where to = accountId
        Query q2 = db.collection("transactions").whereEqualTo("toAccountId", accountId);
        for (DocumentSnapshot d : q2.get().get().getDocuments()) {
            com.vaultx.models.Transaction t = d.toObject(com.vaultx.models.Transaction.class);
            boolean exists = false;
            for(com.vaultx.models.Transaction existing : list) if(existing.getTransactionId().equals(t.getTransactionId())) exists = true;
            if(!exists) list.add(t);
        }
        list.sort((a,b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return list;
    }

    public Map<String, Object> getSystemStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        if (db == null) {
            stats.put("totalCustomers", 0);
            stats.put("activeAccounts", 0);
            stats.put("totalBalance", 0.0);
            return stats;
        }
        long customers = db.collection("users").whereEqualTo("role", "CUSTOMER").get().get().size();
        long activeAccs = db.collection("accounts").whereEqualTo("status", "ACTIVE").get().get().size();
        
        double totalBalance = 0;
        for (DocumentSnapshot d : db.collection("accounts").get().get().getDocuments()) {
            Double b = d.getDouble("balance");
            if (b != null) totalBalance += b;
        }

        stats.put("totalCustomers", customers);
        stats.put("activeAccounts", activeAccs);
        stats.put("totalBalance", totalBalance);
        return stats;
    }
}
