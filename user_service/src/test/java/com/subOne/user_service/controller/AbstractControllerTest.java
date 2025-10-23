package com.subOne.user_service.controller;

import com.subOne.user_service.config.TestConfig;
import com.subOne.user_service.config.TestContainerConfig;
import com.subOne.user_service.config.TestSecurityConfig;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.kafka.serviceProducer.KafkaServiceImpl;
import com.subOne.user_service.repository.user_repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class, RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@AutoConfigureWebTestClient
@Import({TestConfig.class, TestContainerConfig.class, TestSecurityConfig.class})
@Slf4j
public abstract class AbstractControllerTest {

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
        lenient().when(kafkaService.sendToTopic(anyString(), anyLong())).thenReturn(Mono.empty());

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
