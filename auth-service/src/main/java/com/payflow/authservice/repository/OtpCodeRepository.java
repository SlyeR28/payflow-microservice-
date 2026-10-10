package com.payflow.authservice.repository;


import com.payflow.authservice.model.entity.OtpCode;
import com.payflow.authservice.model.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

  @Query("""
              SELECT o FROM OtpCode o 
                            WHERE o.email = :email
                            AND o.purpose = :purpose
                            AND o.consumed = false
                            AND o.expiresAt > :now 
                           ORDER BY o.createdAt DESC                                    
              """)
  Optional<OtpCode>findLatestValid(
          @Param("email") String email,
          @Param("purpose") OtpPurpose purpose,
          @Param("now") Instant now
  );
  /**
   * Invalidate all pending OTPs for an email + purpose.
   * Called before issuing a new OTP.
   */
  @Transactional
  @Modifying
  @Query("""
           UPDATE OtpCode o
           SET o.consumed = true
           WHERE o.email = :email
             AND o.purpose = :purpose
             AND o.consumed = false
           """)
  int invalidateAll(
          @Param("email") String email,
          @Param("purpose") OtpPurpose purpose);

  /**
   * Delete expired OTPs. Called by a scheduled cleanup job.
   */
  @Transactional
  @Modifying
  @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :cutoff")
  int deleteExpiredBefore(@Param("cutoff") Instant cutoff);


}
