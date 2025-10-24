package com.subOne.notifications_service.serviceImpl;

import com.subOne.keycloak_dto.UserInfo;
import com.subOne.notifications_service.dto.response.ResponseNotificationsDto;
import com.subOne.notifications_service.entity.Notification;
import com.subOne.notifications_service.mapper.MapperNotification;
import com.subOne.notifications_service.repository.NotificationRepository;
import com.subOne.notifications_service.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final MapperNotification mapperNotification;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Integer pageSize;

    public NotificationServiceImpl(NotificationRepository notificationRepository, MapperNotification mapperNotification,
                                   SimpMessagingTemplate simpMessagingTemplate,  @Value("${spring.page.size}") Integer pageSize) {
        this.notificationRepository = notificationRepository;
        this.mapperNotification = mapperNotification;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.pageSize = pageSize;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseNotificationsDto findByUserId(Jwt jwt, Integer page) {
        return new ResponseNotificationsDto(notificationRepository.findByUserIdOrderByCreatedAtDesc(UUID.fromString(jwt.getSubject()), PageRequest.of(page, pageSize)));
    }

    @Override
    @Transactional
    public void saveNotification(ConsumerRecord<UUID, String> record) {
        Notification notification = notificationRepository.save(mapperNotification.messageDtoToNotification(record));
        simpMessagingTemplate.convertAndSendToUser(
                notification.getUserId().toString(),
                "/queue/notifications",
                notification.getMessage()
        );
    }

    @Override
    @Transactional
    public void saveNotification(UserInfo userInfo) {
        String message = userInfo.name() +
                ", спасибо, за регистрацию, теперь у вас есть множество возможностей анализировать подписки и многое другое.";
        Notification notification = notificationRepository.save(mapperNotification.parametersToNotification(message, userInfo.userId()));
        simpMessagingTemplate.convertAndSendToUser(
                notification.getUserId().toString(),
                "/queue/notifications",
                notification.getMessage());
    }
}
