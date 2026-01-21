package com.vaultx.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex Patterns
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_PATTERN = "^\\+92[0-9]{10}$";
    private static final String NAME_PATTERN = "^[a-zA-Z\\s]{3,50}$";
    private static final String ACCOUNT_ID_PATTERN = "^[A-Z0-9-]{5,20}$";

    /**
     * Validates if the given email is in a valid format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && Pattern.matches(EMAIL_PATTERN, email);
    }

    /**
     * Validates if the given Pakistani phone number is in valid format
     * (+92XXXXXXXXXX).
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && Pattern.matches(PHONE_PATTERN, phone);
    }

    /**
     * Validates if the name contains only letters and spaces, and is of reasonable
     * length.
     */
    public static boolean isValidFullName(String name) {
        return name != null && Pattern.matches(NAME_PATTERN, name);
    }

    /**
     * Master validation for financial transactions.
     * Prevents negative amounts, zero, and overflow attempts.
     */
    public static void validateAmount(double amount) throws Exception {
        if (amount <= 0) {
            throw new Exception("Transaction amount must be a positive value greater than zero.");
        }
        if (amount > 10_000_000) { // Safety cap for individual transactions
            throw new Exception("Amount exceeds maximum allowed single transaction limit (Rs. 10M).");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new Exception("Invalid numeric amount detected.");
        }
    }

    /**
     * Validates if string is a valid account ID.
     */
    public static boolean isValidAccountId(String accountId) {
        return accountId != null && Pattern.matches(ACCOUNT_ID_PATTERN, accountId);
    }

    /**
     * Sanitizes input to prevent basic script injections or layout breaking.
     */
    public static String sanitize(String input) {
        if (input == null)
            return "";
        return input.trim()
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * Validates password strength (MASTER LEVEL: 8+ chars, upper, lower, number,
     * special).
     */
    public static void validatePasswordStrength(String password) throws Exception {
        if (password == null || password.length() < 8) {
            throw new Exception("Security violation: Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new Exception("Security violation: Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new Exception("Security violation: Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new Exception("Security violation: Password must contain at least one numerical digit.");
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new Exception("Security violation: Password must contain at least one special character.");
        }
    }
}
