package com.subOne.notifications_service.kafka;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.kafka_dto.SendNotificationDto;
import com.subOne.keycloak_dto.UserInfo;
import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;
import com.subOne.notifications_service.BaseIntegrationTest;
import com.subOne.notifications_service.client.serviceImpl.RestTemplateServiceImpl;
import com.subOne.notifications_service.kafka.kafka_handler.KafkaHandlerService;
import com.subOne.notifications_service.repository.NotificationRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class KafkaHandlerServiceTest extends BaseIntegrationTest {

    @MockitoBean
    private RestTemplateServiceImpl restTemplateService;

    @Autowired
    private KafkaHandlerService kafkaHandlerService;

    @Autowired
    private NotificationRepository notificationRepository;


    @Test
    void saveNotification_CorrectConsumerRecord_CorrectSaveAndCheckRepo(){
        SendNotificationDto sendNotificationDto = new SendNotificationDto(NotificationType.PAYEMNT_SUBSCRIPTION, NotificationTargetType.SUBSCRIPTION, Math.abs( (int) System.currentTimeMillis()));
        ConsumerRecord<UUID, SendNotificationDto> consumerRecord = new ConsumerRecord<>("notification_user", 0, 0L, UUID.randomUUID(), sendNotificationDto);

        kafkaHandlerService.saveNotification(consumerRecord);

        long countFound = notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(consumerRecord.key(), PageRequest.of(0, 1000))
                .stream().filter(notification ->
                        notification.notificationType().equals(sendNotificationDto.notificationType()) &&
                        notification.notificationTargetType().equals(sendNotificationDto.notificationTargetType()) &&
                        notification.targetId().equals(sendNotificationDto.targetId())
                ).count();
        assertTrue(countFound >= 1);
    }
    @Test
    void saveNotificationCreateUser_CorrectUserInfo_CorrectSaveAndCheckRepo(){
        UserInfo userInfo = new UserInfo("name", "surname", UUID.randomUUID(), "email", true);

        kafkaHandlerService.saveNotificationCreateUser(userInfo);

        long countFound = notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userInfo.userId(), PageRequest.of(0, 1000))
                .stream().filter(notification ->
                        notification.notificationType().equals(NotificationType.CREATE_USER) &&
                                notification.notificationTargetType() == null &&
                                notification.targetId() == null
                ).count();
        assertEquals(1, countFound);
    }
    @Test
    void saveNotificationPaymentSubscription_CorrectKafkaDtoPaymentSubscription_CorrectSaveAndCheckRepo(){
        UUID userId = UUID.randomUUID();
        when(restTemplateService.getOwnerIdByGroupId(anyInt())).thenReturn(userId);
        KafkaDtoPaymentSubscription kafkaDtoPaymentSubscription = new KafkaDtoPaymentSubscription(1, 1, NotificationType.PAYEMNT_SUBSCRIPTION);

        kafkaHandlerService.saveNotificationPaymentSubscription(kafkaDtoPaymentSubscription);

        long countFound = notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, PageRequest.of(0, 1000))
                .stream().filter(notification ->
                        notification.notificationType().equals(kafkaDtoPaymentSubscription.notificationType()) &&
                                notification.notificationTargetType().equals(NotificationTargetType.SUBSCRIPTION) &&
                                notification.targetId().equals(kafkaDtoPaymentSubscription.subscriptionId())
                ).count();
        assertEquals(1, countFound);
    }
}
