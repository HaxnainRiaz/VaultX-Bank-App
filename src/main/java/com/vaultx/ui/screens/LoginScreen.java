package com.vaultx.ui.screens;

import com.vaultx.auth.SessionManager;
import com.vaultx.models.User;
import com.vaultx.services.BankingService;
import com.vaultx.ui.components.ModernButton;
import com.vaultx.ui.components.ModernTextField;
import com.vaultx.ui.components.Toast;
import com.vaultx.utils.SecurityUtils;
import com.vaultx.utils.ThemeManager;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {
    private final BankingService bankingService = new BankingService();

    public LoginScreen() {
        setTitle("VaultX - Secure Gateway");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 0", "[50%]0[50%]", "fill"));
        mainPanel.add(createBrandingPanel(), "grow");
        mainPanel.add(createLoginSection(), "grow");

        add(mainPanel);
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 40", "fill", "center"));
        panel.setBackground(ThemeManager.PRIMARY);

        JLabel logo = new JLabel("VaultX");
        logo.setFont(new Font("Inter", Font.BOLD, 48));
        logo.setForeground(Color.WHITE);

        JLabel slogan = new JLabel("Your Money, Our Priority.");
        slogan.setFont(new Font("Inter", Font.PLAIN, 18));
        slogan.setForeground(new Color(255, 255, 255, 180));

        panel.add(logo, "wrap, center");
        panel.add(slogan, "center");
        return panel;
    }

    private JPanel createLoginSection() {
        JPanel section = new JPanel(new MigLayout("fill, insets 60", "fill", "center"));
        section.setBackground(ThemeManager.BACKGROUND);

        JLabel title = new JLabel("Login to Your Account");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        ModernTextField userField = new ModernTextField("Email / Account ID");
        JPasswordField passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(300, 45));
        passField.putClientProperty("JTextField.placeholderText", "Password");

        ModernButton loginBtn = new ModernButton("Login Now", ModernButton.Type.PRIMARY);
        loginBtn.addActionListener(e -> {
            String input = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (input.isEmpty() || password.isEmpty()) {
                Toast.show(this, "Fields cannot be empty", Toast.Type.ERROR);
                return;
            }

            new Thread(() -> {
                try {
                    User user = bankingService.login(input, password);
                    SwingUtilities.invokeLater(() -> {
                        try {
                            SessionManager.getInstance().login(user, null);

                            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                                new AdminDashboard().setVisible(true);
                            } else {
                                new CustomerDashboard().setVisible(true);
                            }
                            this.dispose();
                        } catch (Exception ex) {
                            Toast.show(this, "Login failed: " + ex.getMessage(), Toast.Type.ERROR);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        Toast.show(this, ex.getMessage(), Toast.Type.ERROR);
                    });
                }
            }).start();
        });

        ModernButton registerBtn = new ModernButton("Open New Account", ModernButton.Type.SECONDARY);
        registerBtn.addActionListener(e -> {
            new RegistrationScreen().setVisible(true);
            this.dispose();
        });

        section.add(title, "wrap, gapbottom 30");
        section.add(new JLabel("Identity Identifier"), "wrap");
        section.add(userField, "wrap, height 45!, gapbottom 15");
        section.add(new JLabel("Security Credential"), "wrap");
        section.add(passField, "wrap, height 45!, gapbottom 30");

        section.add(loginBtn, "wrap, height 50!, gapbottom 10");
        section.add(registerBtn, "height 50!");

        return section;
    }
}
