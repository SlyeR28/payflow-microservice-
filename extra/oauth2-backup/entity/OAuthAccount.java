package com.payflow.authservice.model.entity;

import com.payflow.authservice.model.enums.OAuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "oauth_accounts",
        indexes = {
                @Index(name = "idx_oauth_provider_user_id",
                        columnList = "provider,provider_user_id", unique = true),
                @Index(name = "idx_oauth_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OAuthProvider provider;

    /**
     * The provider's stable user ID — Google's "sub" claim.
     * Never changes even if the user's email changes.
     */
    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    @PrePersist
    void onCreate() {
        if (linkedAt == null) linkedAt = Instant.now();
    }
}
