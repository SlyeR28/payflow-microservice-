package com.payflow.authservice.repository;

import com.payflow.authservice.model.entity.OAuthAccount;
import com.payflow.authservice.model.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndProviderUserId(
            OAuthProvider provider,
            String providerUserId
    );
}
