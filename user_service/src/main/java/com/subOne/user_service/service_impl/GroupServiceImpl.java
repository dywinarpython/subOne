package com.subOne.user_service.service_impl;

import com.subOne.user_service.dto.group.request.RequestGroupDto;
import com.subOne.user_service.dto.group.request.RequestUpdateGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupsDto;
import com.subOne.user_service.mapper.MapperGroup;
import com.subOne.user_service.repository.GroupRepository;
import com.subOne.user_service.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    private final MapperGroup mapperGroup;

    @Override
    @Transactional
    public Mono<ResponseGroupDto> saveGroup(Mono<RequestGroupDto> requestGroupDtoMono, Jwt jwt) {
        return requestGroupDtoMono
                .map(requestGroupDto -> mapperGroup.requestGroupDtotoGroup(requestGroupDto, jwt.getSubject()))
                .flatMap(groupRepository::save).map(mapperGroup::groupToResponseGroupDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseGroupDto> getGroupById(Long groupId, Jwt jwt) {
        return groupRepository.findGroupById(groupId, UUID.fromString(jwt.getSubject()))
                .switchIfEmpty(checkRights(groupId));
    }

    @Override
    public Mono<ResponseGroupsDto> getGroups(Jwt jwt) {
        return groupRepository.findByOwnerId(UUID.fromString(jwt.getSubject())).collectList().map(ResponseGroupsDto::new);
    }

    @Override
    @Transactional
    public Mono<Void> updateGroup(Mono<RequestUpdateGroupDto> requestGroupDtoMono, Jwt jwt) {
        return requestGroupDtoMono.flatMap(requestUpdateGroupDto ->
            groupRepository.updateGroupByIdAndOwnerId(requestUpdateGroupDto.groupId(), requestUpdateGroupDto.name(), UUID.fromString(jwt.getSubject()))
                    .flatMap(count -> {
                        if(count != 1) return checkRights(requestUpdateGroupDto.groupId()).then();
                        return Mono.empty();
                    }));
    }

    @Override
    @Transactional
    // TODO при удалении группы обязательно в дальнейшем требуется удаления всех подписок
    public Mono<Void> deleteGroup(Long groupId, Jwt jwt) {
        return groupRepository.deleteByIdAndOwnerId(groupId, UUID.fromString(jwt.getSubject())).flatMap(count ->{
           if(count != 1) return checkRights(groupId).then();
           return Mono.empty();}
        );
    }

    @Override
    // TODO добавить кеширование
    public Mono<Void> checkUserIsOwner(Long groupId, Jwt jwt) {
        return groupRepository.existsByIdAndOwnerId(groupId, UUID.fromString(jwt.getSubject())).flatMap(
                exists -> {
                    if(exists) return Mono.empty();
                    return checkRights(groupId).then();}
        );
    }

    private Mono<ResponseGroupDto> checkRights(Long groupId) {
        return groupRepository.existsById(groupId)
                .flatMap(exists -> {
                    if (exists) return Mono.error(new AccessDeniedException("Access is denied"));
                    else return Mono.error(new NoSuchElementException("Group is not found!"));
                });
    }
}
