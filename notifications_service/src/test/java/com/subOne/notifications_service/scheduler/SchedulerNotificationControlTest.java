package com.subOne.notifications_service.scheduler;

import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;
import com.subOne.notifications_service.BaseIntegrationTest;
import com.subOne.notifications_service.client.serviceImpl.RestTemplateServiceImpl;
import com.subOne.notifications_service.entity.Notification;
import com.subOne.notifications_service.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchedulerNotificationControlTest extends BaseIntegrationTest {

    @MockitoBean
    private RestTemplateServiceImpl restTemplateService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SchedulerNotificationControl schedulerNotificationControl;


    @Transactional
    private List<Notification> generateNotifications(int size, boolean old){
        List<Notification> notificationList = new ArrayList<>();
        for (int i = 0; i < size + 1; i++) {
            Notification notification = new Notification();
            notification.setNotificationType(NotificationType.PAYEMNT_SUBSCRIPTION);
            notification.setNotificationTargetType(NotificationTargetType.SUBSCRIPTION);
            notification.setUserId(UUID.randomUUID());
            notification.setTargetId(1L);
            if(old){
                notification.setCreatedAt(OffsetDateTime.now().minusDays(7));
            } else {
                notification.setCreatedAt(OffsetDateTime.now());
            }
            notification.setRead(true);
            notificationList.add(notification);
        }
        return notificationRepository.saveAll(notificationList);
    }

    @Test
    void deleteOldNotifications_FoundOldNotifications_CorrectDelete(){
         List<Notification> notificationList = generateNotifications(5, true);

         schedulerNotificationControl.deleteOldNotifications();

         notificationList.forEach(notification -> assertTrue(notificationRepository.findById(notification.getId()).isEmpty()));
    }

    @Test
    void deleteOldNotifications_FoundNewNotifications_CorrectNotDelete(){
        List<Notification> notificationList = generateNotifications(5, false);

        schedulerNotificationControl.deleteOldNotifications();

        notificationList.forEach(notification -> assertTrue(notificationRepository.findById(notification.getId()).isPresent()));
    }
}
