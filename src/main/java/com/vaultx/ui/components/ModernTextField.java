package com.vaultx.ui.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.vaultx.utils.ThemeManager;
import javax.swing.*;
import java.awt.*;

public class ModernTextField extends JTextField {
    public ModernTextField(String placeholder) {
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        putClientProperty(FlatClientProperties.STYLE, 
            "arc: 12; " +
            "background: #1A1D26; " +
            "margin: 8,12,8,12; " +
            "borderWidth: 1; " +
            "focusWidth: 1; " +
            "focusColor: #6C63FF");
        setFont(new Font("Inter", Font.PLAIN, 14));
        setForeground(ThemeManager.TEXT_PRIMARY);
        setCaretColor(ThemeManager.PRIMARY);
    }
}
