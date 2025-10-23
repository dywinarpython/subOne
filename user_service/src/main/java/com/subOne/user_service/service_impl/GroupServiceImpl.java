package com.subOne.user_service.service_impl;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group.request.RequestGroupDto;
import com.subOne.user_service.dto.group.request.RequestUpdateGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupsDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.kafka.serviceProducer.KafkaService;
import com.subOne.user_service.mapper.MapperGroup;
import com.subOne.user_service.repository.group_repository.GroupRepository;
import com.subOne.user_service.service.GroupService;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    private final MapperGroup mapperGroup;

    private final CacheService cacheService;

    private final KafkaService kafkaService;

    private final Integer pageSize;

    public GroupServiceImpl(GroupRepository groupRepository,
                            MapperGroup mapperGroup,
                            CacheService cacheService, KafkaService kafkaService,
                            @Value("${spring.page.size}") Integer pageSize) {
        this.groupRepository = groupRepository;
        this.mapperGroup = mapperGroup;
        this.cacheService = cacheService;
        this.kafkaService = kafkaService;
        this.pageSize = pageSize;
    }


    @Override
    @Transactional
    public Mono<ResponseGroupDto> saveGroup(Mono<RequestGroupDto> requestGroupDtoMono, Jwt jwt) {
        return groupRepository.findCountByOwnerId(UUID.fromString(jwt.getSubject()))
                .flatMap( count -> {
                 if(count >= 5) return Mono.error(new ValidationException("The maximum number of groups is 5"));
                 return requestGroupDtoMono
                .map(requestGroupDto -> mapperGroup.requestGroupDtotoGroup(requestGroupDto, jwt.getSubject()))
                .flatMap(groupRepository::save).map(mapperGroup::groupToResponseGroupDto);});
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseGroupDto> getGroupById(Long groupId, Jwt jwt) {
        return groupRepository.findGroupById(groupId, UUID.fromString(jwt.getSubject()))
                .switchIfEmpty(Mono.defer(() -> checkRights(groupId)));
    }

    @Override
    public Mono<ResponseUserDto> getOwner(Long groupId) {
        return groupRepository.selectOwnerByGroupId(groupId);
    }

    @Override
    public Mono<ResponseGroupsDto> getGroupsCreateUser(Jwt jwt, Integer page) {
        return groupRepository.findByOwnerId(UUID.fromString(jwt.getSubject()), PageRequest.of(page, pageSize)).collectList().map(ResponseGroupsDto::new);
    }

    @Override
    public Mono<ResponseGroupsDto> getGroupsUserIsMember(Jwt jwt, Integer page) {
        return groupRepository.findGroupsUserIsMember(UUID.fromString(jwt.getSubject()), page*pageSize, pageSize)
                .collectList()
                .map(ResponseGroupsDto::new);
    }

    @Override
    @Transactional
    public Mono<Void> updateGroup(Mono<RequestUpdateGroupDto> requestGroupDtoMono, Long groupId, Jwt jwt) {
        return requestGroupDtoMono.flatMap(requestUpdateGroupDto ->
            groupRepository.updateGroupByIdAndOwnerId(groupId, requestUpdateGroupDto.name(), UUID.fromString(jwt.getSubject()))
                    .flatMap(count -> {
                        if(count != 1) return checkRights(groupId).then();
                        return Mono.empty();
                    }));
    }

    @Override
    @Transactional
    public Mono<Void> deleteGroup(Long groupId, Jwt jwt) {
        return groupRepository.deleteByIdAndOwnerId(groupId, UUID.fromString(jwt.getSubject())).flatMap(count -> {
           if(count != 1) return checkRights(groupId).then();
           return cacheService.deleteValue("OWNER::" + groupId);}
        ).then(Mono.defer( () -> kafkaService.sendToTopic("delete_group", groupId)));
    }

    @Override
    public Mono<Boolean> checkUserIsOwner(Long groupId, Jwt jwt) {
        return cacheService.getValue("OWNER::" + groupId, UUID.class)
                .flatMap(userId ->
                        userId.equals(UUID.fromString(jwt.getSubject())) ? Mono.just(true) : Mono.empty())
                .switchIfEmpty(Mono.defer(() ->
                    groupRepository.existsByIdAndOwnerId(groupId, UUID.fromString(jwt.getSubject())).flatMap(
                            exists -> {
                                if(exists) {
                                    return cacheService.saveValue("OWNER::" + groupId, UUID.fromString(jwt.getSubject()), Duration.ofMinutes(30)).thenReturn(true);
                                }
                                return checkRights(groupId).thenReturn(false);
                            })
                ));
    }

    @Override
    public Mono<Void> checkUserIsOwnerGroups(List<Long> groupsId, Jwt jwt) {
        return groupRepository.findCountWhereUserIsOwnerByGroupsId(UUID.fromString(jwt.getSubject()), groupsId)
                .flatMap(count -> count == groupsId.size()? Mono.empty(): Mono.error(new AccessDeniedException("Access is denied")));
    }

    @Override
    public Mono<Boolean> checkUserIsOwnerWithoutCacheGet(Long groupId, Jwt jwt) {
        return groupRepository.existsByIdAndOwnerId(groupId, UUID.fromString(jwt.getSubject())).flatMap(
                exists -> {
                    if(exists) {
                        return cacheService.saveValue("OWNER::" + groupId, UUID.fromString(jwt.getSubject()), Duration.ofMinutes(30)).thenReturn(true);
                    }
                    return checkRights(groupId).thenReturn(false);
                });
    }

    @Override
    public Mono<Void> deleteDataRelatedGroupsByOwnerId(Jwt jwt) {
        return groupRepository.findGroupsIdByOwnerId(UUID.fromString(jwt.getSubject()))
                .flatMap(groupId ->
                    kafkaService.sendToTopic("delete_group" , groupId).then(cacheService.deleteValue("OWNER::" + groupId))
                ).then();
    }


    private Mono<ResponseGroupDto> checkRights(Long groupId) {
        return groupRepository.existsById(groupId)
                .flatMap(exists -> {
                    if (exists) return Mono.error(new AccessDeniedException("Access is denied"));
                    else return Mono.error(new NoSuchElementException("Group is not found!"));
                });
    }
}
