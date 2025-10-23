package com.subOne.user_service.controller;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group.request.RequestGroupDto;
import com.subOne.user_service.dto.group.request.RequestUpdateGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupsDto;
import com.subOne.user_service.entity.Group;
import com.subOne.user_service.mapper.MapperGroup;
import com.subOne.user_service.repository.group_repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

public class GroupControllerTest extends AbstractControllerTest{

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    private MapperGroup mapperGroup;

    @Autowired
    protected CacheService cacheService;


    private final String URI = "/api/v1/groups";

    protected Group group;

    private void checkGroup(ResponseGroupDto responseGroupDto){
        StepVerifier.create(groupRepository.findById(responseGroupDto.id()))
                .assertNext(group -> {
                    assertEquals(group.getUpdatedAt(), responseGroupDto.updatedAt());
                    assertEquals(group.getName(), responseGroupDto.name());
                    assertEquals(group.getOwnerId(), UUID.fromString(jwt.getSubject()));
                })
                .verifyComplete();
    }

    @BeforeEach
    void setUpForGroupTests(){
        StepVerifier.create(groupRepository.save(mapperGroup
                .requestGroupDtotoGroup(new RequestGroupDto("groupTests"), user.getUserId().toString())))
                .assertNext(group -> this.group = group)
                .verifyComplete();
    }

    @DisplayName("POST -> /api/v1/groups")
    @Test
    void createGroup_UserIsCreated_CorrectResult(){
        Flux<ResponseGroupDto> result = webTestClient
                .post()
                .uri(URI)
                .bodyValue(new RequestGroupDto("groupTests"))
                .exchange()
                .expectStatus().isCreated()
                .returnResult(ResponseGroupDto.class).getResponseBody();
        StepVerifier.create(result)
                .assertNext(this::checkGroup)
                .verifyComplete();
    }

    @DisplayName("GET -> /api/v1/groups/{groupId}")
    @Test
    void getGroupById_GroupIsCreated_CorrectResult(){
        Flux<ResponseGroupDto> result = webTestClient
                .get()
                .uri(URI + "/" + group.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseGroupDto.class).getResponseBody();
        StepVerifier.create(result)
                .assertNext(this::checkGroup)
                .verifyComplete();
    }

    @DisplayName("GET -> /api/v1/groups/owner/me?page=")
    @Test
    void getGroups_GroupsIsCreated_CorrectReturn(){
        groupRepository.save(mapperGroup
                .requestGroupDtotoGroup(new RequestGroupDto("groupTests"), user.getUserId().toString())).block();

        Flux<ResponseGroupsDto> result = webTestClient
                .get()
                .uri(URI + "/owner/me?page=" + 0)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseGroupsDto.class).getResponseBody();
        StepVerifier.create(result)
                .assertNext(
                        responseGroupsDto -> assertTrue(2 <= responseGroupsDto.groups().size())

                )
                .verifyComplete();
    }

    @DisplayName("GET -> /api/v1/groups/me?page=")
    @Test
    void getGroupsUserIsMember_UserIsNotMember_CorrectReturn(){
        Flux<ResponseGroupsDto> result = webTestClient
                .get()
                .uri(URI + "/me?page=" + 0)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseGroupsDto.class).getResponseBody();
        StepVerifier.create(result)
                .assertNext(
                        responseGroupsDto -> {
                            assertEquals(0, responseGroupsDto.groups().size());
                            responseGroupsDto.groups()
                                    .forEach(this::checkGroup);
                        }
                )
                .verifyComplete();
    }

    @DisplayName("PATCH -> /api/v1/groups/{groupId}")
    @Test
    void updateGroup_GroupIsFound_CorrectUpdate(){
        RequestUpdateGroupDto requestUpdateGroupDto = new RequestUpdateGroupDto("groupTestNewName");
        webTestClient
                .patch()
                .uri(URI + "/" + group.getId())
                .bodyValue(requestUpdateGroupDto)
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(groupRepository.findById(group.getId()))
                .assertNext(group -> assertEquals(requestUpdateGroupDto.name(), group.getName()))
                .verifyComplete();
    }

    @DisplayName("PATCH -> /api/v1/groups/{groupId} group is not found")
    @Test
    void updateGroup_GroupIsNotFound_NotCorrectUpdate(){
        RequestUpdateGroupDto requestUpdateGroupDto = new RequestUpdateGroupDto("groupTestNewName");
        webTestClient
                .patch()
                .uri(URI + "/" + 1000)
                .bodyValue(requestUpdateGroupDto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @DisplayName("PATCH -> /api/v1/groups/{groupId} access denied")
    @Test
    void updateGroup_GroupIsFoundUserIsNotOwner_NotCorrectUpdate(){
        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID())
                .build();

        RequestUpdateGroupDto requestUpdateGroupDto = new RequestUpdateGroupDto("groupTestNewName");
        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .patch()
                .uri(URI + "/" + group.getId())
                .bodyValue(requestUpdateGroupDto)
                .exchange()
                .expectStatus().isForbidden();
    }


    @DisplayName("DELETE -> /api/v1/groups/{groupId}")
    @Test
    void deleteGroup_GroupIsFound_CorrectDelete(){
        cacheService.saveValue("OWNER::" + group.getId(), user.getUserId(), Duration.ofMinutes(10)).block();
        webTestClient
                .delete()
                .uri(URI + "/" + group.getId())
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(groupRepository.findById(group.getId()))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(cacheService.getValue("OWNER::" + group.getId(), UUID.class))
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("DELETE -> /api/v1/groups/{groupId} group is not found")
    @Test
    void deleteGroup_GroupIsNotFound_NotCorrectDelete(){
        webTestClient
                .delete()
                .uri(URI + "/" + 1000)
                .exchange()
                .expectStatus().isNotFound();
    }
}
