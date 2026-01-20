package com.vaultx.utils;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    
    // Core Colors
    public static final Color PRIMARY = Color.decode("#6C63FF");
    public static final Color SECONDARY = Color.decode("#00E5A8");
    public static final Color BACKGROUND = Color.decode("#0F1117");
    public static final Color SURFACE = Color.decode("#1A1D26");
    public static final Color CARD = Color.decode("#222634");
    public static final Color TEXT_PRIMARY = Color.decode("#FFFFFF");
    public static final Color TEXT_SECONDARY = Color.decode("#B0B3C2");
    public static final Color SUCCESS = Color.decode("#2ED573");
    public static final Color WARNING = Color.decode("#FFA502");
    public static final Color ERROR = Color.decode("#FF4757");

    public static void setup() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            
            // Global UI Customization
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            
            UIManager.put("Panel.background", BACKGROUND);
            UIManager.put("ScrollBar.track", BACKGROUND);
            UIManager.put("ScrollBar.thumb", SURFACE);
            
            // Set default font
            // System.setProperty("flatlaf.uiScale", "1.0"); // Example scaling
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
    }

    public static void applyCardStyle(JComponent component) {
        component.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 16; " +
            "background: #222634; " +
            "border: 1,1,1,1,#2D3244");
    }

    public static void applyPrimaryButtonStyle(JButton button) {
        button.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 12; " +
            "background: #6C63FF; " +
            "foreground: #FFFFFF; " +
            "borderWidth: 0; " +
            "focusWidth: 0; " +
            "hoverBackground: #7D75FF");
    }
    
    public static void applySecondaryButtonStyle(JButton button) {
        button.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 12; " +
            "background: #1A1D26; " +
            "foreground: #00E5A8; " +
            "borderWidth: 1; " +
            "borderColor: #00E5A8; " +
            "focusWidth: 0; " +
            "hoverBackground: #222634");
    }
}
