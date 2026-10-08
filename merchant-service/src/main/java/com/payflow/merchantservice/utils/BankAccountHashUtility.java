package com.payflow.merchantservice.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class BankAccountHashUtility {

    private BankAccountHashUtility() {
    }

    /**
     * Computes a deterministic 64-character SHA-256 hash for a bank account number.
     * Guaranteed to match the @Column(name = "account_number_hash", length = 64) column.
     *
     * @param accountNumber raw account number
     * @return 64-character hex SHA-256 hash string
     */
    public static String computeHash(String accountNumber) {
        String normalized = normalize(accountNumber);
        return sha256(normalized);
    }

    /**
     * Normalizes account number by trimming and removing dashes or spaces.
     * Example: " 1234-5678-90 " -> "1234567890"
     */
    public static String normalize(String accountNumber) {
        if (accountNumber == null) {
            return "";
        }
        return accountNumber
                .replaceAll("[\\s-]+", "")
                .trim();
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
