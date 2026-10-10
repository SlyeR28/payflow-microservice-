package com.payflow.authservice.config;

import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.model.entity.UserCredentials;
import com.payflow.authservice.model.enums.UserStatus;
import com.payflow.authservice.repository.UserCredentialRepository;
import com.payflow.authservice.repository.UserRepository;
import com.payflow.common.constant.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String ADMIN_EMAIL = "admin@payflow.com";
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "Admin@PayFlow2026!";

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL) || userRepository.existsByUserName(ADMIN_USERNAME)) {
            userRepository.findByEmail(ADMIN_EMAIL).ifPresent(user -> {
                boolean modified = false;
                if (!user.getRoles().contains(Roles.ADMIN)) {
                    user.getRoles().add(Roles.ADMIN);
                    modified = true;
                }
                if (!user.getRoles().contains(Roles.MERCHANT)) {
                    user.getRoles().add(Roles.MERCHANT);
                    modified = true;
                }
                if (!user.getRoles().contains(Roles.CUSTOMER)) {
                    user.getRoles().add(Roles.CUSTOMER);
                    modified = true;
                }
                if (user.getUserStatus() != UserStatus.ACTIVE) {
                    user.setUserStatus(UserStatus.ACTIVE);
                    modified = true;
                }
                if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                    user.setEmailVerified(true);
                    modified = true;
                }
                if (modified) {
                    userRepository.save(user);
                    log.info("Updated existing admin user credentials & roles to [ADMIN, MERCHANT, CUSTOMER]");
                }

                if (!userCredentialRepository.existsByUserId(user.getId())) {
                    userCredentialRepository.save(UserCredentials.builder()
                            .user(user)
                            .password(passwordEncoder.encode(ADMIN_PASSWORD))
                            .passwordChangedAt(Instant.now())
                            .resetTokenConsumed(true)
                            .build());
                    log.info("Created missing credentials for admin user: {}", ADMIN_EMAIL);
                }
            });
            log.info("Admin user verified in Auth database: {}", ADMIN_EMAIL);
            return;
        }

        log.info("Seeding default Admin user into Auth database...");

        Set<Roles> roles = new HashSet<>();
        roles.add(Roles.ADMIN);
        roles.add(Roles.MERCHANT);
        roles.add(Roles.CUSTOMER);

        User adminUser = User.builder()
                .userName(ADMIN_USERNAME)
                .email(ADMIN_EMAIL)
                .firstName("System")
                .lastName("Admin")
                .phoneNumber("+919876543210")
                .roles(roles)
                .userStatus(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedAttempts(0)
                .userNameChangedAt(Instant.now())
                .userNameReminderCount(0)
                .build();

        User savedAdmin = userRepository.save(adminUser);

        UserCredentials credentials = UserCredentials.builder()
                .user(savedAdmin)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .passwordChangedAt(Instant.now())
                .resetTokenConsumed(true)
                .build();

        userCredentialRepository.save(credentials);

        log.info("====================================================================");
        log.info(">>> Admin user successfully seeded! <<<");
        log.info("Email:    {}", ADMIN_EMAIL);
        log.info("Username: {}", ADMIN_USERNAME);
        log.info("Password: {}", ADMIN_PASSWORD);
        log.info("Roles:    [ADMIN, MERCHANT, CUSTOMER]");
        log.info("====================================================================");
    }
}
