package com.subOne.user_service.service;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group.response.ResponseGroupOwnerIdDto;
import com.subOne.user_service.dto.group_invite.CodeDto;
import com.subOne.user_service.dto.group_invite.response.ResponseInviteCodeDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.entity.GroupInvite;
import com.subOne.user_service.kafka.serviceProducer.KafkaService;
import com.subOne.user_service.mapper.MapperGroupInvite;
import com.subOne.user_service.repository.group_invite_repository.GroupInviteRepository;
import com.subOne.user_service.service_impl.GroupInviteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class GroupInviteServiceTest {

    @InjectMocks
    private GroupInviteServiceImpl groupInviteService;

    @Mock
    private GroupMemberService groupMemberService;

    @Mock
    private GroupService groupService;

    @Mock
    private MapperGroupInvite mapperGroupInvite;

    @Mock
    private GroupInviteRepository groupInviteRepository;

    @Mock
    private UserService userService;

    @Mock
    private CacheService cacheService;

    @Mock
    private KafkaService kafkaService;

    @Mock
    private Jwt jwt;

    @Test
    void addUserByCode_CodeIsCorrectAndUserIsNotMemberAndVerifyEmailAndCacheNotFound_CorrectAddAndCheckRepo(){
        ResponseMembersDto responseMembersDto = new ResponseMembersDto(List.of());
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(userService.checkVerifyEmail(any())).thenReturn(Mono.just(Boolean.TRUE));
        when(groupInviteRepository.findGroupIdByCode(any())).thenReturn(Mono.just(1));
        when(groupMemberService.addUser(any(), anyInt())).thenReturn(Mono.just(responseMembersDto));
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.empty());
        when(kafkaService.sendToTopic(anyString(), any(), any())).thenReturn(Mono.empty());
        when(groupService.getOwnerId(anyInt())).thenReturn(Mono.just(new ResponseGroupOwnerIdDto(UUID.randomUUID())));

        Mono<ResponseMembersDto> result = groupInviteService.addUserByCode(UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .expectNext(responseMembersDto)
                .verifyComplete();
        verify(userService).checkVerifyEmail(any());
        verify(groupInviteRepository).findGroupIdByCode(any());
        verify(groupMemberService).addUser(any(), anyInt());
        verify(groupService).getOwnerId(anyInt());
        verify(cacheService).getValue(anyString(), any());
        verify(kafkaService).sendToTopic(anyString(), any(), any());
    }
    @Test
    void addUserByCode_CodeIsCorrectAndUserIsNotMemberAndVerifyEmailAndCacheFound_CorrectAddAndCheckRepo(){
        ResponseMembersDto responseMembersDto = new ResponseMembersDto(List.of());
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(userService.checkVerifyEmail(any())).thenReturn(Mono.just(Boolean.TRUE));
        when(groupMemberService.addUser(any(), anyInt())).thenReturn(Mono.just(responseMembersDto));
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.just(1));
        when(kafkaService.sendToTopic(anyString(), any(), any())).thenReturn(Mono.empty());
        when(groupService.getOwnerId(anyInt())).thenReturn(Mono.just(new ResponseGroupOwnerIdDto(UUID.randomUUID())));

        Mono<ResponseMembersDto> result = groupInviteService.addUserByCode(UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .expectNext(responseMembersDto)
                .verifyComplete();
        verify(userService).checkVerifyEmail(any());
        verify(groupInviteRepository, times(0)).findGroupIdByCode(any());
        verify(groupService).getOwnerId(anyInt());
        verify(groupMemberService).addUser(any(), anyInt());
        verify(cacheService).getValue(anyString(), any());
        verify(kafkaService).sendToTopic(anyString(), any(), any());
    }

    @Test
    void addUserByCode_UserNotVerifyEmail_NotCorrectAddAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(userService.checkVerifyEmail(any())).thenReturn(Mono.just(Boolean.FALSE));

        Mono<ResponseMembersDto> result = groupInviteService.addUserByCode(UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(AccessDeniedException.class, throwable.getClass()))
                .verify();
        verify(userService).checkVerifyEmail(any());
        verify(groupInviteRepository, times(0)).findGroupIdByCode(any());
        verify(groupMemberService, times(0)).addUser(any(), anyInt());
        verify(cacheService, times(0)).getValue(anyString(), any());
    }

    @Test
    void addUserByCode_CodeIsNotCorrect_NotCorrectAddAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(userService.checkVerifyEmail(any())).thenReturn(Mono.just(Boolean.TRUE));
        when(groupInviteRepository.findGroupIdByCode(any())).thenReturn(Mono.empty());
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.empty());

        Mono<ResponseMembersDto> result = groupInviteService.addUserByCode(UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(ResponseStatusException.class, throwable.getClass()))
                .verify();
        verify(userService).checkVerifyEmail(any());
        verify(groupInviteRepository).findGroupIdByCode(any());
        verify(groupMemberService, times(0)).addUser(any(), anyInt());
        verify(cacheService).getValue(anyString(), any());
    }

    @Test
    @DisplayName("Проверка создания кода при наличии группы и код не истек")
    void  getCodeByGroupId_UserIsOwnerAndGroupIsFoundAndCodeIsNotCreated_CorrectReturnAndCheckRepo(){
        CodeDto codeDto = new CodeDto(UUID.randomUUID(), LocalDateTime.now().plusMinutes(5));
        when(groupService.checkUserIsOwner(anyInt(), any())).thenReturn(Mono.empty());
        when(groupInviteRepository.findCodeByGroupId(anyInt()))
                .thenReturn(Mono.just(codeDto));

        Mono<ResponseInviteCodeDto> result = groupInviteService.getCodeByGroupId(anyInt(), any());

        ResponseInviteCodeDto responseInviteCodeDto = new ResponseInviteCodeDto(
                codeDto.code(),
                Duration.between(LocalDateTime.now(), codeDto.expiresAt()).toSeconds());
        StepVerifier.create(result)
                .assertNext(code -> assertEquals(responseInviteCodeDto.code(), code.code()))
                .verifyComplete();
        verify(groupService).checkUserIsOwner(anyInt(), any());
        verify(groupInviteRepository).findCodeByGroupId(anyInt());
        verify(groupInviteRepository, times(0)).save(any());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
    }


    @Test
    @DisplayName("Проверка создания кода при наличии группы и код истек")
    void  getCodeByGroupId_UserIsOwnerAndGroupIsFoundAndCodeIsCreated_CorrectReturnAndCheckRepo(){
        GroupInvite groupInvite = new GroupInvite();
        groupInvite.setCode(UUID.randomUUID());
        groupInvite.setGroupId(1);
        groupInvite.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        when(groupService.checkUserIsOwner(anyInt(), any())).thenReturn(Mono.empty());
        when(groupInviteRepository.findCodeByGroupId(anyInt())).thenReturn(Mono.empty());
        when(mapperGroupInvite.codeAndGroupIdToGroupInvite(anyInt(), any())).thenReturn(new GroupInvite());
        when(groupInviteRepository.save(any())).thenReturn(Mono.just(groupInvite));
        when(cacheService.saveValue(anyString(), any(), any())).thenReturn(Mono.empty());

        Mono<ResponseInviteCodeDto> result = groupInviteService.getCodeByGroupId(anyInt(), any());

        ResponseInviteCodeDto responseInviteCodeDto = new ResponseInviteCodeDto(
                groupInvite.getCode(), Duration.ofMinutes(5).toSeconds());
        StepVerifier.create(result)
                .expectNext(responseInviteCodeDto)
                .verifyComplete();
        verify(groupService).checkUserIsOwner(anyInt(), any());
        verify(groupInviteRepository).findCodeByGroupId(anyInt());
        verify(groupInviteRepository).save(any());
        verify(cacheService).saveValue(anyString(), any(), any());
    }


}
