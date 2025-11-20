package com.subOne.user_service.kafka.kasfka_handler_impl;

import com.subOne.keycloak_dto.UserInfo;
import com.subOne.user_service.kafka.kasfka_handler.KafkaHandlerService;
import com.subOne.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaHandlerServiceImpl implements KafkaHandlerService {

    private final UserService userService;


    @Override
    @KafkaListener(topics = "create_user", containerFactory = "userInfoKafkaListenerFactory")
    public void saveUserForKeycloak(UserInfo userInfo) {
        userService.saveUser(userInfo).doOnError(e -> log.error("Error saving user: userId={}, error={}", userInfo == null ? null : userInfo.userId(), e.getMessage(), e))
                .subscribe();
    }

    @Override
    @KafkaListener(topics = "verify_email", concurrency = "3")
    public void addVerifyEmailUser(String email) {
        userService.addVerifyEmailUser(email).doOnError(e -> log.error("Mail verification error: email={}, error={}", email, e.getMessage(), e))
                .subscribe();
    }
}
