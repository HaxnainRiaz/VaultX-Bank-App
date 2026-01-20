package com.vaultx.ui.screens;

import com.vaultx.ui.components.ModernButton;
import com.vaultx.ui.components.ModernTextField;
import com.vaultx.ui.components.Toast;
import com.vaultx.utils.ThemeManager;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class OTPScreen extends JDialog {
    private String correctOTP;
    private Consumer<Boolean> callback;

    public OTPScreen(Frame owner, String otp, Consumer<Boolean> callback) {
        super(owner, "Two-Factor Authentication", true);
        this.correctOTP = otp;
        this.callback = callback;
        
        System.out.println("DEBUG: OTP SENT TO USER -> " + otp);

        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new MigLayout("fill, insets 30", "[fill]", "[]20[]20[]"));
        
        JLabel title = new JLabel("Identity Verification");
        title.setFont(new Font("Inter", Font.BOLD, 22));
        add(title, "wrap");
        
        JLabel msg = new JLabel("<html>Please enter the security code sent to your registered device. For demo: <b>" + otp + "</b></html>");
        add(msg, "wrap");
        
        ModernTextField otpField = new ModernTextField("Enter 6-Digit Code");
        add(otpField, "wrap, height 45!");
        
        ModernButton verify = new ModernButton("Verify & Proceed", ModernButton.Type.PRIMARY);
        verify.addActionListener(e -> {
            if (otpField.getText().trim().equals(correctOTP)) {
                this.dispose();
                callback.accept(true);
            } else {
                Toast.show((JFrame)getOwner(), "Incorrect OTP. Access Denied.", Toast.Type.ERROR);
            }
        });
        add(verify, "height 50!");
    }
}
