package com.subOne.user_service.scheduler;

import com.subOne.user_service.repository.GroupInviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteCleanupScheduler {

    private final GroupInviteRepository groupInviteRepository;

    @Scheduled(fixedRate = 60 * 10 * 1000)
    public void cleanExpiredInvites() {
        Instant now = Instant.now();
        groupInviteRepository.deleteAllByExpiresAtBefore(now)
                .doOnSuccess(v -> log.info("Expired invites deleted"))
                .doOnError(throwable -> log.error("Expired invites not deleted", throwable))
                .subscribe();
    }
}
