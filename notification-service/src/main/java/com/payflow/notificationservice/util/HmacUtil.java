package com.payflow.notificationservice.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class HmacUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public HmacUtil() {}

    public static String calculateHmac(String key, String secret) {
        try{
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(secretKey);
            byte [] hmacByte = mac.doFinal(key.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacByte);
        }catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256 signature", e);
        }
    }

    private static String bytesToHex(byte [] bytes){
        StringBuilder hexString  = new StringBuilder();

        for (byte b : bytes){
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

}
