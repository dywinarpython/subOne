package com.subOne.user_service.controller;

import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.dto.user.request.RequestGroupOwnershipChangesDto;
import com.subOne.user_service.dto.user.request.RequestGroupsOwnershipChangesDto;
import com.subOne.user_service.entity.GroupMember;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.repository.group_member_repository.GroupMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

public class GroupMemberControllerTest extends GroupControllerTest{

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private final String URI = "/api/v1/groups/";

    private GroupMember groupMember;

    private User userMember;

    private GroupMember generateGroupMember(){
        User user1 = new User();
        user1.setEmail("email" + System.currentTimeMillis() + "@mail.com");
        user1.setName("name");
        user1.setSurname("surname");
        user1.setVerifyEmail(true);
        user1.setUserId(UUID.randomUUID());
        StepVerifier.create(userRepository.save(user1))
                .assertNext(userSave -> this.userMember = userSave)
                .verifyComplete();
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(group.getId());
        groupMember.setUserId(userMember.getUserId());
        return groupMember;
    }


    @BeforeEach
    void setUpGroupMember(){
        StepVerifier.create(groupMemberRepository.save(generateGroupMember()))
                .assertNext(gm -> this.groupMember = gm)
                .verifyComplete();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/members")
    void getMembers_GroupIsFound_CorrectReturn(){

        Flux<ResponseMembersDto> result = webTestClient
                .get()
                .uri(URI + group.getId()  + "/members")
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseMembersDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(responseMembersDto -> {
                   assertEquals(2, responseMembersDto.users().size());
                   List<ResponseMemberDto> users = responseMembersDto.users();
                   assertEquals(users.getFirst().user().userId(), UUID.fromString(jwt.getSubject()));
                   assertEquals(true, users.getFirst().owner());
                   assertEquals(users.get(1).user().userId(), groupMember.getUserId());
                   assertEquals(false, users.get(1).owner());
                });
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/members (group is not found)")
    void getMembers_GroupIsNotFound_NotCorrectReturn(){
        webTestClient
                .get()
                .uri(URI + (int) System.currentTimeMillis()  + "/members")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/members/check (user is owner)")
    void checkUserInGroup_GroupFoundUserOwnerGroup_CorrectReturn(){
        webTestClient.get()
                .uri(URI + group.getId() + "/members/check")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/members/check (user is member)")
    void checkUserInGroup_GroupFoundUserMemberGroup_CorrectReturn(){
        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", groupMember.getUserId())
                .build();

        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri(URI + groupMember.getGroupId() + "/members/check")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/members/check (group not found)")
    void checkUserInGroup_GroupNotFound_CorrectReturn(){
        webTestClient.get()
                .uri(URI + (int) System.currentTimeMillis() + "/members/check")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/members/check/owner (user is owner)")
    void checkUserIsOwnerGroup_GroupFoundUserOwnerGroup_CorrectReturn(){
        webTestClient.get()
                .uri(URI + group.getId() + "/members/check/owner")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/members/check/owner (user is member)")
    void checkUserIsOwnerGroup_GroupFoundUserMemberGroup_CorrectReturn(){
        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", userMember.getUserId())
                .build();

        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri(URI + group.getId() + "/members/check/owner")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/members/check/owner (group not found)")
    void checkUserIsOwnerGroup_GroupNotFound_CorrectReturn(){
        webTestClient.get()
                .uri(URI + (int) System.currentTimeMillis() + "/members/check/owner")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PATCH -> /api/v1/groups/change-owners")
    void changesOwnerGroups_UsersMembersAndFetchOwner_CorrectReturnAndCheckRepo(){
        setUpForGroupTests();
        GroupMember groupMember2 = generateGroupMember();
        groupMemberRepository.save(groupMember2).block();
        RequestGroupsOwnershipChangesDto requestGroupsOwnershipChangesDto = new RequestGroupsOwnershipChangesDto(
                List.of(
                   new RequestGroupOwnershipChangesDto(groupMember.getUserId(), groupMember.getGroupId()),
                   new RequestGroupOwnershipChangesDto(groupMember2.getUserId(), groupMember2.getGroupId())
                )
        );
        StepVerifier.create(cacheService.saveValue("OWNER::" + groupMember.getId(), user.getUserId(), Duration.ofMinutes(10)))
                .verifyComplete();
        StepVerifier.create(cacheService.saveValue("OWNER::" + groupMember2.getId(), user.getUserId(), Duration.ofMinutes(10)))
                .verifyComplete();
        StepVerifier.create(cacheService.saveValue("MEMBER::" + groupMember.getUserId() + ' ' +  groupMember.getId(), Boolean.TRUE, Duration.ofMinutes(10)))
                 .verifyComplete();
        StepVerifier.create(cacheService.saveValue("MEMBER::" + groupMember2.getUserId() + ' ' +  groupMember2.getId(), Boolean.TRUE, Duration.ofMinutes(10)))
                .verifyComplete();

        webTestClient
                .patch()
                .uri(URI + "change-owners")
                .bodyValue(requestGroupsOwnershipChangesDto)
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(groupMemberRepository.existsByGroupIdAndUserId(groupMember.getGroupId(), user.getUserId()))
                .expectNext(Boolean.TRUE)
                .verifyComplete();
        StepVerifier.create(groupMemberRepository.existsByGroupIdAndUserId(groupMember2.getGroupId(), user.getUserId()))
                .expectNext(Boolean.TRUE)
                .verifyComplete();
        StepVerifier.create(groupRepository.existsByIdAndOwnerId(groupMember.getGroupId(), groupMember.getUserId()))
                .expectNext(Boolean.TRUE)
                .verifyComplete();
        StepVerifier.create(groupRepository.existsByIdAndOwnerId(groupMember2.getGroupId(), groupMember2.getUserId()))
                .expectNext(Boolean.TRUE)
                .verifyComplete();
        StepVerifier.create(cacheService.getValue("OWNER::" + groupMember.getGroupId(), UUID.class))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(cacheService.getValue("OWNER::" + groupMember2.getGroupId(), UUID.class))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(cacheService.getValue("MEMBER::" + groupMember.getUserId() + ' ' +  groupMember.getGroupId(), Boolean.class))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(cacheService.getValue("MEMBER::" + groupMember2.getUserId() + ' ' +  groupMember2.getGroupId(), Boolean.class))
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    @DisplayName("DELETE -> /api/v1/groups/{groupId}/members/{userId}")
    void deleteMember_UserIsFoundAndGroupIsFound(){
        StepVerifier.create(cacheService.saveValue("MEMBER::" + groupMember.getUserId() + ' ' + group.getId(), true, Duration.ofMinutes(10)))
                .verifyComplete();

        webTestClient
                .delete()
                .uri(URI + group.getId() + "/members/" + groupMember.getUserId())
                .exchange()
                .expectStatus().isNoContent();
        StepVerifier.create(groupMemberRepository.findById(groupMember.getId()))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(cacheService.getValue("MEMBER::" + groupMember.getUserId() + ' ' + group.getId(), Boolean.class))
                .expectNextCount(0)
                .verifyComplete();

    }



    @Test
    @DisplayName("DELETE -> /api/v1/groups/{groupId}/members/{userId} (group is not found)")
    void deleteMember_UserIsFoundAndGroupIsNotFound(){
        webTestClient
                .delete()
                .uri(URI + 1000 + "/members/" + groupMember.getUserId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE -> /api/v1/groups/{groupId}/members/{userId} (user is not found)")
    void deleteMember_UserIsNotFoundAndGroupIsFound(){
        webTestClient
                .delete()
                .uri(URI + group.getId() + "/members/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }
}
