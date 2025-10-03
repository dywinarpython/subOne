package com.subOne.user_service.kafka.kasfka_handler;

import com.subOne.kecyloak_dto.UserInfo;
public interface KafkaHandlerService {
    void saveUserForKeycloak(UserInfo userInfo);
    void addVerifyEmailUser(String email);
}
