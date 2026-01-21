package com.vaultx.ui.screens;

import com.vaultx.ui.components.ModernButton;
import com.vaultx.ui.components.ModernTextField;
import com.vaultx.services.BankingService;
import com.vaultx.models.User;
import com.vaultx.utils.CaptchaHelper;
import com.vaultx.utils.SecurityUtils;
import com.vaultx.utils.ThemeManager;
import com.vaultx.utils.ValidationUtils;
import net.miginfocom.swing.MigLayout;
import com.vaultx.ui.components.Toast;
import javax.swing.*;
import java.awt.*;

public class RegistrationScreen extends JFrame {
    private final CaptchaHelper captchaHelper = new CaptchaHelper();
    private final JLabel captchaLabel = new JLabel();
    private final ModernTextField captchaInput = new ModernTextField("Enter Security Code Above");

    public RegistrationScreen() {
        setTitle("VaultX - Join the Future of Banking");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 850);
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
            if (passField.getEchoChar() == (char) 0) {
                passField.setEchoChar('•');
                showPass1.setText("👁");
            } else {
                passField.setEchoChar((char) 0);
                showPass1.setText("🙈");
            }
        });

        JButton showPass2 = new JButton("👁");
        showPass2.setPreferredSize(new Dimension(45, 45));
        showPass2.addActionListener(e -> {
            if (confirmPassField.getEchoChar() == (char) 0) {
                confirmPassField.setEchoChar('•');
                showPass2.setText("👁");
            } else {
                confirmPassField.setEchoChar((char) 0);
                showPass2.setText("🙈");
            }
        });

        JComboBox<String> typeBox = new JComboBox<>(
                new String[] { "Savings Account", "Current Account", "Business Account" });

        // Captcha Section
        captchaLabel.setIcon(new ImageIcon(captchaHelper.generateCaptchaImage()));
        captchaLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        captchaLabel.setToolTipText("Click to refresh security code");
        captchaLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                captchaLabel.setIcon(new ImageIcon(captchaHelper.generateCaptchaImage()));
            }
        });

        ModernButton registerBtn = new ModernButton("Register Now", ModernButton.Type.PRIMARY);
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String confirmPass = new String(confirmPassField.getPassword()).trim();
            String type = (String) typeBox.getSelectedItem();
            String captchaText = captchaInput.getText().trim();

            // Captcha Validation
            if (!captchaHelper.validate(captchaText)) {
                Toast.show(this, "Security Error: Code mismatch!", Toast.Type.ERROR);
                captchaLabel.setIcon(new ImageIcon(captchaHelper.generateCaptchaImage()));
                return;
            }

            // Master Level Validation using ValidationUtils
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                Toast.show(this, "All fields are mandatory", Toast.Type.ERROR);
                return;
            }

            if (!ValidationUtils.isValidFullName(name)) {
                Toast.show(this, "Name must be 3-50 letters/spaces only", Toast.Type.ERROR);
                return;
            }

            if (!ValidationUtils.isValidEmail(email)) {
                Toast.show(this, "Invalid email format", Toast.Type.ERROR);
                return;
            }

            if (!ValidationUtils.isValidPhone(phone)) {
                Toast.show(this, "Phone must be +92XXXXXXXXXX", Toast.Type.ERROR);
                return;
            }

            try {
                ValidationUtils.validatePasswordStrength(password);
            } catch (Exception ex) {
                Toast.show(this, ex.getMessage(), Toast.Type.ERROR);
                return;
            }

            if (!password.equals(confirmPass)) {
                Toast.show(this, "Passwords do not match", Toast.Type.ERROR);
                return;
            }

            try {
                BankingService bankingService = new BankingService();
                if (!bankingService.getSystemSettingBool("allowRegistration", true)) {
                    Toast.show(this, "Self-Registration is disabled", Toast.Type.ERROR);
                    return;
                }

                String hashedPassword = SecurityUtils.hashPassword(password);
                User newUser = new User(email, "CUSTOMER", email, email.split("@")[0], name, phone, hashedPassword,
                        "ACTIVE");

                boolean success = bankingService.registerUser(newUser, type);

                if (success) {
                    Toast.show(this, "Account created successfully!", Toast.Type.SUCCESS);
                    new LoginScreen().setVisible(true);
                    this.dispose();
                } else {
                    Toast.show(this, "Registration failed", Toast.Type.ERROR);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.show(this, "Error: " + ex.getMessage(), Toast.Type.ERROR);
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
        panel.add(typeBox, "wrap, width 350!, height 45!, gapbottom 20");

        panel.add(new JLabel("Security Verification"), "wrap, align left, gapbottom 10");
        panel.add(captchaLabel, "wrap, align center, gapbottom 10");
        panel.add(captchaInput, "wrap, width 350!, height 45!, gapbottom 40");

        panel.add(registerBtn, "wrap, width 350!, height 50!");

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane);
    }
}
