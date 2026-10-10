package com.payflow.merchantservice.model.entity;

import com.payflow.merchantservice.model.enums.BusinessType;
import com.payflow.merchantservice.model.enums.MerchantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "merchants", indexes = {
        @Index(name = "idx_merchants_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_merchants_business_email", columnList = "business_email"),
        @Index(name = "idx_merchants_pan_hash", columnList = "pan_number_hash"),
        @Index(name = "idx_merchants_status", columnList = "status")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "legal_name", nullable = false, length = 255)
    private String legalName;

    @Column(name = "business_email", nullable = false, length = 255)
    private String businessEmail;

    @Column(name = "business_phone", length = 20)
    private String businessPhone;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "business_category", length = 100)
    private String businessCategory;

    @Column(name = "pan_number_masked", length = 20)
    private String panNumberMasked;

    @Column(name = "pan_number_encrypted", columnDefinition = "TEXT")
    private String panNumberEncrypted;

    @Column(name = "pan_number_hash", length = 64)
    private String panNumberHash;

    @Column(name = "is_pan_verified", nullable = false)
    @Builder.Default
    private Boolean isPanVerified = false;

    @Column(name = "pan_verified_at")
    private Instant panVerifiedAt;
    @Column(name = "is_bank_verified", nullable = false)
    @Builder.Default
    private Boolean isBankVerified = false;

    @Column(name = "bank_verified_at")
    private Instant bankVerifiedAt;

    @Column(name = "gstin", length = 30)
    private String gstin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MerchantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false, length = 50)
    private BusinessType businessType;

    @Column(name = "rejection_reason", length = 500)
    private String rejectReason;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MerchantAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MerchantKyc> kycDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BankAccount> bankAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GatewayCredentials> gatewayConfigs = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (status == null) {
            status = MerchantStatus.PENDING;
        }
        if (isPanVerified == null) {
            isPanVerified = false;
        }
        if (isBankVerified == null) {
            isBankVerified = false;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
