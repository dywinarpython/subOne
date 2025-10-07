package com.subOne.user_service.controller;

import com.subOne.user_service.config.TestConfig;
import com.subOne.user_service.config.TestContainerConfig;
import com.subOne.user_service.config.TestSecurityConfig;
import com.subOne.user_service.dto.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.response.UserResponseDto;
import com.subOne.user_service.dto.response.UsersResponseDto;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.kafka.serviceProducer.KafkaService;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureWebTestClient
@Import({TestConfig.class, TestContainerConfig.class, TestSecurityConfig.class})
@Slf4j
public class UserControllerTest {

    @MockitoBean
    private KafkaService kafkaService;

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

    private static void checkUser(UserResponseDto usersResponseDto, User user){
        assertEquals(usersResponseDto.email(), user.getEmail());
        assertEquals(usersResponseDto.name(), user.getName());
        assertEquals(usersResponseDto.surname(), user.getSurname());
        assertEquals(usersResponseDto.userId(), user.getUserId());
    }


    @BeforeEach
    void setUp(){
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
        user.setVerifyEmail(false);

        StepVerifier.create(userRepository.save(user))
                .assertNext(savedUser -> this.user = savedUser)
                .verifyComplete();

        webTestClient = webTestClient.mutateWith(mockJwt().jwt(jwt));
    }

    @Test
    @DisplayName("ПРОВЕРКА GET -> /api/v1/user")
    void getUserById_UserISCreate_ReturnCorrect(){
        Flux<UserResponseDto> result = webTestClient
                .get()
                .uri("/api/v1/user")
                .exchange()
                .expectStatus().isOk().returnResult(UserResponseDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(userResponseDto -> checkUser(userResponseDto, user))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("ПРОВЕРКА GET -> /api/v1/user/list?usersId=")
    void getUsersById_UsersISCreateAndOneIsNotFound_ReturnCorrect(){
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        user2.setEmail("testEmail2@mail.com");
        user2.setName("test");
        user2.setSurname("testSurname");
        user2.setVerifyEmail(false);
        userRepository.save(user2).block();

        Flux<UsersResponseDto> result = webTestClient
                .get()
                .uri("/api/v1/user/list?usersId=" + user.getUserId() + "," + UUID.randomUUID() + "," + user2.getUserId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(UsersResponseDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(usersResponseDto -> {
                    assertEquals(3, usersResponseDto.users().size());
                    UserResponseDto user1Dto = usersResponseDto.users().getFirst();
                    checkUser(user1Dto, user);
                    UserResponseDto user2Dto = usersResponseDto.users().get(1);
                    checkUser(user2Dto, new User());
                    UserResponseDto user3Dto = usersResponseDto.users().getLast();
                    checkUser(user3Dto, user2);
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("ПРОВЕКРА PATCH -> /api/v1/user userIsFound")
    void updateUser_UserIsCreate_CorrectUpdate(){
        RequestUpdateUserDto requestUpdateUserDto = new RequestUpdateUserDto("user", "surname");
        webTestClient
                .patch()
                .uri("/api/v1/user")
                .bodyValue(requestUpdateUserDto)
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(userRepository.findByUserId(UUID.fromString(jwt.getSubject())))
                .assertNext(user -> {
                    assertEquals(user.name(), requestUpdateUserDto.name());
                    assertEquals(user.surname(), requestUpdateUserDto.surname());
                }).expectComplete()
                .verify();
    }

    @Test
    @DisplayName("ПРОВЕКРА PATCH -> /api/v1/user userIsFoundAndRequestIsUnCorrect")
    void updateUser_UserIsFoundAndRequestISUnCorrect_CorrectUpdate(){
        RequestUpdateUserDto requestUpdateUserDto = new RequestUpdateUserDto(null, null);
        webTestClient
                .patch()
                .uri("/api/v1/user")
                .bodyValue(requestUpdateUserDto)
                .exchange()
                .expectStatus().is4xxClientError();

        StepVerifier.create(userRepository.findByUserId(UUID.fromString(jwt.getSubject())))
                .assertNext(user -> {
                    assertNotEquals(user.name(), requestUpdateUserDto.name());
                    assertNotEquals(user.surname(), requestUpdateUserDto.surname());
                }).expectComplete()
                .verify();
    }


    @Test
    @DisplayName("ПРОВЕРКА DELETE /api/v1/user")
    void deleteUser_UserIsCorrect_CorrectDelete(){

        when(kafkaService.sendToTopic(anyString(), anyString())).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/api/v1/user")
                .exchange()
                .expectStatus().isNoContent();


        StepVerifier.create(userRepository.findByUserId(UUID.fromString(jwt.getSubject())))
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }


    @Test
    @DisplayName("ПРОВЕРКА DELETE /api/v1/user userIsNotFound")
    void deleteUser_UserIsNotFound_UnCorrectDelete(){

        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID())
                .build();

        when(kafkaService.sendToTopic(anyString(), anyString())).thenReturn(Mono.empty());

        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .delete()
                .uri("/api/v1/user")
                .exchange()
                .expectStatus().isNotFound();


        StepVerifier.create(userRepository.findByUserId(UUID.fromString(jwt.getSubject())))
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

}
