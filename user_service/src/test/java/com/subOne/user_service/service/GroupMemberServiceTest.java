package com.subOne.user_service.service;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group_member.GroupMemberInfoDto;
import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.dto.user.request.RequestGroupOwnershipChangesDto;
import com.subOne.user_service.dto.user.request.RequestGroupsOwnershipChangesDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.GroupMember;
import com.subOne.user_service.exception.ConflictException;
import com.subOne.user_service.kafka.serviceProducer.KafkaService;
import com.subOne.user_service.mapper.MapperGroupMember;
import com.subOne.user_service.repository.group_member_repository.GroupMemberRepository;
import com.subOne.user_service.service_impl.GroupMemberServiceImpl;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @Mock
    private KafkaService kafkaService;

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
        when(groupMemberRepository.findExistUserInGroupAndCountMemberInGroup(any(), anyInt()))
                .thenReturn(Mono.just(new GroupMemberInfoDto(false, 1L)));
        when(groupMemberRepository.save(any())).thenReturn(Mono.just(new GroupMember()));
        when(mapperGroupMember.userIdAndGroupIdToGroupMember(any(), anyInt())).thenReturn(new GroupMember());
        when(groupService.getOwner(anyInt())).thenReturn(Mono.just(responseMembersDto.users().getFirst().user()));
        when(groupMemberRepository.findMembersIdByGroupId(anyInt()))
                .thenReturn(Flux.fromIterable(responseMembersDto.users()
                        .stream()
                        .filter(responseMemberDto -> !responseMemberDto.owner())
                        .map(ResponseMemberDto::user)
                        .toList()
                ));

        Mono<ResponseMembersDto> responseMembersDtoMono = groupMemberService.addUser(any(), anyInt());

        StepVerifier.create(responseMembersDtoMono)
                .expectNext(responseMembersDto)
                .verifyComplete();

        verify(groupMemberRepository).findExistUserInGroupAndCountMemberInGroup(any(), anyInt());
        verify(groupMemberRepository).save(any());
        verify(groupService).getOwner(anyInt());
        verify(groupMemberRepository).findMembersIdByGroupId(anyInt());
    }

    @Test
    @DisplayName("Проверка добавления пользователя в группу (он не в группе, количество members > 5) ")
    void addUser_UserIsNotMemberGroupAndGroupCountMemberIsMoreFive_NotCorrectReturnAndNotSaveToDbAndCheckRepo(){
        when(groupMemberRepository.findExistUserInGroupAndCountMemberInGroup(any(), anyInt()))
                .thenReturn(Mono.just(new GroupMemberInfoDto(false, 5L)));

        Mono<ResponseMembersDto> result = groupMemberService.addUser(any(), anyInt());

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ConflictException.class, throwable.getClass()))
                .verify();
        verify(groupMemberRepository).findExistUserInGroupAndCountMemberInGroup(any(), anyInt());
        verify(groupMemberRepository, times(0)).save(any());
        verify(groupService, times(0)).getOwner(anyInt());
        verify(groupMemberRepository, times(0)).findMembersIdByGroupId(anyInt());
    }

    @Test
    @DisplayName("Проверка добавления пользователя в группу (он уже в этой группе) ")
    void addUser_UserIsMemberGroup_NotCorrectReturnAndNotSaveToDbAndCheckRepo(){
        when(groupMemberRepository.findExistUserInGroupAndCountMemberInGroup(any(), anyInt()))
                .thenReturn(Mono.just(new GroupMemberInfoDto(true, 1L)));

        Mono<ResponseMembersDto> responseMembersDtoMono = groupMemberService.addUser(any(), anyInt());

        StepVerifier.create(responseMembersDtoMono)
                .expectErrorSatisfies(throwable -> assertEquals(ConflictException.class, throwable.getClass()))
                .verify();
        verify(groupMemberRepository).findExistUserInGroupAndCountMemberInGroup(any(), anyInt());
        verify(groupMemberRepository, times(0)).save(any());
        verify(groupService, times(0)).getOwner(anyInt());
        verify(groupMemberRepository, times(0)).findMembersIdByGroupId(anyInt());
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
        when(groupService.getOwner(anyInt())).thenReturn(Mono.just(responseMembersDto.users().getFirst().user()));
        when(groupMemberRepository.findMembersIdByGroupId(anyInt()))
                .thenReturn(Flux.fromIterable(responseMembersDto.users()
                        .stream()
                        .filter(responseMemberDto -> !responseMemberDto.owner())
                        .map(ResponseMemberDto::user)
                        .toList()
                ));

        Mono<ResponseMembersDto> responseMembersDtoMono = groupMemberService.getUsers(1, jwt);

        StepVerifier.create(responseMembersDtoMono)
                .expectNext(responseMembersDto)
                .verifyComplete();
        verify(groupMemberRepository).existsByGroupIdAndUserId(anyInt(), any());
        verify(groupService, times(0)).checkUserIsOwner(anyInt(), any());
        verify(groupService).getOwner(anyInt());
        verify(groupMemberRepository).findMembersIdByGroupId(anyInt());
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
        when(groupMemberRepository.existsByGroupIdAndUserId(anyInt(), any())).thenReturn(Mono.just(false));
        when(groupService.getOwner(anyInt())).thenReturn(Mono.just(responseMembersDto.users().getFirst().user()));
        when(groupService.checkUserIsOwnerWithoutCacheGet(anyInt(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.findMembersIdByGroupId(anyInt()))
                .thenReturn(Flux.fromIterable(responseMembersDto.users()
                        .stream()
                        .filter(responseMemberDto -> !responseMemberDto.owner())
                        .map(ResponseMemberDto::user)
                        .toList()
                ));

        Mono<ResponseMembersDto> responseMembersDtoMono = groupMemberService.getUsers(1, jwt);

        StepVerifier.create(responseMembersDtoMono)
                .expectNext(responseMembersDto)
                .verifyComplete();
        verify(groupMemberRepository).existsByGroupIdAndUserId(anyInt(), any());
        verify(groupService).checkUserIsOwnerWithoutCacheGet(anyInt(), any());
        verify(groupService).getOwner(anyInt());
        verify(groupMemberRepository).findMembersIdByGroupId(anyInt());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
        verify(cacheService, times(2)).getValue(anyString(), any());
    }

    @Test
    @DisplayName("Удаления пользователя (он в группе, удаляет owner)")
    void deleteMember_UserIsOwnerGroupAndUserDeleteIsMember_CorrectDeleteAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupService.checkUserIsOwner(anyInt(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.deleteByUserIdAndGroupId(any(), anyInt())).thenReturn(Mono.just(1L));
        when(cacheService.deleteValue(anyString())).thenReturn(Mono.empty());
        when(kafkaService.sendToTopic(anyString(), any(), any())).thenReturn(Mono.empty());

        Mono<Void> result = groupMemberService.deleteMember(1, UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .verifyComplete();
        verify(groupService).checkUserIsOwner(anyInt(), any());
        verify(groupMemberRepository).deleteByUserIdAndGroupId(any(), anyInt());
        verify(cacheService).deleteValue(anyString());
        verify(kafkaService).sendToTopic(anyString(), any(), any());
    }

    @Test
    @DisplayName("Удаления пользователя (user is not found, удаляет owner)")
    void deleteMember_UserIsOwnerGroupAndUserIsNotFound_CorrectDeleteAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupService.checkUserIsOwner(anyInt(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.deleteByUserIdAndGroupId(any(), anyInt())).thenReturn(Mono.just(0L));

        Mono<Void> result = groupMemberService.deleteMember(1, UUID.randomUUID(), jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                    assertEquals(NoSuchElementException.class, throwable.getClass())
                ).verify();
        verify(groupService).checkUserIsOwner(anyInt(), any());
        verify(groupMemberRepository).deleteByUserIdAndGroupId(any(), anyInt());
        verify(cacheService, times(0)).deleteValue(anyString());
        verify(kafkaService, times(0)).sendToTopic(anyString(), any(), any());
    }

    @Test
    void checkUserInGroup_GroupFoundAndUserMemberAndCacheFoundMember_CorrectCheckAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.just(Boolean.TRUE));

        Mono<Boolean> result = groupMemberService.checkUserInGroup(1, jwt);

        StepVerifier.create(result)
                .expectNext(Boolean.TRUE)
                .verifyComplete();
        verify(cacheService).getValue(any(), any());
    }

    @Test
    void checkUserInGroup_GroupFoundAndUserOwnerAndCacheFoundOwner_CorrectCheckAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(cacheService.getValue(any(), any()))
                .thenReturn(Mono.empty())
                .thenReturn(Mono.just(UUID.fromString(jwt.getSubject())));

        Mono<Boolean> result = groupMemberService.checkUserInGroup(1, jwt);

        StepVerifier.create(result)
                .expectNext(Boolean.TRUE)
                .verifyComplete();
        verify(cacheService, times(2)).getValue(any(), any());
    }

    @Test
    void checkUserInGroup_GroupFoundAndUserMemberAndCacheNotFound_CorrectCheckAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.existsByGroupIdAndUserId(anyInt(), any())).thenReturn(Mono.just(Boolean.TRUE));
        when(cacheService.saveValue(anyString(), any(), any())).thenReturn(Mono.empty());

        Mono<Boolean> result = groupMemberService.checkUserInGroup(1, jwt);

        StepVerifier.create(result)
                .expectNext(Boolean.TRUE)
                .verifyComplete();
        verify(cacheService, times(2)).getValue(any(), any());
        verify(groupMemberRepository).existsByGroupIdAndUserId(anyInt(), any());
        verify(cacheService).saveValue(anyString(), any(), any());
        verify(groupService, times(0)).checkUserIsOwnerWithoutCacheGet(anyInt(), any());
    }

    @Test
    void checkUserInGroup_GroupFoundAndUserOwnerAndCacheNotFound_CorrectCheckAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.existsByGroupIdAndUserId(anyInt(), any())).thenReturn(Mono.just(Boolean.FALSE));
        when(groupService.checkUserIsOwnerWithoutCacheGet(anyInt(), any())).thenReturn(Mono.just(Boolean.TRUE));

        Mono<Boolean> result = groupMemberService.checkUserInGroup(1, jwt);

        StepVerifier.create(result)
                .expectNext(Boolean.TRUE)
                .verifyComplete();
        verify(cacheService, times(2)).getValue(any(), any());
        verify(groupMemberRepository).existsByGroupIdAndUserId(anyInt(), any());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
        verify(groupService).checkUserIsOwnerWithoutCacheGet(anyInt(), any());
    }

    @Test
    void checkUserInGroup_GroupFoundAndUserNotMemberAndCacheNotFound_CorrectCheckAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.existsByGroupIdAndUserId(anyInt(), any())).thenReturn(Mono.just(Boolean.FALSE));
        when(groupService.checkUserIsOwnerWithoutCacheGet(anyInt(), any())).thenReturn(Mono.just(Boolean.FALSE));

        Mono<Boolean> result = groupMemberService.checkUserInGroup(1, jwt);

        StepVerifier.create(result)
                .expectNext(Boolean.FALSE)
                .verifyComplete();
        verify(cacheService, times(2)).getValue(any(), any());
        verify(groupMemberRepository).existsByGroupIdAndUserId(anyInt(), any());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
        verify(groupService).checkUserIsOwnerWithoutCacheGet(anyInt(), any());
    }


    @Test
    void checkUserIsOwnerGroup_GroupFoundAndUserOwner_CheckService(){
        when(groupService.checkUserIsOwner(anyInt(), any())).thenReturn(Mono.just(Boolean.TRUE));

        Mono<Boolean> result = groupMemberService.checkUserIsOwnerGroup(anyInt(), any());

        StepVerifier.create(result)
                .expectNext(Boolean.TRUE)
                .verifyComplete();

        verify(groupService).checkUserIsOwner(anyInt(), any());
    }

    @Test
    void checkUserIsOwnerGroup_GroupFoundAndUserNoOwner_CheckService(){
        when(groupService.checkUserIsOwner(anyInt(), any())).thenReturn(Mono.just(Boolean.FALSE));

        Mono<Boolean> result = groupMemberService.checkUserIsOwnerGroup(anyInt(), any());

        StepVerifier.create(result)
                .expectNext(Boolean.FALSE)
                .verifyComplete();

        verify(groupService).checkUserIsOwner(anyInt(), any());
    }

    @Test
    void existsMemberInGroupByOwnerId_UserNotHaveGroupWhereMember_CorrectReturnAndCheckRepo(){
        when(groupMemberRepository.findGroupWhereExistsUserByOwnerId(any())).thenReturn(Flux.empty());
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());

        Mono<Void> result = groupMemberService.existsMemberInGroupByOwnerId(jwt);

        StepVerifier.create(result)
                .verifyComplete();
        verify(groupMemberRepository).findGroupWhereExistsUserByOwnerId(any());
    }

    @Test
    void existsMemberInGroupByOwnerId_UserHaveGroupWhereMember_NoCorrectReturnAndCheckRepo(){
        when(groupMemberRepository.findGroupWhereExistsUserByOwnerId(any())).thenReturn(Flux.fromIterable(List.of(1L, 2L, 3L)));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());

        Mono<Void> result = groupMemberService.existsMemberInGroupByOwnerId(jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ConflictException.class, throwable.getClass()))
                .verify();
        verify(groupMemberRepository).findGroupWhereExistsUserByOwnerId(any());
    }

    @Test
    @DisplayName("Изменение собственника (owner -> yes, cache -> all get, user -> are member)")
    void changesOwnerGroup_UserOwnerGroupsAndUsersAreMemberAndCacheGetAll_CorrectChangesAndCheckRepo(){
        RequestGroupsOwnershipChangesDto requestGroupsOwnershipChangesDto = new RequestGroupsOwnershipChangesDto(
                List.of(
                        new RequestGroupOwnershipChangesDto(UUID.randomUUID(), 1),
                        new RequestGroupOwnershipChangesDto(UUID.randomUUID(), 2),
                        new RequestGroupOwnershipChangesDto(UUID.randomUUID(), 3)
                )
        );
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupService.checkUserIsOwnerGroups(any(), any())).thenReturn(Mono.empty());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.just(Boolean.TRUE));
        when(groupMemberRepository.deleteAllByUserGroupPairs(any())).thenReturn(Mono.empty());
        when(groupMemberRepository.updateOwnerGroup(any())).thenReturn(Mono.empty());
        when(groupMemberRepository.insertAllMembers(any(), any())).thenReturn(Mono.empty());
        when(cacheService.deleteValue(anyString())).thenReturn(Mono.empty());
        when(kafkaService.sendToTopic(anyString(), any(), any())).thenReturn(Mono.empty());

        Mono<Void> result = groupMemberService.changesOwnerGroup(Mono.just(requestGroupsOwnershipChangesDto), jwt);

        StepVerifier.create(result)
                .verifyComplete();
        verify(groupService).checkUserIsOwnerGroups(any(), any());
        verify(cacheService, times(3)).getValue(any(), any());
        verify(groupMemberRepository, times(0)).existsByGroupIdAndUserId(anyInt(), any());
        verify(groupMemberRepository).deleteAllByUserGroupPairs(any());
        verify(groupMemberRepository).updateOwnerGroup(any());
        verify(groupMemberRepository).insertAllMembers(any(), any());
        verify(cacheService, times(6)).deleteValue(anyString());
        verify(kafkaService, times(requestGroupsOwnershipChangesDto.groupOwnershipChanges().size())).sendToTopic(anyString(), any(), any());
    }

    @Test
    @DisplayName("Изменение собственника (owner -> yes, cache -> not all get, last user not member)")
    void changesOwnerGroup_UserOwnerGroupsAndLasUserNotMemberAndCacheGetAll_NotCorrectChangesAndCheckRepo(){
        RequestGroupsOwnershipChangesDto requestGroupsOwnershipChangesDto = new RequestGroupsOwnershipChangesDto(
                List.of(
                        new RequestGroupOwnershipChangesDto(UUID.randomUUID(), 1),
                        new RequestGroupOwnershipChangesDto(UUID.randomUUID(), 2),
                        new RequestGroupOwnershipChangesDto(UUID.randomUUID(), 3)
                )
        );
        when(groupService.checkUserIsOwnerGroups(any(), any())).thenReturn(Mono.empty());
        when(cacheService.getValue(any(), any())).thenReturn(Mono.empty());
        when(groupMemberRepository.existsByGroupIdAndUserId(anyInt(), any()))
                .thenReturn(Mono.just(Boolean.TRUE))
                .thenReturn(Mono.just(Boolean.TRUE))
                .thenReturn(Mono.just(Boolean.FALSE));

        Mono<Void> result = groupMemberService.changesOwnerGroup(Mono.just(requestGroupsOwnershipChangesDto), jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(groupService).checkUserIsOwnerGroups(any(), any());
        verify(cacheService, times(3)).getValue(any(), any());
        verify(groupMemberRepository, times(3)).existsByGroupIdAndUserId(anyInt(), any());
        verify(groupMemberRepository, times(0)).deleteByUserIdAndGroupId(any(), anyInt());
        verify(groupMemberRepository, times(0)).updateOwnerGroup(any());
        verify(groupMemberRepository, times(0)).insertAllMembers(any(), any());
        verify(cacheService, times(0)).deleteValue(anyString());
        verify(kafkaService, times(0)).sendToTopic(anyString(), any(), any());
    }

    @Test
    void changesOwnerGroup_GroupsIdDublicated_NotCorrectChangesAndCheckRepo(){
        RequestGroupsOwnershipChangesDto requestGroupsOwnershipChangesDto = new RequestGroupsOwnershipChangesDto(
                List.of(
                        new RequestGroupOwnershipChangesDto(UUID.randomUUID(), 1),
                        new RequestGroupOwnershipChangesDto(UUID.randomUUID(), 1),
                        new RequestGroupOwnershipChangesDto(UUID.randomUUID(), 1)
                )
        );


        Mono<Void> result = groupMemberService.changesOwnerGroup(Mono.just(requestGroupsOwnershipChangesDto), jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ValidationException.class, throwable.getClass()))
                .verify();
        verify(groupService, times(0)).checkUserIsOwnerGroups(any(), any());
        verify(cacheService, times(0)).getValue(any(), any());
        verify(groupMemberRepository, times(0)).existsByGroupIdAndUserId(anyInt(), any());
        verify(groupMemberRepository, times(0)).deleteByUserIdAndGroupId(any(), anyInt());
        verify(groupMemberRepository, times(0)).updateOwnerGroup(any());
        verify(groupMemberRepository, times(0)).insertAllMembers(any(), any());
        verify(cacheService, times(0)).deleteValue(anyString());
        verify(kafkaService, times(0)).sendToTopic(anyString(), any(), any());
    }

}
