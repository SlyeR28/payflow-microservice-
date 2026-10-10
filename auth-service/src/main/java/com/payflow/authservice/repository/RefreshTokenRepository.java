package com.payflow.authservice.repository;

import com.payflow.authservice.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken>findByTokenHash(String tokenHash);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedReason = :reason WHERE r.familyId = :familyId AND r.revoked = false")
    int revokeFamily(@Param("familyId") String familyId, @Param("reason") String reason);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedReason = :reason WHERE r.user.id = :userId AND r.revoked = false")
    int revokeAllForUser(@Param("userId") Long userId, @Param("reason") String reason);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
