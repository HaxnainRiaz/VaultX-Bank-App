package com.vaultx.ui.screens;

import com.vaultx.ui.components.ModernButton;
import com.vaultx.ui.components.ModernTextField;
import com.vaultx.services.BankingService;
import com.vaultx.models.User;
import com.vaultx.utils.SecurityUtils;
import com.vaultx.utils.ThemeManager;
import net.miginfocom.swing.MigLayout;
import com.vaultx.ui.components.Toast;
import javax.swing.*;
import java.awt.*;

public class RegistrationScreen extends JFrame {
    
    public RegistrationScreen() {
        setTitle("VaultX - Join the Future of Banking");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 700);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new MigLayout("fill, insets 40", "center", "[]20[]"));
        panel.setBackground(ThemeManager.BACKGROUND);
        
        // Back button
        JButton backBtn = new JButton("← Back to Login");
        backBtn.setFont(new Font("Inter", Font.PLAIN, 14));
        backBtn.setForeground(ThemeManager.PRIMARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            this.dispose();
        });
        
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        
        ModernTextField nameField = new ModernTextField("Full Name");
        ModernTextField emailField = new ModernTextField("Email Address");
        ModernTextField phoneField = new ModernTextField("Phone Number (+92XXXXXXXXXX)");
        
        JPasswordField passField = new JPasswordField();
        passField.putClientProperty("JTextField.placeholderText", "Create Password (Min 8 chars)");
        passField.setPreferredSize(new Dimension(300, 45));
        
        JPasswordField confirmPassField = new JPasswordField();
        confirmPassField.putClientProperty("JTextField.placeholderText", "Confirm Password");
        confirmPassField.setPreferredSize(new Dimension(300, 45));
        
        // Password visibility toggles
        JButton showPass1 = new JButton("👁");
        showPass1.setPreferredSize(new Dimension(45, 45));
        showPass1.addActionListener(e -> {
            if (passField.getEchoChar() == (char)0) {
                passField.setEchoChar('•');
                showPass1.setText("👁");
            } else {
                passField.setEchoChar((char)0);
                showPass1.setText("🙈");
            }
        });
        
        JButton showPass2 = new JButton("👁");
        showPass2.setPreferredSize(new Dimension(45, 45));
        showPass2.addActionListener(e -> {
            if (confirmPassField.getEchoChar() == (char)0) {
                confirmPassField.setEchoChar('•');
                showPass2.setText("👁");
            } else {
                confirmPassField.setEchoChar((char)0);
                showPass2.setText("🙈");
            }
        });
        
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Savings Account", "Current Account", "Business Account"});
        
        ModernButton registerBtn = new ModernButton("Register Now", ModernButton.Type.PRIMARY);
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String confirmPass = new String(confirmPassField.getPassword()).trim();
            String type = (String) typeBox.getSelectedItem();

            // Comprehensive validation
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                Toast.show(this, "Error: All fields are required!", Toast.Type.ERROR);
                return;
            }
            
            if (name.length() < 3 || !name.matches("^[a-zA-Z ]+$")) {
                Toast.show(this, "Name must be at least 3 characters (letters only)", Toast.Type.ERROR);
                return;
            }
            
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                Toast.show(this, "Invalid email format", Toast.Type.ERROR);
                return;
            }
            
            if (!phone.matches("^\\+92[0-9]{10}$")) {
                Toast.show(this, "Phone must be in format: +92XXXXXXXXXX", Toast.Type.ERROR);
                return;
            }
            
            if (password.length() < 8) {
                Toast.show(this, "Password must be at least 8 characters", Toast.Type.ERROR);
                return;
            }
            
            if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*[0-9].*")) {
                Toast.show(this, "Password must contain uppercase, lowercase, and number", Toast.Type.ERROR);
                return;
            }
            
            if (!password.equals(confirmPass)) {
                Toast.show(this, "Passwords do not match!", Toast.Type.ERROR);
                return;
            }

            try {
                BankingService bankingService = new BankingService();
                if (!bankingService.getSystemSettingBool("allowRegistration", true)) {
                    Toast.show(this, "Self-Registration is currently disabled by admin.", Toast.Type.ERROR);
                    return;
                }

                String hashedPassword = SecurityUtils.hashPassword(password);
                User newUser = new User(email, "CUSTOMER", email, email.split("@")[0], name, phone, hashedPassword, "ACTIVE");
                
                boolean success = bankingService.registerUser(newUser, type);
                
                if (success) {
                    Toast.show(this, "Success: Account created! Welcome, " + name.split(" ")[0], Toast.Type.SUCCESS);
                    Timer timer = new Timer(1500, ev -> {
                        new LoginScreen().setVisible(true);
                        this.dispose();
                    });
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    Toast.show(this, "Registration failed. Try again.", Toast.Type.ERROR);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.show(this, "Backend Error: " + ex.getMessage(), Toast.Type.ERROR);
            }
        });
        
        panel.add(backBtn, "align left, wrap, gapbottom 20");
        panel.add(title, "wrap, gapbottom 30");
        panel.add(new JLabel("Personal Information"), "wrap, align left, gapbottom 10");
        panel.add(nameField, "wrap, width 350!, height 45!, gapbottom 15");
        panel.add(emailField, "wrap, width 350!, height 45!, gapbottom 15");
        panel.add(phoneField, "wrap, width 350!, height 45!, gapbottom 15");
        panel.add(new JLabel("Security"), "wrap, align left, gapbottom 10");
        
        JPanel passPanel1 = new JPanel(new MigLayout("insets 0", "[grow]5[]", "[]"));
        passPanel1.setBackground(ThemeManager.BACKGROUND);
        passPanel1.add(passField, "grow");
        passPanel1.add(showPass1);
        panel.add(passPanel1, "wrap, width 350!, gapbottom 15");
        
        JPanel passPanel2 = new JPanel(new MigLayout("insets 0", "[grow]5[]", "[]"));
        passPanel2.setBackground(ThemeManager.BACKGROUND);
        passPanel2.add(confirmPassField, "grow");
        passPanel2.add(showPass2);
        panel.add(passPanel2, "wrap, width 350!, gapbottom 20");
        
        panel.add(new JLabel("Account Type"), "wrap, align left, gapbottom 10");
        panel.add(typeBox, "wrap, width 350!, height 45!, gapbottom 40");
        panel.add(registerBtn, "wrap, width 350!, height 50!");
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane);
    }
}
