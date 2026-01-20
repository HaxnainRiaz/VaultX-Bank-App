package com.vaultx.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaHelper {
    private String captchaText;

    public BufferedImage generateCaptchaImage() {
        captchaText = generateRandomText();
        int width = 120, height = 40;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        g.setFont(new Font("Arial", Font.ITALIC | Font.BOLD, 22));
        g.setColor(new Color(45, 111, 40));
        g.drawString(captchaText, 15, 28);
        
        // Add some noise
        Random r = new Random();
        for(int i=0; i<30; i++) {
            g.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            g.drawRect(r.nextInt(width), r.nextInt(height), 1, 1);
        }
        
        g.dispose();
        return img;
    }

    private String generateRandomText() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for(int i=0; i<5; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

    public String getCaptchaText() { return captchaText; }
    
    public boolean validate(String input) {
        return captchaText != null && captchaText.equalsIgnoreCase(input);
    }
}
