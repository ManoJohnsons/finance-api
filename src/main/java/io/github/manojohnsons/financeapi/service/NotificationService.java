package io.github.manojohnsons.financeapi.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.github.manojohnsons.financeapi.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final UserRepository userRepository;
    private final DashboardService dashboardService;
    private final EmailService emailService;

    @Scheduled(cron = "${api.scheduling.monthly-summary.cron}")
    public void sendMonthlySummary() {
        log.info("Starting scheduled task: Sending monthly summaries...");
        var users = userRepository.findAll();

        if (users.isEmpty()) {
            log.info("No users found. Task completed with no submissions.");
            return;
        }

        var lastMonth = LocalDate.now().minusMonths(1);
        int failureCount = 0;

        log.info("Found {} users to process.", users.size());

        for (var user : users) {
            try {
                log.debug("Processing user ID: {}", user.getId());
                var summary = dashboardService.generateMonthlySummary(
                        user.getId(),
                        lastMonth.getYear(),
                        lastMonth.getMonthValue());
                emailService.sendSummaryEmail(user, summary);
                log.debug("Summary e-mail successfully sen to user ID: {}", user.getId());
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to process summary/email for user ID {}: {}", user.getId(), e.getMessage(), e);
            }
        }

        log.info("Summary submission task completed. Failures: {}", failureCount);
    }
}
