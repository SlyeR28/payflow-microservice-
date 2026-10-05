package com.payflow.authservice.repository;

import com.payflow.authservice.model.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredentials, Long> {

    Optional<UserCredentials> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
