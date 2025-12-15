package com.uniai.security.email;

import java.security.SecureRandom;

public final class EmailUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();


    public static String generateVerificationCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public static boolean isExpired(java.time.LocalDateTime expirationTime) {
        return expirationTime.isBefore(java.time.LocalDateTime.now());
    }
}
