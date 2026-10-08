package com.payflow.merchantservice.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil("test-super-secret-key-for-aes-256-encryption!");
    }

    @Test
    void testEncryptAndDecrypt_Success() {
        String originalText = "50100456789123"; // sensitive bank account number or PAN
        String cipherText = encryptionUtil.encrypt(originalText);

        assertNotNull(cipherText);
        assertNotEquals(originalText, cipherText);

        String decryptedText = encryptionUtil.decrypt(cipherText);
        assertEquals(originalText, decryptedText);
    }

    @Test
    void testEncryptProducesDifferentCiphertexts_DueToRandomIV() {
        String originalText = "ABCDE1234F"; // PAN number
        String cipher1 = encryptionUtil.encrypt(originalText);
        String cipher2 = encryptionUtil.encrypt(originalText);

        assertNotEquals(cipher1, cipher2, "Each encryption must use a fresh IV/nonce");
        assertEquals(originalText, encryptionUtil.decrypt(cipher1));
        assertEquals(originalText, encryptionUtil.decrypt(cipher2));
    }

    @Test
    void testDecrypt_TamperedCiphertext_ThrowsException() {
        String originalText = "ConfidentialPaymentData";
        String cipherText = encryptionUtil.encrypt(originalText);

        byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
        // Flip one bit in the ciphertext payload
        cipherBytes[cipherBytes.length - 1] ^= (byte) 0x01;
        String tamperedCipher = Base64.getEncoder().encodeToString(cipherBytes);

        assertThrows(SecurityException.class, () -> encryptionUtil.decrypt(tamperedCipher));
    }

    @Test
    void testNullOrBlankHandling() {
        assertNull(encryptionUtil.encrypt(null));
        assertNull(encryptionUtil.decrypt(null));
        assertEquals("", encryptionUtil.encrypt(""));
        assertEquals("", encryptionUtil.decrypt(""));
    }
}
