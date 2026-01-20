package com.vaultx.ui.screens;

import com.vaultx.auth.SessionManager;
import com.vaultx.models.User;
import com.vaultx.services.BankingService;
import com.vaultx.ui.components.ModernButton;
import com.vaultx.ui.components.ModernTextField;
import com.vaultx.ui.components.Toast;
import com.vaultx.utils.CaptchaHelper;
import com.vaultx.utils.SecurityUtils;
import com.vaultx.utils.ThemeManager;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginScreen extends JFrame {
    private final BankingService bankingService = new BankingService();
    private final CaptchaHelper captchaHelper = new CaptchaHelper();
    private final JLabel captchaLabel = new JLabel();
    private final ModernTextField captchaInput = new ModernTextField("Enter Code Above");

    public LoginScreen() {
        setTitle("VaultX - Secure Gateway");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 0", "[50%]0[50%]", "fill"));
        mainPanel.add(createBrandingPanel(), "grow");
        mainPanel.add(createLoginSection(), "grow");

        add(mainPanel);
        refreshCaptcha();
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 40", "fill", "center"));
        panel.setBackground(ThemeManager.PRIMARY);
        
        JLabel logo = new JLabel("VaultX");
        logo.setFont(new Font("Inter", Font.BOLD, 48));
        logo.setForeground(Color.WHITE);
        
        JLabel slogan = new JLabel("Fortified Banking Excellence.");
        slogan.setFont(new Font("Inter", Font.PLAIN, 18));
        slogan.setForeground(new Color(255, 255, 255, 180));
        
        panel.add(logo, "wrap, center");
        panel.add(slogan, "center");
        return panel;
    }

    private JPanel createLoginSection() {
        JPanel section = new JPanel(new MigLayout("fill, insets 60", "fill", "center"));
        section.setBackground(ThemeManager.BACKGROUND);

        JLabel title = new JLabel("Identity Access Management");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        ModernTextField userField = new ModernTextField("User ID / Email / Account ID");
        JPasswordField passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(300, 45));
        passField.putClientProperty("JTextField.placeholderText", "Secure Password");
        
        JButton showPass = new JButton("👁");
        showPass.setPreferredSize(new Dimension(45, 45));
        showPass.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        showPass.addActionListener(e -> {
            if (passField.getEchoChar() == (char)0) {
                passField.setEchoChar('•');
                showPass.setText("👁");
            } else {
                passField.setEchoChar((char)0);
                showPass.setText("🙈");
            }
        });
        
        // CAPTCHA UI
        refreshCaptcha();
        captchaLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        captchaLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { refreshCaptcha(); }
        });

        ModernButton loginBtn = new ModernButton("Verify & Authorize", ModernButton.Type.PRIMARY);
        loginBtn.addActionListener(e -> {
            String input = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String capt = captchaInput.getText().trim();

            if (input.isEmpty() || password.isEmpty() || capt.isEmpty()) {
                Toast.show(this, "All fields are mandatory", Toast.Type.ERROR);
                return;
            }
            
            if (input.length() < 3) {
                Toast.show(this, "Invalid user identifier", Toast.Type.ERROR);
                return;
            }

            if (!captchaHelper.validate(capt)) {
                Toast.show(this, "Security code mismatch", Toast.Type.ERROR);
                refreshCaptcha();
                return;
            }

            new Thread(() -> {
                try {
                    User user = bankingService.login(input, password);
                    SwingUtilities.invokeLater(() -> {
                        // Admin-Only OTP requirement or Sensitive User
                        String otp = SecurityUtils.generateOTP();
                        new OTPScreen(this, otp, (verified) -> {
                            if (verified) {
                                try {
                                    SessionManager.getInstance().login(user, null);
                                    
                                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                                        AdminDashboard adminDashboard = new AdminDashboard();
                                        adminDashboard.setVisible(true);
                                    } else {
                                        CustomerDashboard customerDashboard = new CustomerDashboard();
                                        customerDashboard.setVisible(true);
                                    }
                                    
                                    this.dispose();
                                } catch (Exception ex) {
                                    if(ex.getMessage().contains("DEACTIVATED")) {
                                        handleActivation(user.getEmail());
                                    } else {
                                        Toast.show(this, "Access Error: " + ex.getMessage(), Toast.Type.ERROR);
                                    }
                                }
                            }
                        }).setVisible(true);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        Toast.show(this, ex.getMessage(), Toast.Type.ERROR);
                        refreshCaptcha();
                    });
                }
            }).start();
        });

        ModernButton registerBtn = new ModernButton("Open New Account", ModernButton.Type.SECONDARY);
        registerBtn.addActionListener(e -> { new RegistrationScreen().setVisible(true); this.dispose(); });

        section.add(title, "wrap, gapbottom 30");
        section.add(new JLabel("Username / Account ID"), "wrap");
        section.add(userField, "wrap, height 45!, gapbottom 15");
        section.add(new JLabel("Secure Password"), "wrap");
        
        JPanel passPanel = new JPanel(new MigLayout("insets 0", "[grow]5[]", "[]"));
        passPanel.setBackground(ThemeManager.BACKGROUND);
        passPanel.add(passField, "grow");
        passPanel.add(showPass);
        section.add(passPanel, "wrap, height 45!, gapbottom 20");
        
        section.add(new JLabel("Security Verification (Click to refresh)"), "wrap");
        section.add(captchaLabel, "wrap, gapbottom 10");
        section.add(captchaInput, "wrap, height 45!, gapbottom 30");
        
        section.add(loginBtn, "wrap, height 50!, gapbottom 10");
        section.add(registerBtn, "height 50!");

        return section;
    }

    private void handleActivation(String email) {
        String otp = SecurityUtils.generateOTP();
        new OTPScreen(this, otp, (verified) -> {
            if (verified) {
                try {
                    bankingService.activateAccount(email);
                    Toast.show(this, "Account Reactivated! Please login again.", Toast.Type.SUCCESS);
                } catch (Exception ex) { Toast.show(this, "Activation failed", Toast.Type.ERROR); }
            }
        }).setVisible(true);
    }

    private void refreshCaptcha() {
        captchaLabel.setIcon(new ImageIcon(captchaHelper.generateCaptchaImage()));
        captchaInput.setText("");
    }
}
