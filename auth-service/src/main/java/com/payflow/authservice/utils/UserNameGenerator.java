package com.payflow.authservice.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Locale;

@Component
public class UserNameGenerator {

        private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
        private static final SecureRandom RANDOM = new SecureRandom();

        /**
         * Generate a placeholder username from email:
         *   alice@example.com → "alice_9f3a"
         * Falls back to "user_XXXX" if email prefix is too short.
         */
        public String generateFromEmail(String email) {
            String base = email.split("@")[0]
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]", "");

            if (base.length() < 3) base = "user";
            if (base.length() > 40) base = base.substring(0, 40);

            String suffix = randomAlphanumeric(4);
            return base + "_" + suffix;
        }

        private String randomAlphanumeric(int length) {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
            }
            return sb.toString();
        }
    }

