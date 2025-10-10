package com.subOne.user_service.service;

import com.subOne.kecyloak_dto.UserInfo;
import com.subOne.user_service.dto.user.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.user.response.UserResponseDto;
import com.subOne.user_service.dto.user.response.UsersResponseDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface UserService {
    Mono<Void> saveUser(UserInfo userInfo);
    Mono<Void> addVerifyEmailUser(String email);
    Mono<UserResponseDto> getUserById(Jwt jwt);
    Mono<UsersResponseDto> getUsersById(List<UUID> id);
    Mono<Void> updateUser(Mono<RequestUpdateUserDto> requestUpdateUserDto, Jwt jwt);
    Mono<Void> deleteUser(Jwt jwt);
}
