package com.payflow.authservice.security.oauth2;

import com.payflow.authservice.model.entity.OAuthAccount;
import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.model.enums.OAuthProvider;
import com.payflow.authservice.model.enums.UserStatus;
import com.payflow.authservice.repository.OAuthAccountRepository;
import com.payflow.authservice.repository.UserRepository;
import com.payflow.authservice.utils.UserNameGenerator;
import com.payflow.common.constant.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final GoogleOAuth2UserInfoExtractor googleOAuth2UserInfoExtractor;
    private final UserNameGenerator userNameGenerator;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User rawUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (!"google".equalsIgnoreCase(registrationId)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "OAuth2 provider [" + registrationId + "] is not supported. Only Google is currently configured."
            );
        }

        OAuth2UserInfoDto userInfo = googleOAuth2UserInfoExtractor.extract(rawUser.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email"),
                    "Email not provided by Google OAuth2"
            );
        }

        User user = reconcileGoogleUser(userInfo);

        if (userRequest instanceof OidcUserRequest oidcUserRequest) {
            return new CustomOAuth2User(
                    user,
                    rawUser.getAttributes(),
                    oidcUserRequest.getIdToken(),
                    new OidcUserInfo(rawUser.getAttributes())
            );
        }

        return new CustomOAuth2User(user, rawUser.getAttributes());
    }

    private User reconcileGoogleUser(OAuth2UserInfoDto userInfo) {
        // 1. Check if Google OAuth account is already registered
        Optional<OAuthAccount> oAuthAccountOpt = oAuthAccountRepository
                .findByProviderAndProviderUserId(OAuthProvider.GOOGLE, userInfo.getProviderUserId());

        if (oAuthAccountOpt.isPresent()) {
            User existingUser = oAuthAccountOpt.get().getUser();
            validateUserStatus(existingUser);
            updateUserTelemetry(existingUser, userInfo);
            return userRepository.save(existingUser);
        }

        // 2. Account linking via email match
        Optional<User> userByEmailOpt = userRepository.findByEmail(userInfo.getEmail());
        if (userByEmailOpt.isPresent()) {
            User existingUser = userByEmailOpt.get();
            validateUserStatus(existingUser);

            if (!userInfo.isEmailVerified()) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("unverified_email"),
                        "Cannot link account: Google email is not verified."
                );
            }

            linkOAuthAccount(existingUser, userInfo.getProviderUserId());
            existingUser.setEmailVerified(true);
            updateUserTelemetry(existingUser, userInfo);
            return userRepository.save(existingUser);
        }

        // 3. Auto-provision new user
        return provisionNewUser(userInfo);
    }

    private User provisionNewUser(OAuth2UserInfoDto userInfo) {
        String baseUserName = userNameGenerator.generateFromEmail(userInfo.getEmail());
        String generatedUserName = baseUserName;
        int counter = 1;
        while (userRepository.existsByUserName(generatedUserName)) {
            generatedUserName = baseUserName + counter++;
        }

        User newUser = User.builder()
                .userName(generatedUserName)
                .email(userInfo.getEmail())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .avatarUrl(userInfo.getAvatarUrl())
                .role(Roles.CUSTOMER)
                .userStatus(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedAttempts(0)
                .lastLoginAt(Instant.now())
                .userNameReminderCount(0)
                .build();

        User savedUser = userRepository.save(newUser);
        linkOAuthAccount(savedUser, userInfo.getProviderUserId());
        log.info("Provisioned new user ID [{}] via Google OAuth2", savedUser.getId());
        return savedUser;
    }

    private void linkOAuthAccount(User user, String providerUserId) {
        OAuthAccount account = OAuthAccount.builder()
                .user(user)
                .provider(OAuthProvider.GOOGLE)
                .providerUserId(providerUserId)
                .linkedAt(Instant.now())
                .build();
        oAuthAccountRepository.save(account);
        log.info("Linked Google account (sub={}) to user ID [{}]", providerUserId, user.getId());
    }

    private void updateUserTelemetry(User user, OAuth2UserInfoDto userInfo) {
        user.setLastLoginAt(Instant.now());
        if ((user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) && userInfo.getAvatarUrl() != null) {
            user.setAvatarUrl(userInfo.getAvatarUrl());
        }
    }

    private void validateUserStatus(User user) {
        if (user.getUserStatus() == UserStatus.SUSPENDED || user.getUserStatus() == UserStatus.DEACTIVATED) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_disabled"),
                    "User account is " + user.getUserStatus()
            );
        }
    }
}
