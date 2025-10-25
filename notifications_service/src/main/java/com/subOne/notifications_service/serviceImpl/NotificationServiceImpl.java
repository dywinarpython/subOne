package com.subOne.notifications_service.serviceImpl;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.keycloak_dto.UserInfo;
import com.subOne.notifications_service.client.service.RestTemplateService;
import com.subOne.notifications_service.dto.notification.request.RequestUpdateNotificationsDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsCountDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsDto;
import com.subOne.notifications_service.entity.Notification;
import com.subOne.notifications_service.mapper.MapperNotification;
import com.subOne.notifications_service.repository.NotificationRepository;
import com.subOne.notifications_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final MapperNotification mapperNotification;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RestTemplateService restTemplateService;
    private final Integer pageSize;

    public NotificationServiceImpl(NotificationRepository notificationRepository, MapperNotification mapperNotification,
                                   SimpMessagingTemplate simpMessagingTemplate, RestTemplateService restTemplateService, @Value("${spring.page.size}") Integer pageSize) {
        this.notificationRepository = notificationRepository;
        this.mapperNotification = mapperNotification;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.restTemplateService = restTemplateService;
        this.pageSize = pageSize;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseNotificationsDto findNotReadNotificationsByUserId(Jwt jwt, Integer page) {
        return new ResponseNotificationsDto(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID.fromString(jwt.getSubject()), PageRequest.of(page, pageSize)));
    }

    @Override
    public ResponseNotificationsDto findReadNotificationsByUserId(Jwt jwt, Integer page) {
        return new ResponseNotificationsDto(notificationRepository.findByUserIdAndReadTrueOrderByCreatedAtDesc(UUID.fromString(jwt.getSubject()), PageRequest.of(page, pageSize)));
    }

    @Override
    public ResponseNotificationsCountDto findCountNotReadNotifications(Jwt jwt) {
        return new ResponseNotificationsCountDto(notificationRepository.countByUserId(UUID.fromString(jwt.getSubject())));
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

    @Override
    public void saveNotificationPaymentSubscription(KafkaDtoPaymentSubscription kafkaDtoPaymentSubscription) {
            UUID ownerId = restTemplateService.getOwnerIdByGroupId(kafkaDtoPaymentSubscription.groupId());
            if(ownerId == null) return;
            String message = "Завтра произойдет оплата подписки: " + kafkaDtoPaymentSubscription.subscriptionId();
            Notification notification = notificationRepository.save(mapperNotification.parametersToNotification(message, ownerId));
            simpMessagingTemplate.convertAndSendToUser(
                    notification.getUserId().toString(),
                    "/queue/notifications",
                    notification.getMessage());
    }

    @Override
    @Transactional
    public void readNotification(RequestUpdateNotificationsDto requestUpdateNotificationsDto, Jwt jwt) {
        int count = notificationRepository.updateReadNotificationsByUserId(requestUpdateNotificationsDto.ids(), UUID.fromString(jwt.getSubject()));
        if(count != requestUpdateNotificationsDto.ids().size()){
            log.warn("Some notifications is not update");
        }
    }
}
