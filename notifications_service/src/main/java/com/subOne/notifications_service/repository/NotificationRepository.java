package com.subOne.notifications_service.repository;

import com.subOne.notifications_service.dto.notification.response.ResponseNotificationDto;
import com.subOne.notifications_service.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<ResponseNotificationDto> findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<ResponseNotificationDto> findByUserIdAndReadTrueOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query(value = "select count(*) from notifications n where n.user_id = :userId and n.read = false", nativeQuery = true)
    long countByUserId(UUID userId);

    @Modifying
    @Query(
            value = """
            update notifications
            set read = true
            where user_id = :userId and id in :notificationsId
            """, nativeQuery = true
    )
    int updateReadNotificationsByUserId(List<Long> notificationsId, UUID userId);

    @Modifying
    @Query(value = "delete from notifications where created_at < :threshold", nativeQuery = true)
    int deleteByCreatedAtBefore(OffsetDateTime threshold);

}
