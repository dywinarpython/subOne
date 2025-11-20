package com.subOne.notifications_service.scheduler;

import com.subOne.notifications_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerNotificationControl {

    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteOldNotifications() {
        OffsetDateTime timeDelete = OffsetDateTime.now().minusDays(7);
        Integer count = notificationRepository.deleteByCreatedAtBefore(timeDelete);
        log.info("Successful deletion of old notifications deleted: {}", count);
    }
}
