package com.subOne.user_service.service;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group_invite.CodeDto;
import com.subOne.user_service.dto.group_invite.response.ResponseInviteCodeDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.entity.GroupInvite;
import com.subOne.user_service.mapper.MapperGroupInvite;
import com.subOne.user_service.repository.group_invite_repository.GroupInviteRepository;
import com.subOne.user_service.service_impl.GroupInviteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private CacheService cacheService;

    @Mock
    private Jwt jwt;

    @Test
    void addUserByCode_CodeIsCorrectAndUserIsNotMember_CorrectAddAndCheckRepo(){
        ResponseMembersDto responseMembersDto = new ResponseMembersDto(List.of());
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupInviteRepository.findGroupIdByCode(any())).thenReturn(Mono.just(1L));
        when(groupMemberService.addUser(any(), anyLong())).thenReturn(Mono.just(responseMembersDto));
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.empty());

        Mono<ResponseMembersDto> result = groupInviteService.addUserByCode(UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .expectNext(responseMembersDto)
                .verifyComplete();
        verify(groupInviteRepository).findGroupIdByCode(any());
        verify(groupMemberService).addUser(any(), anyLong());
        verify(cacheService).getValue(anyString(), any());
    }

    @Test
    void addUserByCode_CodeIsNotCorrect_NotCorrectAddAndCheckRepo(){
        when(groupInviteRepository.findGroupIdByCode(any())).thenReturn(Mono.empty());
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.empty());

        Mono<ResponseMembersDto> result = groupInviteService.addUserByCode(UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(ResponseStatusException.class, throwable.getClass()))
                .verify();
        verify(groupInviteRepository).findGroupIdByCode(any());
        verify(groupMemberService, times(0)).addUser(any(), anyLong());
        verify(cacheService).getValue(anyString(), any());
    }

    @Test
    @DisplayName("Проверка создания кода при наличии группы и код не истек")
    void  getCodeByGroupId_UserIsOwnerAndGroupIsFoundAndCodeIsNotCreated_CorrectReturnAndCheckRepo(){
        CodeDto codeDto = new CodeDto(UUID.randomUUID(), LocalDateTime.now().plusMinutes(5));
        when(groupService.checkUserIsOwner(anyLong(), any())).thenReturn(Mono.empty());
        when(groupInviteRepository.findCodeByGroupId(anyLong()))
                .thenReturn(Mono.just(codeDto));

        Mono<ResponseInviteCodeDto> result = groupInviteService.getCodeByGroupId(anyLong(), any());

        ResponseInviteCodeDto responseInviteCodeDto = new ResponseInviteCodeDto(
                codeDto.code(),
                Duration.between(LocalDateTime.now(), codeDto.expiresAt()).toSeconds());
        StepVerifier.create(result)
                .assertNext(code -> assertEquals(responseInviteCodeDto.code(), code.code()))
                .verifyComplete();
        verify(groupService).checkUserIsOwner(anyLong(), any());
        verify(groupInviteRepository).findCodeByGroupId(anyLong());
        verify(groupInviteRepository, times(0)).save(any());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
    }


    @Test
    @DisplayName("Проверка создания кода при наличии группы и код истек")
    void  getCodeByGroupId_UserIsOwnerAndGroupIsFoundAndCodeIsCreated_CorrectReturnAndCheckRepo(){
        GroupInvite groupInvite = new GroupInvite();
        groupInvite.setCode(UUID.randomUUID());
        groupInvite.setGroupId(1L);
        groupInvite.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        when(groupService.checkUserIsOwner(anyLong(), any())).thenReturn(Mono.empty());
        when(groupInviteRepository.findCodeByGroupId(anyLong())).thenReturn(Mono.empty());
        when(mapperGroupInvite.codeAndGroupIdToGroupInvite(anyLong(), any())).thenReturn(new GroupInvite());
        when(groupInviteRepository.save(any())).thenReturn(Mono.just(groupInvite));
        when(cacheService.saveValue(anyString(), any(), any())).thenReturn(Mono.empty());

        Mono<ResponseInviteCodeDto> result = groupInviteService.getCodeByGroupId(anyLong(), any());

        ResponseInviteCodeDto responseInviteCodeDto = new ResponseInviteCodeDto(
                groupInvite.getCode(), Duration.ofMinutes(5).toSeconds());
        StepVerifier.create(result)
                .expectNext(responseInviteCodeDto)
                .verifyComplete();
        verify(groupService).checkUserIsOwner(anyLong(), any());
        verify(groupInviteRepository).findCodeByGroupId(anyLong());
        verify(groupInviteRepository).save(any());
        verify(cacheService).saveValue(anyString(), any(), any());
    }


}
