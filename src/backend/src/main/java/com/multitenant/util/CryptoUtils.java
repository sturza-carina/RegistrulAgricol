package com.multitenant.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {

    // Fixed salt matched with the SQL migration script V37
    private static final String SALT = "RegistruAgricolDeterministicSalt_2026";

    /**
     * Generates a deterministic SHA-256 hash of a string combined with a fixed salt.
     * Useful for Blind Indexing.
     *
     * @param value The raw string to hash (e.g., CNP or CUI)
     * @return Hexadecimal representation of the SHA-256 hash, or null if value is null
     */
    public static String hashSha256(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedValue = value + SALT;
            byte[] hash = digest.digest(saltedValue.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
