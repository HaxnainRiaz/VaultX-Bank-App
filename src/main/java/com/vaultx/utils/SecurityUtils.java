package com.vaultx.utils;

import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;

public class SecurityUtils {
    
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
    
    public static boolean checkPassword(String plainPassword, String hashed) {
        try {
            return BCrypt.checkpw(plainPassword, hashed);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    public static String generateAccountId() {
        SecureRandom random = new SecureRandom();
        int id = 100000 + random.nextInt(900000);
        return String.valueOf(id);
    }
}
