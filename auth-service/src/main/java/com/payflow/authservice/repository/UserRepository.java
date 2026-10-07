package com.payflow.authservice.repository;

import com.payflow.authservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByUserName(String userName);

    @Query("SELECT u FROM User u WHERE u.userName = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);


    /**
     * Users with auto-generated username who need a reminder
     * Account must be at least createdBefore old.
     * Last Remainder must be older than 'reminderCuttOff'(or never sent)
     * At most 'MaxReminders' totals
     */

    @Query("""
    SELECT u
    FROM User u
    WHERE u.userNameChangedAt IS NULL
      AND u.userStatus = 'ACTIVE'
      AND u.createdAt < :createdBefore
      AND (
          u.lastUserNameReminderAt IS NULL
          OR u.lastUserNameReminderAt < :reminderCutoff
      )
      AND u.userNameReminderCount < :maxReminders
    """)
    List<User> findUsersNeedingUsernameReminder(
            @Param("createdBefore") Instant createdBefore,
            @Param("reminderCutoff") Instant reminderCutoff,
            @Param("maxReminders") Integer maxReminders
    );

}
