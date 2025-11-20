package com.subOne.user_service.service_impl;

import com.subOne.kafka_dto.SendNotificationDto;
import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;
import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.dto.user.request.RequestGroupOwnershipChangesDto;
import com.subOne.user_service.dto.user.request.RequestGroupsOwnershipChangesDto;
import com.subOne.user_service.exception.ConflictException;
import com.subOne.user_service.kafka.serviceProducer.KafkaService;
import com.subOne.user_service.mapper.MapperGroupMember;
import com.subOne.user_service.repository.group_member_repository.GroupMemberRepository;
import com.subOne.user_service.service.GroupMemberService;
import com.subOne.user_service.service.GroupService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMemberServiceImpl implements GroupMemberService {

    private final MapperGroupMember mapperGroupMember;

    private final GroupService groupService;

    private final CacheService cacheService;

    private final GroupMemberRepository groupMemberRepository;

    private final KafkaService kafkaService;



    @Override
    public Mono<ResponseMembersDto> addUser(UUID userId, Long groupId) {
        return groupMemberRepository.findExistUserInGroupAndCountMemberInGroup(userId, groupId)
                .flatMap( dto -> {
                    if (dto.exist()) return Mono.error(new ConflictException("User is already a member of the group", Map.of()));
                    if (dto.count() >= 5) return Mono.error(new ConflictException("There can be no more than 5 members of the group (the owner is not considered)", Map.of()));
                    return Mono.empty();
                })
                .then(Mono.defer(() -> groupMemberRepository.save(mapperGroupMember.userIdAndGroupIdToGroupMember(userId, groupId))))
                .then(Mono.defer(() -> getUsersAndOwner(groupId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseMembersDto> getUsers(Long groupId, Jwt jwt) {
        return checkUserInGroup(groupId, jwt)
                .then(Mono.defer(() -> getUsersAndOwner(groupId)));
    }

    @Override
    @Transactional
    public Mono<Void> deleteMember(Long groupId, UUID userId, Jwt jwt) {
        return Mono.just(jwt.getSubject())
                .map(UUID::fromString)
                .flatMap(jwtUserId -> {
                    if (jwtUserId.equals(userId)) {
                        return Mono.error(new AccessDeniedException("The user cannot delete himself"));
                    }
                    return groupService.checkUserIsOwner(groupId, jwt)
                            .then(groupMemberRepository.deleteByUserIdAndGroupId(userId, groupId)
                                    .flatMap(count -> {
                                        if (count == 0)
                                            return Mono.error(new NoSuchElementException("User is not found"));
                                        return Mono.empty();
                                    }));
                    })
                .then(Mono.defer( () -> cacheService.deleteValue("MEMBER::" + userId + ' ' + groupId)))
                .then(Mono.defer(() -> kafkaService.sendToTopic("notification_user", userId, new SendNotificationDto(NotificationType.DELETE_MEMBER, null, null))));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Boolean> checkUserInGroup(Long groupId, Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String memberKey = "MEMBER::" + userId + ' ' + groupId;
        String ownerKey = "OWNER::" + groupId;
        return cacheService.getValue(memberKey, Boolean.class)
                .switchIfEmpty(
                        Mono.defer( () -> cacheService.getValue(ownerKey, UUID.class)
                                .map(ownerId -> ownerId.equals(userId))
                                .switchIfEmpty(Mono.just(false)))
                )
                .flatMap(isMemberOrOwner -> {
                    if (isMemberOrOwner) return Mono.just(true);
                    return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)
                            .flatMap(exists -> {
                                if (exists) {
                                    return cacheService.saveValue(memberKey, true, Duration.ofMinutes(15))
                                            .thenReturn(true);
                                }
                                return groupService.checkUserIsOwnerWithoutCacheGet(groupId, jwt);
                            });
                });
    }

    @Override
    public Mono<Boolean> checkUserIsOwnerGroup(Long groupId, Jwt jwt) {
        return groupService.checkUserIsOwner(groupId, jwt);
    }

    @Override
    public Mono<Void> existsMemberInGroupByOwnerId(Jwt jwt) {
        return groupMemberRepository.findGroupWhereExistsUserByOwnerId(UUID.fromString(jwt.getSubject()))
                .collectList()
                .flatMap(groupsId -> groupsId.isEmpty() ? Mono.empty(): Mono.error(new ConflictException("Group has members and cannot be deleted", Map.of("groupsId", groupsId))))
                .then();
    }

    @Override
    @Transactional
    public Mono<Void> changesOwnerGroup(Mono<RequestGroupsOwnershipChangesDto> requestGroupsOwnershipChangesDtoMono, Jwt jwt) {
        return requestGroupsOwnershipChangesDtoMono
                .flatMap(dtos -> {
                    List<RequestGroupOwnershipChangesDto> dto = dtos.groupOwnershipChanges();
                    if(dto.size() == dto.stream().map(RequestGroupOwnershipChangesDto::groupId).collect(Collectors.toSet()).size()){
                        return Mono.just(dto);
                    }
                    return Mono.error(new ValidationException("The group IDs are not unique or empty"));
                })
                .flatMap(dto ->
                        groupService.checkUserIsOwnerGroups(dto.stream().map(RequestGroupOwnershipChangesDto::groupId).toList(), jwt)
                                    .thenReturn(dto))
                .flatMapMany(Flux::fromIterable)
                .flatMap(dto ->
                        checkUserIsMemberGroup(dto.userId(), dto.groupId()))
                .collectList()
                .flatMap(dtos ->
                        groupMemberRepository.deleteAllByUserGroupPairs(dtos.stream().map(dto -> Tuples.of(dto.userId(), dto.groupId())).toList())
                                .thenReturn(dtos))
                .flatMap(ls -> groupMemberRepository.updateOwnerGroup(ls)
                        .then(groupMemberRepository.insertAllMembers(ls.stream().map(RequestGroupOwnershipChangesDto::groupId).toList(),UUID.fromString(jwt.getSubject())))
                        .thenReturn(ls))
                .flatMapMany(Flux::fromIterable)
                .flatMap(dto -> {
                        kafkaService.sendToTopic("notification_user", dto.userId(), new SendNotificationDto(NotificationType.CHANGE_OWNER, NotificationTargetType.GROUP, dto.groupId())).subscribe();
                        return cacheService.deleteValue("MEMBER::" + dto.userId() + ' ' + dto.groupId()).then(cacheService.deleteValue("OWNER::" + dto.groupId()));
                })
                .then();
    }

    private Mono<RequestGroupOwnershipChangesDto> checkUserIsMemberGroup(UUID userId, Long groupId){
        String memberKey = "MEMBER::" + userId + ' ' + groupId;
        return cacheService.getValue(memberKey, Boolean.class)
                .switchIfEmpty(Mono.just(Boolean.FALSE))
                .flatMap(isMember -> {
                    if (isMember) return Mono.just(new RequestGroupOwnershipChangesDto(userId, groupId));
                    return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.just(new RequestGroupOwnershipChangesDto(userId, groupId));
                                }
                                return Mono.error(new NoSuchElementException("Member: " + userId + " is not found"));
                            });
                });
    }

    private Mono<ResponseMembersDto> getUsersAndOwner(Long groupId){
        return groupService.getOwner(groupId).flatMap( ownerId -> {
            Flux<ResponseMemberDto> members = groupMemberRepository.findMembersIdByGroupId(groupId).map(id -> new ResponseMemberDto(id, false));
            return Flux.concat(
                    Flux.just(new ResponseMemberDto(ownerId, true)),
                    members
            ).collectList().map(ResponseMembersDto::new);
        });
    }

}
