package com.payflow.authservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "user_credentials" ,
        indexes = {
                @Index(name = "idx_users_user_id", columnList = "user_id" , unique = true),

        }
)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCredentials {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;


        /**
         * one-to-one with User
         * Unique Constraint ensures exactly one credential row per user
         * Lazy b/c we only load credentials during login/password flow
         */
        @OneToOne(fetch = FetchType.LAZY , optional = false)
        @JoinColumn(name = "user_id" , nullable = false , unique = true)
        private User user;

        /**
         *  BCrypt hash of the password. Never plain text.
         *   Never logged. Never returned in any API response.
         */
        @Column(name = "password" , nullable = false , length = 255)
        private String password;

        @Column(name = "password_changed_at" , nullable = false)
        private Instant passwordChangedAt;

        //-------------------Forgot Password-------------------

        /**
         * SHA-256 hash of the reset token sent via email.
         * Null when no reset is pending.
         * Hashed for the same reason passwords are hashed — DB leaks don't expose tokens.
         */
        @Column(name = "reset_token" , length = 64)
        private String resetToken;

        @Column(name = "reset_token_expires_at")
        private Instant resetTokenExpiresAt;

        /**
         * True when the reset token has been used OR never issued.
         * Prevents replay of a used token.
         */
        @Column(name = "reset_token_consumed")
        private Boolean resetTokenConsumed;


        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        @PrePersist
        void onCreate() {
                Instant now = Instant.now();
                createdAt = now;
                updatedAt = now;

                if (passwordChangedAt == null) passwordChangedAt = now;
                if (resetTokenConsumed == null) resetTokenConsumed = true;
        }

        @PreUpdate
        void onUpdate() {
                updatedAt = Instant.now();
        }


}
