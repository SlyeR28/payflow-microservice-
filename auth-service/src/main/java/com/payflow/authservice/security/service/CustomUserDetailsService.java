package com.payflow.authservice.security.service;

import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.model.entity.UserCredentials;
import com.payflow.authservice.repository.UserCredentialRepository;
import com.payflow.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialsRepository;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with identifier: " + identifier));

        UserCredentials userCredentials = userCredentialsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User credentials not found for user: " + identifier));

        return new CustomUserDetails(user, userCredentials);


    }
}
