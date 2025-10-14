package com.subOne.user_service.controller;

import com.subOne.user_service.dto.user.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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



    private static void checkUser(ResponseUserDto usersResponseDto, User user){
        assertEquals(usersResponseDto.email(), user.getEmail());
        assertEquals(usersResponseDto.name(), user.getName());
        assertEquals(usersResponseDto.surname(), user.getSurname());
        assertEquals(usersResponseDto.userId(), user.getUserId());
    }



    @Test
    @DisplayName("ПРОВЕРКА GET -> /api/v1/users/me")
    void getUserById_UserISCreate_ReturnCorrect(){
        Flux<ResponseUserDto> result = webTestClient
                .get()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isOk().returnResult(ResponseUserDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(userResponseDto -> checkUser(userResponseDto, user))
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
