package com.subOne.user_service.service;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group.request.RequestGroupDto;
import com.subOne.user_service.dto.group.request.RequestUpdateGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupOwnerIdDto;
import com.subOne.user_service.dto.group.response.ResponseGroupsDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.Group;
import com.subOne.user_service.kafka.serviceProducer.KafkaService;
import com.subOne.user_service.mapper.MapperGroup;
import com.subOne.user_service.repository.group_repository.GroupRepository;
import com.subOne.user_service.service_impl.GroupServiceImpl;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private Jwt jwt;

    @Mock
    private MapperGroup mapperGroup;

    @Mock
    private CacheService cacheService;

    @Mock
    private KafkaService kafkaService;

    private GroupServiceImpl groupService;

    @BeforeEach
    void setUp(){
        groupService = new GroupServiceImpl(groupRepository, mapperGroup, cacheService, kafkaService, 10);
    }

    @Test
    void saveGroup_GroupInfoCorrect_CorrectSaveAndCheckRepo() {
        RequestGroupDto requestDto = new RequestGroupDto("groupTest");
        Mono<RequestGroupDto> requestMono = Mono.just(requestDto);
        Group mappedGroup = new Group();
        mappedGroup.setName("groupTest");
        mappedGroup.setOwnerId(UUID.randomUUID());
        ResponseGroupDto responseGroupDto = new ResponseGroupDto(
                1, "groupTest", OffsetDateTime.now(), null
        );

        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(mapperGroup.requestGroupDtotoGroup(any(), anyString()))
                .thenReturn(mappedGroup);
        when(groupRepository.save(any())).thenReturn(Mono.just(mappedGroup));
        when(mapperGroup.groupToResponseGroupDto(any())).thenReturn(responseGroupDto);
        when(groupRepository.findCountByOwnerId(any())).thenReturn(Mono.just(0));

        Mono<ResponseGroupDto> result = groupService.saveGroup(requestMono, jwt);

        StepVerifier.create(result)
                .expectNext(responseGroupDto)
                .verifyComplete();

        verify(groupRepository).findCountByOwnerId(any());
        verify(groupRepository).save(mappedGroup);
    }

    @Test
    void saveGroup_UserHaveFiveGroup_CorrectSaveAndCheckRepo() {
        RequestGroupDto requestDto = new RequestGroupDto("groupTest");
        Mono<RequestGroupDto> requestMono = Mono.just(requestDto);
        Group mappedGroup = new Group();
        mappedGroup.setName("groupTest");
        mappedGroup.setOwnerId(UUID.randomUUID());
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.findCountByOwnerId(any())).thenReturn(Mono.just(5));

        Mono<ResponseGroupDto> result = groupService.saveGroup(requestMono, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ValidationException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).findCountByOwnerId(any());
        verify(groupRepository, times(0)).save(mappedGroup);
    }

    @Test
    void getGroupById_GroupIsFoundAndUserIsMember_CorrectReturnAndCheckRepo(){
        ResponseGroupDto responseGroupDto = new ResponseGroupDto(1,  "name", OffsetDateTime.now(), null);

        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.findGroupById(any(), any())).thenReturn(Mono.just(responseGroupDto));

        Mono<ResponseGroupDto> result = groupService.getGroupById(1, jwt);

        StepVerifier.create(result)
                .expectNext(responseGroupDto)
                .verifyComplete();

        verify(groupRepository).findGroupById(any(), any());
        verify(groupRepository, times(0)).existsById(anyInt());
    }

    @Test
    void getGroupById_GroupIsFoundAndUserIsNotMember_NotCorrectReturnAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.findGroupById(any(), any())).thenReturn(Mono.empty());
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(true));

        Mono<ResponseGroupDto> result = groupService.getGroupById(1, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(AccessDeniedException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).findGroupById(any(), any());
        verify(groupRepository).existsById(anyInt());
    }

    @Test
    void getGroupById_GroupIsNotFound_NotCorrectReturnAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.findGroupById(any(), any())).thenReturn(Mono.empty());
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(false));

        Mono<ResponseGroupDto> result = groupService.getGroupById(1, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).findGroupById(any(), any());
        verify(groupRepository).existsById(anyInt());
    }

    @Test
    void getOwner_GroupIsFound_CorrectReturnAndCheckRepo(){
        ResponseUserDto responseUserDto = new ResponseUserDto(UUID.randomUUID(), "testName", "surname", "email");
        when(groupRepository.selectOwnerByGroupId(anyInt())).thenReturn(Mono.just(responseUserDto));

        Mono<ResponseUserDto> result = groupService.getOwner(anyInt());

        StepVerifier.create(result)
                .expectNext(responseUserDto)
                .verifyComplete();

        verify(groupRepository).selectOwnerByGroupId(anyInt());
    }

    @Test
    void getOwnerId_GroupFound_CorrectReturnAndCheckRepo(){
        ResponseGroupOwnerIdDto userDto = new ResponseGroupOwnerIdDto(UUID.randomUUID());
        when(groupRepository.findOwnerIdByGroupId(anyInt())).thenReturn(Mono.just(userDto.ownerId()));

        Mono<ResponseGroupOwnerIdDto> result = groupService.getOwnerId(0);

        StepVerifier.create(result)
                .expectNext(userDto)
                .verifyComplete();

        verify(groupRepository).findOwnerIdByGroupId(anyInt());
    }

    @Test
    void getOwnerId_GroupNotFound_CorrectReturnAndCheckRepo(){
        when(groupRepository.findOwnerIdByGroupId(anyInt())).thenReturn(Mono.empty());

        Mono<ResponseGroupOwnerIdDto> result = groupService.getOwnerId(0);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();

        verify(groupRepository).findOwnerIdByGroupId(anyInt());
    }

    @Test
    void getGroupsCreateUser_GroupOneIsCreate_CorrectReturnAndCheckRepo(){
        ResponseGroupsDto responseGroupsDto = new ResponseGroupsDto(
                List.of(new ResponseGroupDto(1, "testName", OffsetDateTime.now(), OffsetDateTime.now()))
        );
        when(groupRepository.findByOwnerId(any())).thenReturn(Flux.fromIterable(responseGroupsDto.groups()));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());

        Mono<ResponseGroupsDto> result = groupService.getGroupsCreateUser(jwt);

        StepVerifier.create(result)
                .expectNext(responseGroupsDto)
                .verifyComplete();

        verify(groupRepository).findByOwnerId(any());
    }

    @Test
    void getGroupsUserIsMember_UserIsMember_CorrectReturnAndCheckRepo(){
        ResponseGroupsDto responseGroupsDto = new ResponseGroupsDto(
                List.of(new ResponseGroupDto(1, "testName", OffsetDateTime.now(), OffsetDateTime.now()))
        );
        when(groupRepository.findGroupsUserIsMember(any(), anyInt(), anyInt())).thenReturn(Flux.fromIterable(responseGroupsDto.groups()));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());

        Mono<ResponseGroupsDto> result = groupService.getGroupsUserIsMember(jwt, 0);

        StepVerifier.create(result)
                .expectNext(responseGroupsDto)
                .verifyComplete();

        verify(groupRepository).findGroupsUserIsMember(any(), anyInt(), anyInt());
    }

    @Test
    void getCountGroup_GroupsFound_CorrectReturn(){
        when(groupRepository.findCountByOwnerId(any())).thenReturn(Mono.just(3));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());

        Mono<Integer> result = groupService.getCountGroup(jwt);

        StepVerifier.create(result)
                .expectNext(3)
                .verifyComplete();
        verify(groupRepository).findCountByOwnerId(any());
    }


    @Test
    void updateGroup_GroupIsFoundAndUserIsOwner_CorrectUpdateAndCheckRepo(){
        Mono<RequestUpdateGroupDto> requestUpdateGroupDtoMono = Mono.just(new RequestUpdateGroupDto("name"));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.updateGroupByIdAndOwnerId(anyInt(), anyString(), any()))
                .thenReturn(Mono.just(1));

        Mono<Void> result = groupService.updateGroup(requestUpdateGroupDtoMono, 2, jwt);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(groupRepository).updateGroupByIdAndOwnerId(anyInt(), anyString(), any());
        verify(groupRepository, times(0)).existsById(anyInt());
    }

    @Test
    void updateGroup_GroupIsFoundAndUserIsNoOwner_NoCorrectUpdateAndCheckRepo(){
        Mono<RequestUpdateGroupDto> requestUpdateGroupDtoMono = Mono.just(new RequestUpdateGroupDto("name"));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.updateGroupByIdAndOwnerId(anyInt(), anyString(), any()))
                .thenReturn(Mono.just(0));
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(true));

        Mono<Void> result = groupService.updateGroup(requestUpdateGroupDtoMono, 2, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(AccessDeniedException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).updateGroupByIdAndOwnerId(anyInt(), anyString(), any());
        verify(groupRepository).existsById(anyInt());
    }

    @Test
    void updateGroup_GroupIsNotFound_NoCorrectUpdateAndCheckRepo(){
        Mono<RequestUpdateGroupDto> requestUpdateGroupDtoMono = Mono.just(new RequestUpdateGroupDto("name"));
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.updateGroupByIdAndOwnerId(anyInt(), anyString(), any()))
                .thenReturn(Mono.just(0));
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(false));

        Mono<Void> result = groupService.updateGroup(requestUpdateGroupDtoMono, 2, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).updateGroupByIdAndOwnerId(anyInt(), anyString(), any());
        verify(groupRepository).existsById(anyInt());
    }

    @Test
    void deleteGroup_GroupIsFoundAndUserIsOwner_CorrectDeleteAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.deleteByIdAndOwnerId(anyInt(), any())).thenReturn(Mono.just(1));
        when(cacheService.deleteValue(anyString())).thenReturn(Mono.empty());
        when(kafkaService.sendToTopic(anyString(), anyInt())).thenReturn(Mono.empty());

        Mono<Void> result = groupService.deleteGroup( 2, jwt);

        StepVerifier.create(result)
                .verifyComplete();
        verify(groupRepository).deleteByIdAndOwnerId(anyInt(), any());
        verify(groupRepository, times(0)).existsById(anyInt());
        verify(cacheService).deleteValue(anyString());
        verify(kafkaService).sendToTopic(anyString(), anyInt());
    }

    @Test
    void deleteGroup_GroupIsFoundAndUserIsNoOwner_NoCorrectDeleteAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.deleteByIdAndOwnerId(anyInt(), any())).thenReturn(Mono.just(0));
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(true));

        Mono<Void> result = groupService.deleteGroup( 2, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(AccessDeniedException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).deleteByIdAndOwnerId(anyInt(), any());
        verify(groupRepository).existsById(anyInt());
        verify(cacheService, times(0)).deleteValue(anyString());
        verify(kafkaService, times(0)).sendToTopic(anyString(), anyInt());
    }

    @Test
    void deleteGroup_GroupIsNotFound_NoCorrectDeleteAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.deleteByIdAndOwnerId(anyInt(), any())).thenReturn(Mono.just(0));
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(false));

        Mono<Void> result = groupService.deleteGroup( 2, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).deleteByIdAndOwnerId(anyInt(), any());
        verify(groupRepository).existsById(anyInt());
        verify(cacheService, times(0)).deleteValue(anyString());
        verify(kafkaService, times(0)).sendToTopic(anyString(), anyInt());
    }

    @Test
    void checkUserIsOwner_GroupIsFoundAndUserIsOwner_CorrectResultAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.existsByIdAndOwnerId(anyInt(), any())).thenReturn(Mono.just(true));
        when(cacheService.saveValue(anyString(), any(), any())).thenReturn(Mono.empty());
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.empty());

        Mono<Boolean> result = groupService.checkUserIsOwner(1, jwt);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
        verify(groupRepository).existsByIdAndOwnerId(anyInt(), any());
        verify(groupRepository, times(0)).existsById(anyInt());
        verify(cacheService).saveValue(anyString(), any(), any());
        verify(cacheService).getValue(anyString(), any());
    }

    @Test
    void checkUserIsOwner_GroupIsFoundAndUserIsNoOwner_NoCorrectResultAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.existsByIdAndOwnerId(anyInt(), any())).thenReturn(Mono.just(false));
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(true));
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.empty());

        Mono<Boolean> result = groupService.checkUserIsOwner(1, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                assertEquals(AccessDeniedException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).existsByIdAndOwnerId(anyInt(), any());
        verify(groupRepository).existsById(anyInt());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
        verify(cacheService).getValue(anyString(), any());
    }

    @Test
    void checkUserIsOwner_GroupIsNotFound_NoCorrectResultAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.existsByIdAndOwnerId(anyInt(), any())).thenReturn(Mono.just(false));
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(false));
        when(cacheService.getValue(anyString(), any())).thenReturn(Mono.empty());

        Mono<Boolean> result = groupService.checkUserIsOwner(1, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).existsByIdAndOwnerId(anyInt(), any());
        verify(groupRepository).existsById(anyInt());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
        verify(cacheService).getValue(anyString(), any());
    }
    @Test
    void checkUserIsOwnerGroups_GroupsFoundAndUserIsOwnerGroups_CorrectCheckAndCheckRepo(){
        List<Integer> groupsId = List.of(1, 2, 3);
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.findCountWhereUserIsOwnerByGroupsId(any(), any())).thenReturn(Mono.just((long) groupsId.size()));

        Mono<Void> result = groupService.checkUserIsOwnerGroups(groupsId, jwt);

        StepVerifier.create(result)
                .verifyComplete();
        verify(groupRepository).findCountWhereUserIsOwnerByGroupsId(any(), any());
    }
    @Test
    void checkUserIsOwnerGroups_GroupsFoundAndUserIsOwnerNotAllGroups_NotCorrectCheckAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.findCountWhereUserIsOwnerByGroupsId(any(), any())).thenReturn(Mono.just(0L));

        Mono<Void> result = groupService.checkUserIsOwnerGroups(List.of(1, 2, 3), jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(AccessDeniedException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).findCountWhereUserIsOwnerByGroupsId(any(), any());
    }

    @Test
    void checkUserIsOwnerWithoutCacheGet_GroupIsFoundAndUserIsOwner_CorrectResultAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.existsByIdAndOwnerId(anyInt(), any())).thenReturn(Mono.just(true));
        when(cacheService.saveValue(anyString(), any(), any())).thenReturn(Mono.empty());

        Mono<Boolean> result = groupService.checkUserIsOwnerWithoutCacheGet(1, jwt);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
        verify(groupRepository).existsByIdAndOwnerId(anyInt(), any());
        verify(groupRepository, times(0)).existsById(anyInt());
        verify(cacheService).saveValue(anyString(), any(), any());
        verify(cacheService, times(0)).getValue(anyString(), any());
    }

    @Test
    void checkUserIsOwnerWithoutCacheGet_GroupIsFoundAndUserIsNoOwner_NoCorrectResultAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.existsByIdAndOwnerId(anyInt(), any())).thenReturn(Mono.just(false));
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(true));

        Mono<Boolean> result = groupService.checkUserIsOwnerWithoutCacheGet(1, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(AccessDeniedException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).existsByIdAndOwnerId(anyInt(), any());
        verify(groupRepository).existsById(anyInt());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
        verify(cacheService, times(0)).getValue(anyString(), any());
    }

    @Test
    void checkUserIsOwnerWithoutCacheGet_GroupIsNotFound_NoCorrectResultAndCheckRepo(){
        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(groupRepository.existsByIdAndOwnerId(anyInt(), any())).thenReturn(Mono.just(false));
        when(groupRepository.existsById(anyInt())).thenReturn(Mono.just(false));

        Mono<Boolean> result = groupService.checkUserIsOwnerWithoutCacheGet(1, jwt);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable ->
                        assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(groupRepository).existsByIdAndOwnerId(anyInt(), any());
        verify(groupRepository).existsById(anyInt());
        verify(cacheService, times(0)).saveValue(anyString(), any(), any());
        verify(cacheService, times(0)).getValue(anyString(), any());
    }

    @Test
    void deleteDataRelatedGroupsByOwnerId_GroupsFoundAndUserIsOwnerGroups_Correct(){
        List<Integer> groupsId = List.of(1, 2);
        when(kafkaService.sendToTopic(anyString(), anyInt())).thenReturn(Mono.empty());
        when(cacheService.deleteValue(anyString())).thenReturn(Mono.empty());

        Mono<Void> result = groupService.deleteDataRelatedGroupsByOwnerId(Flux.fromIterable(groupsId));

        StepVerifier.create(result)
                .verifyComplete();
        verify(kafkaService, times(groupsId.size())).sendToTopic(anyString(), anyInt());
        verify(cacheService, times(groupsId.size())).deleteValue(anyString());
    }

    @Test
    void deleteDataRelatedGroupsByOwnerId_GroupsNotFound_Correct(){
        Mono<Void> result = groupService.deleteDataRelatedGroupsByOwnerId(Flux.fromIterable(List.of()));

        StepVerifier.create(result)
                .verifyComplete();
        verify(kafkaService, times(0)).sendToTopic(anyString(), anyInt());
        verify(cacheService, times(0)).deleteValue(anyString());
    }
}
