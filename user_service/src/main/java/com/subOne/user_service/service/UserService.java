package com.subOne.user_service.service;

import com.subOne.kecyloak_dto.UserInfo;
import com.subOne.user_service.dto.response.UserResponseDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {
    Mono<Void> saveUser(UserInfo userInfo);
    Mono<Void> addVerifyEmailUser(String email);
    Mono<UserResponseDto> getUserById(UUID id);
}
