package com.subOne.user_service.service.scheduler;

import com.subOne.user_service.repository.group_invite_repository.GroupInviteRepository;
import com.subOne.user_service.scheduler.InviteCleanupScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InviteCleanupSchedulerTest {

    @Mock
    private GroupInviteRepository groupInviteRepository;

    @InjectMocks
    private InviteCleanupScheduler inviteCleanupScheduler;

    @Test
    void cleanExpiredInvites_CorrectClean() {
        when(groupInviteRepository.deleteAllByExpiresAtBefore(any())).thenReturn(Mono.empty());

        inviteCleanupScheduler.cleanExpiredInvites();

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(groupInviteRepository).deleteAllByExpiresAtBefore(any()));
    }
}
