package com.subOne.notifications_service.repository;

import com.subOne.notifications_service.dto.response.ResponseNotificationDto;
import com.subOne.notifications_service.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<ResponseNotificationDto> findByUserIdOrderByCreatedAt(UUID userId, Pageable pageable);
}
