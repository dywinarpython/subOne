package com.subOne.user_service.controller;

import com.subOne.user_service.config.TestConfig;
import com.subOne.user_service.config.TestContainerConfig;
import com.subOne.user_service.config.TestSecurityConfig;
import com.subOne.user_service.dto.response.UserResponseDto;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.mapper.MapperUser;
import com.subOne.user_service.repository.UserRepository;
import com.subOne.user_service.service_impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureWebTestClient
@Import({TestConfig.class, TestContainerConfig.class, TestSecurityConfig.class})
@Slf4j
public class UserControllerTest {

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MapperUser mapperUser;

    private Jwt jwt;

    private User user;


    @BeforeEach
    void setUp(){
        UUID userId = UUID.randomUUID();

        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();

        user = new User();
        user.setUserId(userId);
        user.setEmail("testEmail@mail.com");
        user.setName("test");
        user.setSurname("testSurname");
        user.setVerifyEmail(false);
        user = userRepository.save(user).block();
    }

    @Test
    @DisplayName("ПРОВЕРКА GET -> /api/v1/user")
    void getUserById(){
        Flux<UserResponseDto> result = webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri("/api/v1/user")
                .exchange()
                .expectStatus().isOk().returnResult(UserResponseDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(userResponseDto -> {
                    assertEquals(userResponseDto.email(), user.getEmail());
                    log.info("Email is correct");
                    assertEquals(userResponseDto.name(), user.getName());
                    log.info("Name is correct");
                    assertEquals(userResponseDto.surname(), user.getSurname());
                    log.info("Surname is correct");
                    assertEquals(userResponseDto.userId(), user.getUserId());
                    log.info("UserId is correct");
                })
                .expectComplete()
                .verify();
    }
}
