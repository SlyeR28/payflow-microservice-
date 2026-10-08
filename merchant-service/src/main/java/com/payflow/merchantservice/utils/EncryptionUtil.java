package com.payflow.merchantservice.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for AES-256-GCM (Galois/Counter Mode) authenticated encryption and decryption.
 *
 * Security guarantees:
 * - Confidentiality + Authenticity / Integrity (AEAD)
 * - Fresh cryptographically secure random 12-byte IV per encryption operation
 * - 128-bit authentication tag to prevent tampering or ciphertext manipulation
 * - Payload structure: Base64([12-byte IV] + [Ciphertext + 16-byte Auth Tag])
 */
@Slf4j
@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128; // 128-bit authentication tag
    private static final int IV_LENGTH_BYTE = 12;  // 96-bit IV recommended by NIST SP 800-38D

    @Value("${app.security.encryption-key:payflow-256-bit-aes-gcm-secret-key-32b!}")
    private String secretKeyString;

    private SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionUtil() {
    }

    public EncryptionUtil(String secretKeyString) {
        this.secretKeyString = secretKeyString;
        init();
    }

    @PostConstruct
    public void init() {
        try {
            // Derive deterministic 256-bit (32 bytes) key using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secretKeyString.getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
            log.info("EncryptionUtil successfully initialized with AES-256-GCM.");
        } catch (Exception e) {
            log.error("Failed to initialize AES key in EncryptionUtil", e);
            throw new IllegalStateException("Could not initialize AES-256 key", e);
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Generates a unique 12-byte IV for every invocation.
     *
     * @param plainText sensitive string to encrypt (e.g., account number, PAN)
     * @return Base64-encoded string combining IV and ciphertext + tag
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Failed to encrypt data: {}", e.getMessage());
            throw new SecurityException("Failed to encrypt sensitive data", e);
        }
    }

    /**
     * Decrypts an AES-256-GCM payload.
     * Extracts the 12-byte IV from the front and decrypts the ciphertext.
     *
     * @param cipherTextBase64 Base64 string produced by {@link #encrypt(String)}
     * @return original plaintext
     */
    public String decrypt(String cipherTextBase64) {
        if (cipherTextBase64 == null || cipherTextBase64.isBlank()) {
            return cipherTextBase64;
        }

        try {
            byte[] cipherMessage = Base64.getDecoder().decode(cipherTextBase64);
            if (cipherMessage.length < IV_LENGTH_BYTE + (TAG_LENGTH_BIT / 8)) {
                throw new IllegalArgumentException("Invalid ciphertext length: payload corrupted or truncated");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt data: {}", e.getMessage());
            throw new SecurityException("Failed to decrypt sensitive data", e);
        }
    }
}

