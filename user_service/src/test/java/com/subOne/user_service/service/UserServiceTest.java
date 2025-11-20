package com.subOne.user_service.service;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.user.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.kafka.serviceProducer.KafkaService;
import com.subOne.user_service.mapper.MapperUser;
import com.subOne.user_service.repository.user_repository.UserRepository;
import com.subOne.user_service.service_impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaService kafkaService;

    @Mock
    private Jwt jwt;

    @Mock
    private MapperUser mapperUser;

    @Mock
    private CacheService cacheService;

    @Mock
    private GroupMemberService groupMemberService;

    @Mock
    private GroupService groupService;

    private UserServiceImpl userService;

     @BeforeEach
        void setUp() {
         userService = new UserServiceImpl(
             kafkaService, userRepository, mapperUser, "delete_test", cacheService, groupMemberService, groupService);
     }

    private static void accept(Throwable throwable) {
        assertEquals(NoSuchElementException.class, throwable.getClass());
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
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(userRepository).updateToVerifyEmail(anyString());
    }

    @Test
    void getUserById_UserIsFoundAndCacheNotFound_CorrectReturnAndCheckRepo(){
        ResponseUserDto responseUserDto = new ResponseUserDto(UUID.randomUUID(), "testName", "testSurname", "testEmail");
        when(userRepository.findByUserId(any())).thenReturn(Mono.just(responseUserDto));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(cacheService.saveValue(anyString(), any(), any())).thenReturn(Mono.empty());

        Mono<ResponseUserDto> result = userService.getUserById(jwt);

        StepVerifier.create(result)
                .expectNext(responseUserDto)
                .expectComplete()
                .verify();
        verify(cacheService).getValue(any(), any());
        verify(cacheService).saveValue(anyString(), any(), any());
        verify(userRepository).findByUserId(any());
    }
    @Test
    void getUserById_UserIsFoundAndCacheFound_CorrectReturnAndCheckRepo(){
        ResponseUserDto responseUserDto = new ResponseUserDto(UUID.randomUUID(), "testName", "testSurname", "testEmail");
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.just(responseUserDto));
        Mono<ResponseUserDto> result = userService.getUserById(jwt);

        StepVerifier.create(result)
                .expectNext(responseUserDto)
                .expectComplete()
                .verify();
        verify(cacheService).getValue(any(), any());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
        verify(userRepository, times(0)).findByUserId(any());
    }

    @Test
    void getUserById_UserIsNotFoundAndCacheNotFound_UnCorrectReturnAndCheckRepo(){
       when(userRepository.findByUserId(any())).thenReturn(Mono.empty());
       when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
       when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());

       Mono<ResponseUserDto> result = userService.getUserById(jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(UserServiceTest::accept)
                .verify();
        verify(cacheService).getValue(any(), any());
        verify(userRepository).findByUserId(any());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
    }

    @Test
    void updateUser_UpdateUserDtoIsCorrectAndUserIsFound_CorrectUpdateAndCheckRepo(){
        Mono<RequestUpdateUserDto> requestUpdateUserDtoMono = Mono.just(new RequestUpdateUserDto(null, null));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(mapperUser.addUpdateField(any(RequestUpdateUserDto.class))).thenReturn(Map.of());
        when(userRepository.updateFields(any(), any(), anyString(), any())).thenReturn(Mono.just(1L));
        when(cacheService.deleteValue(anyString())).thenReturn(Mono.empty());

        Mono<Void> result = userService.updateUser(requestUpdateUserDtoMono, jwt);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(userRepository).updateFields(any(), any(), anyString(), any());
        verify(cacheService).deleteValue(anyString());
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
        verify(cacheService, times(0)).deleteValue(anyString());
    }

    @Test
    void deleteUser_UserIsFoundInCache_CorrectDeleteAndCheckRepo(){
        UUID userId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(cacheService.getValue(any(), any()))
                .thenReturn(Mono.just(new ResponseUserDto(userId, null, null, null)));
        when(groupMemberService.existsMemberInGroupByOwnerId(any())).thenReturn(Mono.empty());
        when(groupService.getGroupsIdByUserId(any())).thenReturn(Flux.fromIterable(List.of(1L, 2L, 3L)));
        when(userRepository.deleteByUserId(any())).thenReturn(Mono.just(1));
        when(groupService.deleteDataRelatedGroupsByOwnerId(any())).thenReturn(Mono.empty());
        when(kafkaService.sendToTopic(anyString(), anyString())).thenReturn(Mono.empty());
        when(cacheService.deleteValue(anyString())).thenReturn(Mono.empty());

        Mono<Void> result = userService.deleteUser(jwt);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(groupMemberService).existsMemberInGroupByOwnerId(any());
        verify(groupService).getGroupsIdByUserId(any());
        verify(userRepository).deleteByUserId(any());
        verify(groupService).deleteDataRelatedGroupsByOwnerId(any());
        verify(kafkaService).sendToTopic(anyString(), anyString());
        verify(cacheService).deleteValue(anyString());
        verify(userRepository, times(0)).existsByUserId(any());
    }

    @Test
    void deleteUser_UserIsFoundAndUserNotInCache_CorrectDeleteAndCheckRepo(){
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(userRepository.existsByUserId(any())).thenReturn(Mono.just(Boolean.TRUE));
        when(groupMemberService.existsMemberInGroupByOwnerId(any())).thenReturn(Mono.empty());
        when(groupService.getGroupsIdByUserId(any())).thenReturn(Flux.fromIterable(List.of(1L, 2L, 3L)));
        when(userRepository.deleteByUserId(any())).thenReturn(Mono.just(1));
        when(groupService.deleteDataRelatedGroupsByOwnerId(any())).thenReturn(Mono.empty());
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(kafkaService.sendToTopic(anyString(), anyString())).thenReturn(Mono.empty());
        when(cacheService.deleteValue(anyString())).thenReturn(Mono.empty());

        Mono<Void> result = userService.deleteUser(jwt);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(cacheService).getValue(any(), any());
        verify(userRepository).existsByUserId(any());
        verify(groupMemberService).existsMemberInGroupByOwnerId(any());
        verify(groupService).getGroupsIdByUserId(any());
        verify(userRepository).deleteByUserId(any());
        verify(groupService).deleteDataRelatedGroupsByOwnerId(any());
        verify(kafkaService).sendToTopic(anyString(), anyString());
        verify(cacheService).deleteValue(anyString());
    }

    @Test
    void deleteUser_UserIsNotFound_NotDelete(){
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(userRepository.existsByUserId(any())).thenReturn(Mono.just(Boolean.FALSE));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());


        Mono<Void> result = userService.deleteUser(jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(cacheService).getValue(any(), any());
        verify(userRepository).existsByUserId(any());
        verify(groupMemberService, times(0)).existsMemberInGroupByOwnerId(any());
        verify(groupService, times(0)).deleteDataRelatedGroupsByOwnerId(any());
        verify(userRepository, times(0)).deleteByUserId(any());
        verify(kafkaService, times(0)).sendToTopic(anyString(), anyString());
        verify(cacheService, times(0)).deleteValue(anyString());
    }

    @Test
    void checkVerifyEmail_UserVerifyEmail_CheckReturnAndRepo(){
         when(userRepository.existsByUserIdAndVerifyEmailTrue(any())).thenReturn(Mono.just(Boolean.TRUE));

         Mono<Boolean> result = userService.checkVerifyEmail(UUID.randomUUID());

         StepVerifier.create(result)
                 .expectNext(Boolean.TRUE)
                 .verifyComplete();
         verify(userRepository).existsByUserIdAndVerifyEmailTrue(any());
    }

    @Test
    void checkVerifyEmail_UserNotVerifyEmail_CheckReturnAndRepo(){
        when(userRepository.existsByUserIdAndVerifyEmailTrue(any())).thenReturn(Mono.just(Boolean.FALSE));

        Mono<Boolean> result = userService.checkVerifyEmail(UUID.randomUUID());

        StepVerifier.create(result)
                .expectNext(Boolean.FALSE)
                .verifyComplete();
        verify(userRepository).existsByUserIdAndVerifyEmailTrue(any());
    }
}
