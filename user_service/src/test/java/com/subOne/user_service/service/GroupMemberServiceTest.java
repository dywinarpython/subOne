package com.subOne.user_service.service;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group_member.GroupMemberInfoDto;
import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.GroupMember;
import com.subOne.user_service.mapper.MapperGroupMember;
import com.subOne.user_service.repository.GroupMemberRepository;
import com.subOne.user_service.service_impl.GroupMemberServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupMemberServiceTest {

    @Mock
    private MapperGroupMember mapperGroupMember;

    @Mock
    private GroupService groupService;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private Jwt jwt;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private GroupMemberServiceImpl groupMemberService;

    @Test
    @DisplayName("Проверка добавления пользователя в группу (он не в группе, количество members < 5) ")
    void addUser_UserIsNotMemberGroupAndGroupCountMemberIsNoMoreFive_CorrectReturnAndSaveToDbAndCheckRepo(){
        ResponseMembersDto responseMembersDto = new ResponseMembersDto(
                List.of(
                        new ResponseMemberDto(new ResponseUserDto(UUID.randomUUID(), "test1", "surname1", "email1"), true),
                        new ResponseMemberDto(new ResponseUserDto(UUID.randomUUID(), "test2", "surname2", "email2"), false),
                        new ResponseMemberDto(new ResponseUserDto(UUID.randomUUID(), "test3", "surname3", "email3"), false)
                )
        );
        when(groupMemberRepository.findExistUserInGroupAndCountMemberInGroup(any(), anyLong()))
                .thenReturn(Mono.just(new GroupMemberInfoDto(false, 1L)));
        when(groupMemberRepository.save(any())).thenReturn(Mono.just(new GroupMember()));
        when(mapperGroupMember.userIdAndGroupIdToGroupMember(any(), anyLong())).thenReturn(new GroupMember());
        when(groupService.getOwner(anyLong())).thenReturn(Mono.just(responseMembersDto.users().getFirst().user()));
        when(groupMemberRepository.findMembersIdByGroupId(anyLong()))
                .thenReturn(Flux.fromIterable(responseMembersDto.users()
                        .stream()
                        .filter(responseMemberDto -> !responseMemberDto.owner())
                        .map(ResponseMemberDto::user)
                        .toList()
                ));

        Mono<ResponseMembersDto> responseMembersDtoMono = groupMemberService.addUser(any(), anyLong());

        StepVerifier.create(responseMembersDtoMono)
                .expectNext(responseMembersDto)
                .verifyComplete();

        verify(groupMemberRepository).findExistUserInGroupAndCountMemberInGroup(any(), anyLong());
        verify(groupMemberRepository).save(any());
        verify(groupService).getOwner(anyLong());
        verify(groupMemberRepository).findMembersIdByGroupId(anyLong());
    }

    @Test
    @DisplayName("Проверка добавления пользователя в группу (он не в группе, количество members > 5) ")
    void addUser_UserIsNotMemberGroupAndGroupCountMemberIsMoreFive_NotCorrectReturnAndNotSaveToDbAndCheckRepo(){
        when(groupMemberRepository.findExistUserInGroupAndCountMemberInGroup(any(), anyLong()))
                .thenReturn(Mono.just(new GroupMemberInfoDto(false, 5L)));

        Mono<ResponseMembersDto> result = groupMemberService.addUser(any(), anyLong());

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ResponseStatusException.class, throwable.getClass()))
                .verify();
        verify(groupMemberRepository).findExistUserInGroupAndCountMemberInGroup(any(), anyLong());
        verify(groupMemberRepository, times(0)).save(any());
        verify(groupService, times(0)).getOwner(anyLong());
        verify(groupMemberRepository, times(0)).findMembersIdByGroupId(anyLong());
    }

    @Test
    @DisplayName("Проверка добавления пользователя в группу (он уже в этой группе) ")
    void addUser_UserIsMemberGroup_NotCorrectReturnAndNotSaveToDbAndCheckRepo(){
        when(groupMemberRepository.findExistUserInGroupAndCountMemberInGroup(any(), anyLong()))
                .thenReturn(Mono.just(new GroupMemberInfoDto(true, 1L)));

        Mono<ResponseMembersDto> responseMembersDtoMono = groupMemberService.addUser(any(), anyLong());

        StepVerifier.create(responseMembersDtoMono)
                .expectErrorSatisfies(throwable -> assertEquals(ResponseStatusException.class, throwable.getClass()))
                .verify();
        verify(groupMemberRepository).findExistUserInGroupAndCountMemberInGroup(any(), anyLong());
        verify(groupMemberRepository, times(0)).save(any());
        verify(groupService, times(0)).getOwner(anyLong());
        verify(groupMemberRepository, times(0)).findMembersIdByGroupId(anyLong());
    }

    @Test
    void getUsers_UserIsMember_CorrectReturnAndCheckRepo(){
        ResponseMembersDto responseMembersDto = new ResponseMembersDto(
                List.of(
                        new ResponseMemberDto(new ResponseUserDto(UUID.randomUUID(), "test1", "surname1", "email1"), true),
                        new ResponseMemberDto(new ResponseUserDto(UUID.randomUUID(), "test2", "surname2", "email2"), false),
                        new ResponseMemberDto(new ResponseUserDto(UUID.randomUUID(), "test3", "surname3", "email3"), false)
                )
        );
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.empty());
        when(cacheService.saveValue(anyString(), any(), any())).thenReturn(Mono.empty());
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupMemberRepository.existsByGroupIdAndUserId(any(), any())).thenReturn(Mono.just(true));
        when(groupService.getOwner(anyLong())).thenReturn(Mono.just(responseMembersDto.users().getFirst().user()));
        when(groupMemberRepository.findMembersIdByGroupId(anyLong()))
                .thenReturn(Flux.fromIterable(responseMembersDto.users()
                        .stream()
                        .filter(responseMemberDto -> !responseMemberDto.owner())
                        .map(ResponseMemberDto::user)
                        .toList()
                ));

        Mono<ResponseMembersDto> responseMembersDtoMono = groupMemberService.getUsers(1L, jwt);

        StepVerifier.create(responseMembersDtoMono)
                .expectNext(responseMembersDto)
                .verifyComplete();
        verify(groupMemberRepository).existsByGroupIdAndUserId(anyLong(), any());
        verify(groupService, times(0)).checkUserIsOwner(anyLong(), any());
        verify(groupService).getOwner(anyLong());
        verify(groupMemberRepository).findMembersIdByGroupId(anyLong());
        verify(cacheService).saveValue(anyString(), any(), any());
        verify(cacheService, times(2)).getValue(anyString(), any());
    }

    @Test
    void getUsers_UserIsOwner_CorrectReturnAndCheckRepo(){
        ResponseMembersDto responseMembersDto = new ResponseMembersDto(
                List.of(
                        new ResponseMemberDto(new ResponseUserDto(UUID.randomUUID(), "test1", "surname1", "email1"), true),
                        new ResponseMemberDto(new ResponseUserDto(UUID.randomUUID(), "test2", "surname2", "email2"), false),
                        new ResponseMemberDto(new ResponseUserDto(UUID.randomUUID(), "test3", "surname3", "email3"), false)
                )
        );
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.empty());
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupMemberRepository.existsByGroupIdAndUserId(anyLong(), any())).thenReturn(Mono.just(false));
        when(groupService.getOwner(anyLong())).thenReturn(Mono.just(responseMembersDto.users().getFirst().user()));
        when(groupService.checkUserIsOwner(anyLong(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.findMembersIdByGroupId(anyLong()))
                .thenReturn(Flux.fromIterable(responseMembersDto.users()
                        .stream()
                        .filter(responseMemberDto -> !responseMemberDto.owner())
                        .map(ResponseMemberDto::user)
                        .toList()
                ));

        Mono<ResponseMembersDto> responseMembersDtoMono = groupMemberService.getUsers(1L, jwt);

        StepVerifier.create(responseMembersDtoMono)
                .expectNext(responseMembersDto)
                .verifyComplete();
        verify(groupMemberRepository).existsByGroupIdAndUserId(anyLong(), any());
        verify(groupService).checkUserIsOwner(anyLong(), any());
        verify(groupService).getOwner(anyLong());
        verify(groupMemberRepository).findMembersIdByGroupId(anyLong());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
        verify(cacheService, times(2)).getValue(anyString(), any());
    }

    @Test
    @DisplayName("Удаления пользователя (он в группе, удаляет owner)")
    void deleteUser_UserIsOwnerGroupAndUserDeleteIsMember_CorrectDeleteAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupService.checkUserIsOwner(anyLong(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.deleteByUserId(any())).thenReturn(Mono.just(1L));

        Mono<Void> result = groupMemberService.deleteMember(1L, UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .verifyComplete();
        verify(groupService).checkUserIsOwner(anyLong(), any());
        verify(groupMemberRepository).deleteByUserId(any());
    }

    @Test
    @DisplayName("Удаления пользователя (user is not found, удаляет owner)")
    void deleteUser_UserIsOwnerGroupAndUserIsNotOwner_CorrectDeleteAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupService.checkUserIsOwner(anyLong(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.deleteByUserId(any())).thenReturn(Mono.just(0L));

        Mono<Void> result = groupMemberService.deleteMember(1L, UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                    assertEquals(NoSuchElementException.class, throwable.getClass())
                ).verify();
        verify(groupService).checkUserIsOwner(anyLong(), any());
        verify(groupMemberRepository).deleteByUserId(any());
    }

}
