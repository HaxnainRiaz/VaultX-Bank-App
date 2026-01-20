package com.vaultx;

import com.vaultx.ui.screens.LoginScreen;
import com.vaultx.utils.ThemeManager;
import javax.swing.*;

public class VaultXApp {
    public static void main(String[] args) {
        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Setup Modern Look and Feel
            ThemeManager.setup();
            
            // Show Login Screen
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
        });
    }
}
