package com.subOne.notifications_service.service;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.kafka_dto.SendNotificationDto;
import com.subOne.keycloak_dto.UserInfo;
import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;
import com.subOne.notifications_service.client.service.RestTemplateService;
import com.subOne.notifications_service.dto.notification.request.RequestUpdateNotificationsDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsCountDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsDto;
import com.subOne.notifications_service.entity.Notification;
import com.subOne.notifications_service.mapper.MapperNotification;
import com.subOne.notifications_service.repository.NotificationRepository;
import com.subOne.notifications_service.serviceImpl.NotificationServiceImpl;
import com.subOne.notifications_service.websocket.service.WebSocketSendMessageService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private WebSocketSendMessageService webSocketSendMessageService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RestTemplateService restTemplateService;

    @Mock
    private MapperNotification mapperNotification;

    @Mock
    private Jwt jwt;

    @Mock
    private ConsumerRecord<UUID, SendNotificationDto> consumerRecord;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp(){
        notificationService = new NotificationServiceImpl(notificationRepository, mapperNotification, webSocketSendMessageService, restTemplateService, 10);
        lenient().when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
    }

    @Test
    void findNotReadNotificationsByUserId_NotReadNotificationFound_CorrectReturnAndCheckRepo(){
        List<ResponseNotificationDto> responseNotificationDtoList = List.of(
                new ResponseNotificationDto(1, NotificationTargetType.GROUP, NotificationType.CHANGE_OWNER, 1, OffsetDateTime.now()),
                new ResponseNotificationDto(2, NotificationTargetType.SUBSCRIPTION, NotificationType.PAYEMNT_SUBSCRIPTION, 2, OffsetDateTime.now()),
                new ResponseNotificationDto(3, null, NotificationType.ADD_MEMBER, 3, OffsetDateTime.now())
        );
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(any(), any())).thenReturn(responseNotificationDtoList);


        ResponseNotificationsDto responseNotificationsDto = notificationService.findNotReadNotificationsByUserId(jwt, 0);

        assertEquals(responseNotificationsDto, new ResponseNotificationsDto(responseNotificationDtoList));

        verify(notificationRepository).findByUserIdAndReadFalseOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void findReadNotificationsByUserId_ReadNotificationFound_CorrectReturnAndCheckRepo(){
        List<ResponseNotificationDto> responseNotificationDtoList = List.of(
                new ResponseNotificationDto(1, NotificationTargetType.GROUP, NotificationType.CHANGE_OWNER, 1, OffsetDateTime.now()),
                new ResponseNotificationDto(2, NotificationTargetType.SUBSCRIPTION, NotificationType.PAYEMNT_SUBSCRIPTION, 2, OffsetDateTime.now()),
                new ResponseNotificationDto(3, null, NotificationType.ADD_MEMBER, 3, OffsetDateTime.now())
        );
        when(notificationRepository.findByUserIdAndReadTrueOrderByCreatedAtDesc(any(), any())).thenReturn(responseNotificationDtoList);


        ResponseNotificationsDto result = notificationService.findReadNotificationsByUserId(jwt, 0);

        assertEquals(result, new ResponseNotificationsDto(responseNotificationDtoList));

        verify(notificationRepository).findByUserIdAndReadTrueOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void findCountNotReadNotifications_NotReadNotificationFound_CorrectReturnAndCheckRepo(){
        ResponseNotificationsCountDto responseNotificationsCountDto = new ResponseNotificationsCountDto(10);
        when(notificationRepository.countByUserId(any())).thenReturn(responseNotificationsCountDto.count());

        ResponseNotificationsCountDto result = notificationService.findCountNotReadNotifications(jwt);

        assertEquals(result, responseNotificationsCountDto);

        verify(notificationRepository).countByUserId(any());
    }

    @Test
    void saveNotification_CorrectConsumerInfo_CorrectSaveAndCheckRepoAndWebSocket(){
        Notification notification = new Notification();
        notification.setUserId(UUID.randomUUID());
        when(notificationRepository.save(any())).thenReturn(notification);
        when(consumerRecord.value()).thenReturn(new SendNotificationDto(NotificationType.ADD_MEMBER, NotificationTargetType.GROUP, 1));
        when(mapperNotification.messageDtoToNotification(any())).thenReturn(notification);

        notificationService.saveNotification(consumerRecord);

        verify(notificationRepository).save(any());
        verify(webSocketSendMessageService).sendMessage(anyString(), anyString(), any());
    }

    @Test
    void saveNotificationCreateUser_CorrectUserInfo_CorrectSaveAndCheckRepo(){
        Notification notification = new Notification();
        notification.setNotificationType(NotificationType.CREATE_USER);
        UserInfo userInfo = new UserInfo("name", "surname", UUID.randomUUID(), "email", true);
        when(mapperNotification.parametersToNotification(any(), any(), any(), any())).thenReturn(notification);
        when(notificationRepository.save(any())).thenReturn(notification);

        notificationService.saveNotificationCreateUser(userInfo);

        verify(notificationRepository).save(any());
    }

    @Test
    void saveNotificationPaymentSubscription_OwnerFound_CorrectSaveAndCheckRepoAndWebsocket(){
        KafkaDtoPaymentSubscription kafkaDtoPaymentSubscription = new KafkaDtoPaymentSubscription(1, 1, NotificationType.PAYEMNT_SUBSCRIPTION);
        Notification notification = new Notification();
        notification.setUserId(UUID.randomUUID());
        notification.setNotificationType(kafkaDtoPaymentSubscription.notificationType());
        notification.setNotificationTargetType(NotificationTargetType.SUBSCRIPTION);
        notification.setTargetId(kafkaDtoPaymentSubscription.subscriptionId());
        when(restTemplateService.getOwnerIdByGroupId(anyInt())).thenReturn(UUID.randomUUID());
        when(notificationRepository.save(any())).thenReturn(notification);

        notificationService.saveNotificationPaymentSubscription(kafkaDtoPaymentSubscription);

        verify(restTemplateService).getOwnerIdByGroupId(anyInt());
        verify(notificationRepository).save(any());
        verify(webSocketSendMessageService).sendMessage(anyString(), anyString(), any());
    }

    @Test
    void readNotification_FoundNotifications_CorrectUpdateAndCheckRepo() {
        RequestUpdateNotificationsDto requestUpdateNotificationsDto = new RequestUpdateNotificationsDto(
                List.of(
                      1, 2, 3, 4, 5
                )
        );
        when(notificationRepository.updateReadNotificationsByUserId(anyList(), any())).thenReturn(5);

        notificationService.readNotification(requestUpdateNotificationsDto, jwt);

        verify(notificationRepository).updateReadNotificationsByUserId(anyList(), any());
    }


}
