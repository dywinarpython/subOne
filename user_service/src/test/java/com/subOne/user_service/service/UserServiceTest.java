package com.subOne.user_service.service;

import com.subOne.user_service.dto.user.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.kafka.serviceProducer.KafkaService;
import com.subOne.user_service.mapper.MapperUser;
import com.subOne.user_service.repository.UserRepository;
import com.subOne.user_service.service_impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaService kafkaService;

    @Mock
    private Jwt jwt;

    @Mock
    private MapperUser mapperUser;

    @InjectMocks
    private UserServiceImpl userService;

    private static void accept(Throwable throwable) {
        assertEquals(NoSuchElementException.class, throwable.getClass());
        log.info("User is not found -> correct");
    }


    @Test
    void saveUser_UserInfoCorrect_saveToDb(){
        when(userRepository.save(any())).thenReturn(Mono.just(new User()));

        Mono<Void> result = userService.saveUser(any());

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(userRepository).save(any());
    }


    @Test
    void addVerifyEmailUser_emailIsFound_saveToDb(){
        when(userRepository.updateToVerifyEmail(anyString())).thenReturn(Mono.just(1));

        Mono<Void> result = userService.addVerifyEmailUser(anyString());

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(userRepository).updateToVerifyEmail(anyString());
    }

    @Test
    void addVerifyEmailUser_emailIsNotFound_NotSaveToDb(){
        when(userRepository.updateToVerifyEmail(anyString())).thenReturn(Mono.just(0));

        Mono<Void> result = userService.addVerifyEmailUser(anyString());


        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                            assertEquals(NoSuchElementException.class, throwable.getClass());
                            log.info("Email is not found -> correct");
                        }
                        )
                .verify();
        verify(userRepository).updateToVerifyEmail(anyString());
    }

    @Test
    void getUserById_UserIsFound_CorrectReturnAndCheckRepo(){
        ResponseUserDto responseUserDto = new ResponseUserDto(UUID.randomUUID(), "testName", "testSurname", "testEmail");
        when(userRepository.findByUserId(any())).thenReturn(Mono.just(responseUserDto));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());

        Mono<ResponseUserDto> result = userService.getUserById(jwt);

        StepVerifier.create(result)
                .expectNext(responseUserDto)
                .expectComplete()
                .verify();
        verify(userRepository).findByUserId(any());
    }

    @Test
    void getUserById_UserIsNotFound_UnCorrectReturnAndCheckRepo(){
       when(userRepository.findByUserId(any())).thenReturn(Mono.empty());
       when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
       Mono<ResponseUserDto> result = userService.getUserById(jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(UserServiceTest::accept)
                .verify();
        verify(userRepository).findByUserId(any());
    }

    @Test
    void updateUser_UpdateUserDtoIsCorrectAndUserIsFound_CorrectUpdateAndCheckRepo(){
        Mono<RequestUpdateUserDto> requestUpdateUserDtoMono = Mono.just(new RequestUpdateUserDto(null, null));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(mapperUser.addUpdateField(any(RequestUpdateUserDto.class))).thenReturn(Map.of());
        when(userRepository.updateFields(any(), any(), anyString(), any())).thenReturn(Mono.just(1L));

        Mono<Void> result = userService.updateUser(requestUpdateUserDtoMono, jwt);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(userRepository).updateFields(any(), any(), anyString(), any());
    }

    @Test
    void updateUser_UpdateUserDtoIsCorrectAndUserIsNotFound_NotCorrectUpdateAndCheckRepo(){
        Mono<RequestUpdateUserDto> requestUpdateUserDtoMono = Mono.just(new RequestUpdateUserDto(null, null));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(mapperUser.addUpdateField(any(RequestUpdateUserDto.class))).thenReturn(Map.of());
        when(userRepository.updateFields(any(), any(), anyString(), any())).thenReturn(Mono.just(0L));

        Mono<Void> result = userService.updateUser(requestUpdateUserDtoMono, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(UserServiceTest::accept)
                .verify();
        verify(userRepository).updateFields(any(), any(), anyString(), any());
    }

    @Test
    void deleteUser_UserIsFound_deleteFromDbAndCheckRepo(){

        when(userRepository.deleteByUserId(any())).thenReturn(Mono.just(1));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(kafkaService.sendToTopic(any(), any())).thenReturn(Mono.empty());

        Mono<Void> result = userService.deleteUser(jwt);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(userRepository).deleteByUserId(any());
    }

    @Test
    void deleteUser_UserIsNotFound_NotDeleteFromDb(){

        when(userRepository.deleteByUserId(any())).thenReturn(Mono.just(0));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());


        Mono<Void> result = userService.deleteUser(jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertEquals(NoSuchElementException.class, throwable.getClass());
                    log.info("User if not found -> correct");
                })
                .verify();
        verify(userRepository).deleteByUserId(any());
    }
}
