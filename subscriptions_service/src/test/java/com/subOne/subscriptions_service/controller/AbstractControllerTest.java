package com.subOne.subscriptions_service.controller;

import com.subOne.subscriptions_service.BaseIntegrationTest;
import com.subOne.subscriptions_service.kafka.producer.KafkaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;

import java.util.UUID;

public abstract class AbstractControllerTest extends BaseIntegrationTest {

    protected Jwt jwt;

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    private KafkaServiceImpl kafkaService;

    @BeforeEach
    void setUp(){
        UUID userId = UUID.randomUUID();
        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();
        webTestClient = webTestClient.mutateWith(mockJwt().jwt(jwt));
        lenient().when(kafkaService.sendToTopic(anyString(), any())).thenReturn(Mono.empty());
    }
}
