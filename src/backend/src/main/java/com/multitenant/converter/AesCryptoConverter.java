package com.multitenant.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
@Component
public class AesCryptoConverter implements AttributeConverter<String, String> {

    private static String staticAesKey;

    // We inject the key from Spring configurations. 
    // By storing it in a static field, we ensure that even if Hibernate instantiates 
    // the converter outside the Spring application context, the key is still accessible.
    @Value("${app.crypto.aes-key:DefaultAesEncryptionKey2026RegistruAgricol}")
    public void setAesKey(String aesKey) {
        staticAesKey = aesKey;
    }

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            String keyStr = staticAesKey != null ? staticAesKey : "DefaultAesEncryptionKey2026RegistruAgricol";
            byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(keyStr.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            // Generate a secure random IV for CBC mode
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext: [IV (16 bytes)][Ciphertext]
            byte[] combined = new byte[IV_SIZE + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, IV_SIZE);
            System.arraycopy(encryptedBytes, 0, combined, IV_SIZE, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting field", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            String keyStr = staticAesKey != null ? staticAesKey : "DefaultAesEncryptionKey2026RegistruAgricol";
            byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(keyStr.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            // Base64 decode
            byte[] combined = Base64.getDecoder().decode(dbData);
            if (combined.length < IV_SIZE) {
                // If it is too short to contain an IV, it is likely plain text (historical data)
                return dbData;
            }

            // Extract IV
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, IV_SIZE);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Extract Ciphertext
            int ciphertextLength = combined.length - IV_SIZE;
            byte[] encryptedBytes = new byte[ciphertextLength];
            System.arraycopy(combined, IV_SIZE, encryptedBytes, 0, ciphertextLength);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Graceful fallback for unencrypted or corrupted legacy historical database records.
            // If decryption fails, we return the cleartext database value as-is.
            return dbData;
        }
    }
}
