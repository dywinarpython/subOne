package com.subOne.user_service.service;

import com.subOne.kecyloak_dto.UserInfo;
import com.subOne.user_service.dto.user.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface UserService {
    Mono<Void> saveUser(UserInfo userInfo);
    Mono<Void> addVerifyEmailUser(String email);
    Mono<ResponseUserDto> getUserById(Jwt jwt);
    Mono<Void> updateUser(Mono<RequestUpdateUserDto> requestUpdateUserDto, Jwt jwt);
    Mono<Void> deleteUser(Jwt jwt);
    Mono<Boolean> checkVerifyEmail(UUID userId);
}
