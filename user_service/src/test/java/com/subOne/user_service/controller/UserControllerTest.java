package com.subOne.user_service.controller;

import com.subOne.user_service.dto.user.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.user.response.UserResponseDto;
import com.subOne.user_service.dto.user.response.UsersResponseDto;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.mapper.MapperUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

public class UserControllerTest extends AbstractControllerTest{

    @Autowired
    private MapperUser mapperUser;


    private static void checkUser(UserResponseDto usersResponseDto, User user){
        assertEquals(usersResponseDto.email(), user.getEmail());
        assertEquals(usersResponseDto.name(), user.getName());
        assertEquals(usersResponseDto.surname(), user.getSurname());
        assertEquals(usersResponseDto.userId(), user.getUserId());
    }



    @Test
    @DisplayName("ПРОВЕРКА GET -> /api/v1/users/me")
    void getUserById_UserISCreate_ReturnCorrect(){
        Flux<UserResponseDto> result = webTestClient
                .get()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isOk().returnResult(UserResponseDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(userResponseDto -> checkUser(userResponseDto, user))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("ПРОВЕРКА GET -> /api/v1/users?usersId=")
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
                .uri("/api/v1/users?usersId=" + user.getUserId() + "," + UUID.randomUUID() + "," + user2.getUserId())
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
    @DisplayName("ПРОВЕКРА PATCH -> /api/v1/users/me userIsFound")
    void updateUser_UserIsCreate_CorrectUpdate(){
        RequestUpdateUserDto requestUpdateUserDto = new RequestUpdateUserDto("user", "surname");
        webTestClient
                .patch()
                .uri("/api/v1/users/me")
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
    @DisplayName("ПРОВЕКРА PATCH -> /api/v1/users/me userIsFoundAndRequestIsUnCorrect")
    void updateUser_UserIsFoundAndRequestISUnCorrect_CorrectUpdate(){
        RequestUpdateUserDto requestUpdateUserDto = new RequestUpdateUserDto(null, null);
        webTestClient
                .patch()
                .uri("/api/v1/users/me")
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
    @DisplayName("ПРОВЕРКА DELETE /api/v1/users/me")
    void deleteUser_UserIsCorrect_CorrectDelete(){

        when(kafkaService.sendToTopic(anyString(), anyString())).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isNoContent();


        StepVerifier.create(userRepository.findByUserId(UUID.fromString(jwt.getSubject())))
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }


    @Test
    @DisplayName("ПРОВЕРКА DELETE /api/v1/users/me userIsNotFound")
    void deleteUser_UserIsNotFound_UnCorrectDelete(){

        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID())
                .build();

        when(kafkaService.sendToTopic(anyString(), anyString())).thenReturn(Mono.empty());

        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .delete()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isNotFound();


        StepVerifier.create(userRepository.findByUserId(UUID.fromString(jwt.getSubject())))
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

}
