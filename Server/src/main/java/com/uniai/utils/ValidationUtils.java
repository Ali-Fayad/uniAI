package com.uniai.utils;

import java.util.regex.Pattern;

/**
 * Utility helpers for trimming, capitalization, lowercase conversion and common
 * regex checks.
 * Rules referenced: validation_rules.MD (First/Last name, username, email,
 * password storage rules).
 */
public class ValidationUtils {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{2,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern SHA256_HEX = Pattern.compile("^[a-fA-F0-9]{64}$");

    public static String trim(String s) {
        return s == null ? null : s.trim();
    }

    public static String capitalizeName(String s) {
        if (s == null)
            return null;
        String t = trim(s);
        if (t.isEmpty())
            return t;
        return t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase();
    }

    public static String toLower(String s) {
        if (s == null)
            return null;
        return trim(s).toLowerCase();
    }

    public static boolean isAlphaName(String s) {
        if (s == null)
            return false;
        return NAME_PATTERN.matcher(trim(s)).matches();
    }

    public static boolean isValidUsername(String s) {
        if (s == null)
            return false;
        return USERNAME_PATTERN.matcher(trim(s)).matches();
    }

    public static boolean isValidEmail(String s) {
        if (s == null)
            return false;
        return EMAIL_PATTERN.matcher(trim(s)).matches();
    }

    /**
     * Frontend must submit SHA-256 hex string; backend validates format of received
     * hash.
     */
    public static boolean isValidFrontendPasswordHash(String s) {
        if (s == null)
            return false;
        return SHA256_HEX.matcher(s).matches();
    }
}
