package com.subOne.subscriptions_service.controller;

import com.subOne.subscriptions_service.client.WebClientService;
import com.subOne.subscriptions_service.config.TestConfig;
import com.subOne.subscriptions_service.config.TestContainerConfig;
import com.subOne.subscriptions_service.config.TestSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.webservices.client.AutoConfigureMockWebServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class, RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@AutoConfigureWebTestClient
@AutoConfigureMockWebServiceServer
@Import({TestConfig.class, TestContainerConfig.class, TestSecurityConfig.class})
@Slf4j
public abstract class AbstractControllerTest {

    protected Jwt jwt;

    @Autowired
    protected WebTestClient webTestClient;

    @BeforeEach
    void setUp(){
        UUID userId = UUID.randomUUID();
        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();
        webTestClient = webTestClient.mutateWith(mockJwt().jwt(jwt));
    }
}
