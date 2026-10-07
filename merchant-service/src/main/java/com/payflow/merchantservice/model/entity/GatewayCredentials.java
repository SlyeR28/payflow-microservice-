package com.payflow.merchantservice.model.entity;

import com.payflow.merchantservice.model.enums.GatewayProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "merchant_gateway_configs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_merchant_gateway", columnNames = {"merchant_id", "gateway_type"})
        },
        indexes = {
                @Index(name = "idx_gateway_merchant_active", columnList = "merchant_id,is_active")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatewayCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_type", nullable = false, length = 30)
    private GatewayProvider gatewayType;

    @Column(name = "api_key_encrypted", nullable = false, columnDefinition = "TEXT")
    private String apiKeyEncrypted;

    @Column(name = "api_secret_encrypted", nullable = false, columnDefinition = "TEXT")
    private String apiSecretEncrypted;

    @Column(name = "webhook_secret_encrypted", columnDefinition = "TEXT")
    private String webhookSecretEncrypted;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_test_mode", nullable = false)
    private Boolean isTestMode;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (isActive == null) {
            isActive = true;
        }
        if (isTestMode == null) {
            isTestMode = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
