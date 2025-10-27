package com.subOne.notifications_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;
import com.subOne.notifications_service.client.serviceImpl.RestTemplateServiceImpl;
import com.subOne.notifications_service.config.TestConfig;
import com.subOne.notifications_service.config.TestContainerConfig;
import com.subOne.notifications_service.config.TestSecurityConfig;
import com.subOne.notifications_service.dto.notification.request.RequestUpdateNotificationsDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsCountDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsDto;
import com.subOne.notifications_service.entity.Notification;
import com.subOne.notifications_service.mapper.MapperNotification;
import com.subOne.notifications_service.repository.NotificationRepository;
import com.subOne.notifications_service.websocket.serviceImpl.WebSocketSendMessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(value = {TestConfig.class, TestContainerConfig.class, TestSecurityConfig.class})
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureMockMvc
class NotificationsControllerTest {

    @MockitoBean
    private RestTemplateServiceImpl restTemplateService;

    @MockitoBean
    private WebSocketSendMessageServiceImpl webSocketSendMessageService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MapperNotification mapperNotification;


    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private Jwt jwt;

    @Transactional
    private List<Notification> generateNotifications(int size, boolean read){
        List<Notification> notificationList = new ArrayList<>();
        for (int i = 0; i < size + 1; i++) {
            Notification notification = mapperNotification.parametersToNotification(userId,
                    NotificationType.PAYEMNT_SUBSCRIPTION,
                    NotificationTargetType.SUBSCRIPTION,
                    System.currentTimeMillis());
            if (read){
                notification.setRead(true);
            }
            notificationList.add(notification);
        }
        return notificationRepository.saveAll(notificationList);
    }
    private void checkNotification(ResponseNotificationDto responseNotificationDto){
        Optional<Notification> notificationOptional = notificationRepository.findById(responseNotificationDto.id());
        assertTrue(notificationOptional.isPresent());
        Notification notification = notificationOptional.get();
        assertEquals(notification.getNotificationType(), responseNotificationDto.notificationType());
        assertEquals(notification.getNotificationTargetType(), responseNotificationDto.notificationTargetType());
        assertEquals(notification.getCreatedAt(), responseNotificationDto.createdAt());
        assertEquals(notification.getTargetId(), responseNotificationDto.targetId());
    }

    @BeforeEach
    void setUp(){
        userId = UUID.randomUUID();
        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

    }

	@Test
    @DisplayName("GET -> /api/v1/notifications/new?page=")
	void getNewNotifications_FoundNotificationsNew_CorrectReturn() throws Exception {
        generateNotifications(5, false);

        ResponseNotificationsDto responseNotificationsDto = objectMapper.readValue(
                mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/notifications/new?page=" + 0)
                .with(jwt().jwt(jwt))
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
                ResponseNotificationsDto.class);

        assertTrue(responseNotificationsDto.notifications().size() >= 5);
        responseNotificationsDto.notifications().forEach(this::checkNotification);
    }

    @Test
    @DisplayName("GET -> /api/v1/notifications/old?page=")
    void getOldNotifications_FoundNotificationsOld_CorrectReturn() throws Exception {
        generateNotifications(5, true);

        ResponseNotificationsDto responseNotificationsDto = objectMapper.readValue(
                mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/notifications/old?page=" + 0)
                        .with(jwt().jwt(jwt))
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
                ResponseNotificationsDto.class);

        assertTrue(responseNotificationsDto.notifications().size() >= 5);
        responseNotificationsDto.notifications().forEach(this::checkNotification);
    }

    @Test
    @DisplayName("GET -> /api/v1/notifications/count")
    void getCountNotificationsNew_FoundNotificationsNew_CorrectReturn() throws Exception {
        generateNotifications(5, false);

        ResponseNotificationsCountDto responseNotificationsCountDto = objectMapper.readValue(
                mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/notifications/count")
                        .with(jwt().jwt(jwt))
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
                ResponseNotificationsCountDto.class);

        assertTrue(responseNotificationsCountDto.count() >= 5);
    }

    @Test
    @DisplayName("PATCH -> /api/v1/notifications")
    void updateNotifications_FoundNotificationsNew_CorrectUpdateAndCheckRepo() throws Exception {
        List<Notification> notificationList = generateNotifications(5, false);
        RequestUpdateNotificationsDto requestUpdateNotificationsDto = new RequestUpdateNotificationsDto(
                notificationList.stream().map(Notification::getId).toList()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestUpdateNotificationsDto))
                .with(jwt().jwt(jwt))
                ).andExpect(status().isOk());

        notificationRepository.findAllById(requestUpdateNotificationsDto.ids())
                .forEach(notification -> assertTrue(notification.getRead()));
    }

    @Test
    @DisplayName("PATCH -> /api/v1/notifications (not valid request (min))")
    void updateNotifications_NotValidRequestUpdateNotificationsDtoMinElement_NotCorrectUpdateAndCheckRepo() throws Exception {
        List<Notification> notificationList = generateNotifications(5, false);
        RequestUpdateNotificationsDto requestUpdateNotificationsDto = new RequestUpdateNotificationsDto(
                List.of()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestUpdateNotificationsDto))
                .with(jwt().jwt(jwt))
        ).andExpect(status().is4xxClientError());

        notificationRepository.findAllById(requestUpdateNotificationsDto.ids())
                .forEach(notification -> assertFalse(notification.getRead()));
    }

    @Test
    @DisplayName("PATCH -> /api/v1/notifications (not valid request (max))")
    void updateNotifications_NotValidRequestUpdateNotificationsDtoMaxElement_NotCorrectUpdateAndCheckRepo() throws Exception {
        List<Notification> notificationList = generateNotifications(11, false);
        RequestUpdateNotificationsDto requestUpdateNotificationsDto = new RequestUpdateNotificationsDto(
                notificationList.stream().map(Notification::getId).toList()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestUpdateNotificationsDto))
                .with(jwt().jwt(jwt))
        ).andExpect(status().is4xxClientError());

        notificationRepository.findAllById(requestUpdateNotificationsDto.ids())
                .forEach(notification -> assertFalse(notification.getRead()));
    }

}
