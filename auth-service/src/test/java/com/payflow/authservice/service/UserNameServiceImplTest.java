package com.payflow.authservice.service;

import com.payflow.authservice.exceptions.UserNameAlreadyTakenException;
import com.payflow.authservice.exceptions.UserNameCoolDownException;
import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.payload.requestDto.UsernameUpdateRequest;
import com.payflow.authservice.payload.responseDto.UsernameUpdateResponse;
import com.payflow.authservice.repository.UserRepository;
import com.payflow.authservice.service.impl.UserNameServiceImpl;
import com.payflow.common.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserNameServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserNameServiceImpl userNameService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(101L)
                .userName("old_username")
                .userNameChangedAt(null)
                .build();
    }

    @Test
    @DisplayName("updateUsername: succeeds on first username change")
    void testUpdateUsernameFirstChangeSuccess() {
        UsernameUpdateRequest request = new UsernameUpdateRequest();
        request.setUserName("brand_new_name");

        when(userRepository.findById(101L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByUserName("brand_new_name")).thenReturn(Optional.empty());

        UsernameUpdateResponse response = userNameService.updateUsername(101L, request);

        assertNotNull(response);
        assertEquals("brand_new_name", response.getUserName());
        assertEquals("brand_new_name", sampleUser.getUserName());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("updateUsername: throws UserNameAlreadyTakenException when name already taken")
    void testUpdateUsernameAlreadyTaken() {
        UsernameUpdateRequest request = new UsernameUpdateRequest();
        request.setUserName("taken_name");

        when(userRepository.findById(101L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByUserName("taken_name"))
                .thenReturn(Optional.of(User.builder().id(999L).userName("taken_name").build()));

        assertThrows(UserNameAlreadyTakenException.class, () ->
                userNameService.updateUsername(101L, request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUsername: throws UserNameCoolDownException when within 45 days of last change")
    void testUpdateUsernameCoolDownActive() {
        sampleUser.setUserNameChangedAt(Instant.now().minus(10, ChronoUnit.DAYS));

        UsernameUpdateRequest request = new UsernameUpdateRequest();
        request.setUserName("too_early_name");

        when(userRepository.findById(101L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByUserName("too_early_name")).thenReturn(Optional.empty());

        assertThrows(UserNameCoolDownException.class, () ->
                userNameService.updateUsername(101L, request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUsername: throws ResourceNotFoundException when user does not exist")
    void testUpdateUsernameUserNotFound() {
        UsernameUpdateRequest request = new UsernameUpdateRequest();
        request.setUserName("any_name");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userNameService.updateUsername(999L, request));
    }
}
