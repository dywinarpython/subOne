package com.subOne.user_service.service_impl;

import com.subOne.kecyloak_dto.UserInfo;
import com.subOne.user_service.mapper.MapperUser;
import com.subOne.user_service.dto.response.UserResponseDto;
import com.subOne.user_service.repository.UserRepository;
import com.subOne.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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
        return userRepository.findUserById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("User is not found!")));
    }
}
