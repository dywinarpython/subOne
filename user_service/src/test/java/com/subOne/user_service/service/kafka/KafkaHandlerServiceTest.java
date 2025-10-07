package com.subOne.user_service.service.kafka;

import com.subOne.user_service.kafka.kasfka_handler_impl.KafkaHandlerServiceImpl;
import com.subOne.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class KafkaHandlerServiceTest {

    @InjectMocks
    private KafkaHandlerServiceImpl kafkaHandlerService;

    @Mock
    private UserService userService;

    @Test
    void saveUserForKeycloak_UserInfoIsCorrect_SaveUser() {
        when(userService.saveUser(any())).thenReturn(Mono.empty());

        kafkaHandlerService.saveUserForKeycloak(any());

        verify(userService).saveUser(any());
    }


    @Test
    void addVerifyEmailUser_EmailIsCorrect_UpdateEmail() {
        when(userService.addVerifyEmailUser(any())).thenReturn(Mono.empty());

        kafkaHandlerService.addVerifyEmailUser(anyString());

        verify(userService).addVerifyEmailUser(any());
    }
}