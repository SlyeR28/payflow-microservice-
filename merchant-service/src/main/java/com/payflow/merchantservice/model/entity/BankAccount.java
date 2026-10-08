package com.payflow.merchantservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "merchant_bank_accounts", indexes = {
        @Index(name = "idx_bank_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_bank_account_hash", columnList = "account_number_hash"),
        @Index(name = "idx_bank_primary", columnList = "merchant_id,is_primary")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "account_holder_name", nullable = false, length = 200)
    private String accountHolderName;

    //----------Account Number: encrypted + hashed + masked--------

    @Column(name = "account_number_encrypted", nullable = false, columnDefinition = "TEXT")
    private String accountNumberEncrypted;

    @Column(name = "account_number_hash", nullable = false , length = 64)
    private String accountNumberHash;

    @Column(name = "account_number_last4", nullable = false, length = 4)
    private String accountNumberLast4;

    @Column(name = "account_number_key_version")
    private Integer accountNumberKeyVersion;

    // -------------------bank details-------------------

    @Column(name = "ifsc_code", nullable = false, length = 20)
    private String ifscCode;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

   //----- beneficiary name returned from bank penny drop (nullable until verified)
    @Column(name = "beneficiary_name", length = 150)
    private String beneficiaryName;

    //----- name match score that most be response from bank side when peny drop
    @Column(name = "name_match_score")
    private Double nameMatchScore;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "verification_reference_id", length = 100)
    private String verificationReferenceId;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
       createdAt = Instant.now();
       updatedAt = Instant.now();
       if(isPrimary == null)isPrimary = false;
       if(isVerified == null)isVerified = false;
       if(accountNumberKeyVersion == null)accountNumberKeyVersion = 1;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
