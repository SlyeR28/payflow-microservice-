package com.payflow.authservice.model.entity;

import com.payflow.authservice.model.enums.UserStatus;
import com.payflow.common.constant.Roles;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users" ,
        indexes = {
                @Index(name = "idx_users_username", columnList = "user_name" , unique = true),
                @Index(name = "idx_users_email", columnList = "email" , unique = true),
                @Index(name = "idx_users_username_reminder", columnList = "username_changed_at,user_status")

        }
)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name" , nullable = false, unique = true , length = 50)
    private String userName;

    @Column(name = "email" , nullable = false , unique = true , length = 255)
    private String email;

    @Column(name = "first_name" , length = 50)
    private String firstName;

    @Column(name = "last_name" , length = 50)
    private String lastName;

    @Column(name = "phone_number" , length = 20)
    private String phoneNumber;

    /**
     * Public URL to the user's avatar image.
     * The image bytes live in object storage (S3 / Cloudflare R2 / MinIO).
     * This column stores only the URL — never the binary.
     * Nullable: user may not have uploaded an avatar yet.
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles" , joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false , length = 50)
    @Builder.Default
    private Set<Roles> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status" , nullable = false , length = 50)
    private UserStatus userStatus;

    @Column(name = "email_verified" , nullable = false)
    private Boolean emailVerified;

    @Column(name = "failed_attempts" , nullable = false)
    private Integer failedAttempts;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at" , nullable = false , updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at" , nullable = false)
    private Instant updatedAt;

    /**
     * null = user is still using the auto-generated username.
     * Non-null = Timestamp of last self-chosen username change
     * Used for 45 -days cooldown and reminder Schedular
     */

    @Column(name = "username_changed_at")
    private Instant userNameChangedAt;

    @Column(name = "last_username_reminder_at")
    private Instant lastUserNameReminderAt;

    @Column(name = "username_reminder_count")
    private Integer userNameReminderCount;


    public void addRole(Roles role){
        if (this.roles == null){
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public void removeRole(Roles role){
        if (this.roles != null){
            this.roles.remove(role);
        }
    }

    public boolean hasRole(Roles role){
        return this.roles != null && this.roles.contains(role);
    }


    @PrePersist
    public void onCreate(){
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;

        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add(Roles.CUSTOMER);
        }

        if (userStatus == null)userStatus = UserStatus.PENDING_VERIFICATION;
        if (emailVerified == null)emailVerified = false;
        if (failedAttempts == null)failedAttempts = 0;
        if (userNameReminderCount == null) userNameReminderCount = 0;
    }


    @PreUpdate
    public void onUpdate(){
        updatedAt = Instant.now();
    }

}
