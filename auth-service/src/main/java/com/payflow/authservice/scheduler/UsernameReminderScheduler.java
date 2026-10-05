package com.payflow.authservice.scheduler;

import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.repository.UserRepository;
import com.payflow.authservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsernameReminderScheduler {

    private static final int MAX_REMINDERS = 3;
    private static final long MIN_ACCOUNT_AGE_DAYS = 3;
    private static final long MIN_GAP_DAYS = 7;

    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Runs daily at 9 AM.
     * Sends reminders to users who still have an auto-generated username.
     */
    @Scheduled(cron = "${app.username-reminder.cron:0 0 9 * * *}")
    @Transactional
    public void sendUsernameReminders() {

        Instant createdBefore = Instant.now().minus(MIN_ACCOUNT_AGE_DAYS, ChronoUnit.DAYS);
        Instant reminderCutoff = Instant.now().minus(MIN_GAP_DAYS, ChronoUnit.DAYS);

        List<User> users = userRepository.findUsersNeedingUsernameReminder(
                createdBefore, reminderCutoff, MAX_REMINDERS);

        log.info("Username reminder scheduler: {} users to remind", users.size());

        for (User user : users) {
            try {
                emailService.sendUsernameReminderEmail(user.getEmail(), user.getUserName());
                user.setLastUserNameReminderAt(Instant.now());
                user.setUserNameReminderCount(user.getUserNameReminderCount() + 1);
                userRepository.save(user);
            } catch (Exception e) {
                log.error("Failed to send username reminder to {}: {}",
                        user.getEmail(), e.getMessage());
            }
        }
    }
}