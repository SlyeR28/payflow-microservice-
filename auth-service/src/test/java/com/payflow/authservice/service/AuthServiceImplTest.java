package com.payflow.authservice.service;

import com.payflow.authservice.exceptions.EmailAlreadyRegisteredException;
import com.payflow.authservice.exceptions.InvalidCredentialsException;
import com.payflow.authservice.exceptions.PasswordMismatchException;
import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.model.entity.UserCredentials;
import com.payflow.authservice.model.enums.OtpPurpose;
import com.payflow.authservice.model.enums.UserStatus;
import com.payflow.authservice.payload.requestDto.LoginRequest;
import com.payflow.authservice.payload.requestDto.RegisterRequest;
import com.payflow.authservice.payload.responseDto.MessageResponse;
import com.payflow.authservice.payload.responseDto.TokenResponse;
import com.payflow.authservice.repository.UserCredentialRepository;
import com.payflow.authservice.repository.UserRepository;
import com.payflow.authservice.security.service.CustomUserDetails;
import com.payflow.authservice.service.impl.AuthServiceImpl;
import com.payflow.authservice.utils.UserNameGenerator;
import com.payflow.common.constant.Roles;
import com.payflow.common.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserNameGenerator userNameGenerator;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("clara@payflow.com")
                .userName("clara_merchant")
                .firstName("Clara")
                .lastName("Oswald")
                .phoneNumber("9876543210")
                .userStatus(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(Set.of(Roles.MERCHANT))
                .failedAttempts(0)
                .build();
    }

    @Test
    @DisplayName("registerUser: succeeds and dispatches OTP verification")
    void testRegisterUserSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@payflow.com")
                .password("SecureP@ss123")
                .confirmPassword("SecureP@ss123")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(Roles.MERCHANT))
                .build();

        when(userRepository.existsByEmail("newuser@payflow.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password_xyz");
        when(userNameGenerator.generateFromEmail(anyString())).thenReturn("john_doe_42");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = authService.registerUser(request);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
        verify(userCredentialRepository).save(any(UserCredentials.class));
        verify(otpService).generateAndSend(eq("newuser@payflow.com"), eq(OtpPurpose.EMAIL_VERIFICATION));
        verify(emailService).sendTemporaryUsernameEmail(eq("newuser@payflow.com"), eq("john_doe_42"));
    }

    @Test
    @DisplayName("registerUser: throws PasswordMismatchException when passwords differ")
    void testRegisterUserPasswordMismatch() {
        RegisterRequest request = RegisterRequest.builder()
                .email("clara@payflow.com")
                .password("Password123")
                .confirmPassword("DifferentPassword")
                .build();

        assertThrows(PasswordMismatchException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: throws EmailAlreadyRegisteredException when email exists")
    void testRegisterUserEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("clara@payflow.com")
                .password("SecureP@ss123")
                .confirmPassword("SecureP@ss123")
                .build();

        when(userRepository.existsByEmail("clara@payflow.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: succeeds and issues tokens")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("clara@payflow.com");
        request.setPassword("CorrectPassword123");

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUser()).thenReturn(sampleUser);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(userRepository.findByUsernameOrEmail("clara@payflow.com"))
                .thenReturn(Optional.of(sampleUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenService.issueTokens(sampleUser)).thenReturn(TokenResponse.builder()
                .accessToken("access.token.jwt")
                .refreshToken("refresh.token.val")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build());

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access.token.jwt", response.getAccessToken());
        assertEquals(0, sampleUser.getFailedAttempts());
        assertNotNull(sampleUser.getLastLoginAt());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("login: throws BusinessException when account is locked")
    void testLoginAccountLocked() {
        sampleUser.setLockedUntil(Instant.now().plus(2, ChronoUnit.HOURS));

        LoginRequest request = new LoginRequest();
        request.setIdentifier("clara@payflow.com");
        request.setPassword("AnyPassword");

        when(userRepository.findByUsernameOrEmail("clara@payflow.com"))
                .thenReturn(Optional.of(sampleUser));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new LockedException("Account locked"));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals("ACCOUNT_LOCKED", ex.getErrorCode());
    }

    @Test
    @DisplayName("login: increments failed attempts on BadCredentialsException and locks at 5")
    void testLoginBadCredentialsLocksAccount() {
        sampleUser.setFailedAttempts(4);

        LoginRequest request = new LoginRequest();
        request.setIdentifier("clara@payflow.com");
        request.setPassword("WrongPassword");

        when(userRepository.findByUsernameOrEmail("clara@payflow.com"))
                .thenReturn(Optional.of(sampleUser));
        doThrow(new BadCredentialsException("Invalid password"))
                .when(authenticationManager).authenticate(any());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        assertEquals(5, sampleUser.getFailedAttempts());
        assertNotNull(sampleUser.getLockedUntil());
        verify(userRepository).save(sampleUser);
    }
}
