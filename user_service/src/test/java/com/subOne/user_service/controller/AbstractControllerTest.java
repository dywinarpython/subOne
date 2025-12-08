package com.subOne.user_service.controller;

import com.subOne.user_service.BaseIntegrationTest;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.kafka.serviceProducer.KafkaServiceImpl;
import com.subOne.user_service.repository.user_repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

public abstract class AbstractControllerTest extends BaseIntegrationTest {

    protected Jwt jwt;

    protected User user;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected KafkaServiceImpl kafkaService;

    @BeforeEach
    void setUp(){
        lenient().when(kafkaService.sendToTopic(anyString(), anyString())).thenReturn(Mono.empty());
        lenient().when(kafkaService.sendToTopic(anyString(), anyInt())).thenReturn(Mono.empty());
        lenient().when(kafkaService.sendToTopic(anyString(), any(), any())).thenReturn(Mono.empty());

        UUID userId = UUID.randomUUID();
        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        user = new User();
        user.setUserId(userId);
        user.setEmail("testEmail" + System.currentTimeMillis() +  "@mail.com");
        user.setName("test");
        user.setSurname("testSurname");
        user.setVerifyEmail(true);

        StepVerifier.create(userRepository.save(user))
                .assertNext(savedUser -> this.user = savedUser)
                .verifyComplete();

        webTestClient = webTestClient.mutateWith(mockJwt().jwt(jwt));
    }
}
