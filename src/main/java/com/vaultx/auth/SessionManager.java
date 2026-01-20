package com.vaultx.auth;

import com.vaultx.models.Account;
import com.vaultx.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Account currentAccount;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user, Account account) {
        this.currentUser = user;
        this.currentAccount = account;
    }

    public void logout() {
        this.currentUser = null;
        this.currentAccount = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}
