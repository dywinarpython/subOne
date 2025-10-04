package com.subOne.user_service.service_impl;

import com.subOne.kecyloak_dto.UserInfo;
import com.subOne.user_service.dto.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.response.UsersResponseDto;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.mapper.MapperUser;
import com.subOne.user_service.dto.response.UserResponseDto;
import com.subOne.user_service.repository.UserRepository;
import com.subOne.user_service.service.UserService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    private final MapperUser mapperUser;


    @Override
    @Transactional
    public Mono<Void> saveUser(UserInfo userInfo) {
        return userRepository.save(mapperUser.userInfoToUser(userInfo)).then();
    }

    @Override
    @Transactional
    public Mono<Void> addVerifyEmailUser(String email) {
        System.out.println(email);
        return userRepository.updateToVerifyEmail(email).flatMap(count -> {
            if(count != 1) return Mono.error(new NoSuchElementException("Email is not update!"));
            return Mono.empty();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<UserResponseDto> getUserById(UUID id) {
        return userRepository.findByUserId(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("User is not found!")));
    }

    @Override
    public Mono<UsersResponseDto> getUsersById(List<UUID> uuids) {
        if (uuids.size() > 5) {
            return Mono.error(new ValidationException("There can be no more than 5 people in a group!"));
        }
        return userRepository.findByUserIds(uuids)
                .collectList()
                .map(users -> uuids.stream().map(id ->
                        users.stream().filter(u -> u.userId().equals(id)).findFirst().orElse(new UserResponseDto(null, null, null, null))
                ).toList())
                .map(UsersResponseDto::new);
    }

    @Override
    @Transactional
    public Mono<Void> updateUser(Mono<RequestUpdateUserDto> requestUpdateUserDto, Jwt jwt) {
        return requestUpdateUserDto.map(mapperUser::addUpdateField).flatMap(mp ->
                userRepository.updateFields(mp, User.class, "userId", jwt.getSubject())
        );
    }
}
