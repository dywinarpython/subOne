package com.subOne.notifications_service.serviceImpl;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.kafka_dto.SendNotificationDto;
import com.subOne.keycloak_dto.UserInfo;
import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;
import com.subOne.notifications_service.client.service.RestTemplateService;
import com.subOne.notifications_service.dto.notification.request.RequestUpdateNotificationsDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsCountDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsDto;
import com.subOne.notifications_service.entity.Notification;
import com.subOne.notifications_service.mapper.MapperNotification;
import com.subOne.notifications_service.repository.NotificationRepository;
import com.subOne.notifications_service.service.NotificationService;
import com.subOne.notifications_service.websocket.service.WebSocketSendMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final MapperNotification mapperNotification;
    private final WebSocketSendMessageService webSocketSendMessageService;
    private final RestTemplateService restTemplateService;
    private final Integer pageSize;

    public NotificationServiceImpl(NotificationRepository notificationRepository, MapperNotification mapperNotification,
                                   WebSocketSendMessageService webSocketSendMessageService, RestTemplateService restTemplateService, @Value("${spring.page.size}") Integer pageSize) {
        this.notificationRepository = notificationRepository;
        this.mapperNotification = mapperNotification;
        this.webSocketSendMessageService = webSocketSendMessageService;
        this.restTemplateService = restTemplateService;
        this.pageSize = pageSize;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseNotificationsDto findNotReadNotificationsByUserId(Jwt jwt, Integer page) {
        return new ResponseNotificationsDto(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID.fromString(jwt.getSubject()), PageRequest.of(page, pageSize)));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseNotificationsDto findReadNotificationsByUserId(Jwt jwt, Integer page) {
        return new ResponseNotificationsDto(notificationRepository.findByUserIdAndReadTrueOrderByCreatedAtDesc(UUID.fromString(jwt.getSubject()), PageRequest.of(page, pageSize)));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseNotificationsCountDto findCountNotReadNotifications(Jwt jwt) {
        return new ResponseNotificationsCountDto(notificationRepository.countByUserId(UUID.fromString(jwt.getSubject())));
    }

    @Override
    @Transactional
    public void saveNotification(ConsumerRecord<UUID, SendNotificationDto> record) {
        Notification notification = notificationRepository.save(mapperNotification.messageDtoToNotification(record));
        webSocketSendMessageService.sendMessage(
                notification.getUserId().toString(),
                "/queue/notifications",
                record.value()
        );
    }

    @Override
    @Transactional
    public void saveNotificationCreateUser(UserInfo userInfo) {
        notificationRepository.save(
                mapperNotification.parametersToNotification(userInfo.userId(), NotificationType.CREATE_USER, null, null)
        );
    }

    @Override
    @Transactional
    public void saveNotificationPaymentSubscription(KafkaDtoPaymentSubscription kafkaDtoPaymentSubscription) {
            UUID ownerId = restTemplateService.getOwnerIdByGroupId(kafkaDtoPaymentSubscription.groupId());
            if(ownerId == null) return;
            Notification notification = notificationRepository.save(
                    mapperNotification.parametersToNotification(ownerId, kafkaDtoPaymentSubscription.notificationType(), NotificationTargetType.SUBSCRIPTION, kafkaDtoPaymentSubscription.subscriptionId())
            );
            webSocketSendMessageService.sendMessage(
                    notification.getUserId().toString(),
                    "/queue/notifications",
                    new SendNotificationDto(notification.getNotificationType(), notification.getNotificationTargetType(), notification.getTargetId()));
    }

    @Override
    @Transactional
    public void readNotification(RequestUpdateNotificationsDto requestUpdateNotificationsDto, Jwt jwt) {
        int count = notificationRepository.updateReadNotificationsByUserId(requestUpdateNotificationsDto.ids(), UUID.fromString(jwt.getSubject()));
        if(count != requestUpdateNotificationsDto.ids().size()){
            throw new NoSuchElementException("Some notifications is not found");
        }
    }
}
