package com.payflow.merchantservice.utils;


import com.payflow.merchantservice.payload.requestDto.AddressRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final  class AddressHashUtility {

    private static final String DELIMITER = "|";

    public AddressHashUtility() {
    }

    /**
     * Computes a deterministic SHA-256 hash for given address components
     * Guarantees to produce identical hash for address with identical physical semantics
     */

    public static String computeHash(AddressRequest request){
        String conical = String.join(
                DELIMITER,
                normalizeText(request.getCountry()),
                normalizeText(request.getState()),
                normalizeText(request.getCity()),
                normalizeText(request.getAddressLine1()),
                normalizeText(request.getAddressLine2()),
                normalizeText(request.getPostalCode())
        );
        return sha256(conical);
    }

   public static String normalizeText(String input){
        if (input == null) {
            return "";
        }
        return input.toLowerCase()
                .replaceAll("[^a-z0\\s]" , "")
                .replaceAll( "\\s+" , " ")
                .trim();
   }

   private static String sha256(String input) {
       try{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
       }catch (NoSuchAlgorithmException e){
           throw new IllegalArgumentException("Failed to compute SHA-256 hash", e);
       }
   }
}
