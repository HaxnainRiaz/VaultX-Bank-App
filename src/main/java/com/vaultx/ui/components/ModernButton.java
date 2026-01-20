package com.vaultx.ui.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.vaultx.utils.ThemeManager;
import javax.swing.*;
import java.awt.*;

public class ModernButton extends JButton {
    public enum Type { PRIMARY, SECONDARY, DANGER }
    
    public ModernButton(String text, Type type) {
        super(text);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(new Font("Inter", Font.BOLD, 14));
        
        switch (type) {
            case PRIMARY -> ThemeManager.applyPrimaryButtonStyle(this);
            case SECONDARY -> ThemeManager.applySecondaryButtonStyle(this);
            case DANGER -> {
                putClientProperty(FlatClientProperties.STYLE, 
                    "arc: 12; " +
                    "background: #FF4757; " +
                    "foreground: #FFFFFF; " +
                    "borderWidth: 0; " +
                    "hoverBackground: #FF6B81");
            }
        }
    }
}
