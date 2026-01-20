package com.vaultx.ui.components;

import com.vaultx.utils.ThemeManager;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Toast extends JWindow {
    
    public enum Type { SUCCESS, ERROR, INFO }
    
    private final int duration = 3000;

    public Toast(JFrame parent, String message, Type type) {
        super(parent);
        
        JPanel panel = new JPanel(new MigLayout("fill, insets 10 20 10 20", "[]15[fill]", "center"));
        panel.setBackground(type == Type.SUCCESS ? ThemeManager.SUCCESS : 
                          type == Type.ERROR ? ThemeManager.ERROR : ThemeManager.SURFACE);
        
        JLabel iconLbl = new JLabel(); // You can add icons here
        JLabel msgLbl = new JLabel(message);
        msgLbl.setFont(new Font("Inter", Font.BOLD, 13));
        msgLbl.setForeground(Color.WHITE);
        
        panel.add(iconLbl);
        panel.add(msgLbl);
        
        setContentPane(panel);
        setShape(new RoundRectangle2D.Double(0, 0, getPreferredSize().width, getPreferredSize().height, 12, 12));
        pack();
        
        // Position toast at the bottom center of the parent
        Point p = parent.getLocationOnScreen();
        int x = p.x + (parent.getWidth() - getWidth()) / 2;
        int y = p.y + parent.getHeight() - getHeight() - 50;
        setLocation(x, y);
    }

    public static void show(JFrame parent, String message, Type type) {
        Toast toast = new Toast(parent, message, type);
        toast.setVisible(true);
        
        new Thread(() -> {
            try {
                Thread.sleep(toast.duration);
                // Simple fade logic or just dispose
                toast.dispose();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
