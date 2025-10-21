package com.subOne.user_service.controller;

import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.entity.GroupMember;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.repository.group_member_repository.GroupMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class GroupMemberControllerTest extends GroupControllerTest{

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private final String requestMapping = "/api/v1/groups/";

    private GroupMember groupMember;

    private User userMember;


    @BeforeEach
    void setUpGroupMember(){
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

        StepVerifier.create(groupMemberRepository.save(groupMember))
                .assertNext(gm -> this.groupMember = gm)
                .verifyComplete();
    }

    @Test
    @DisplayName("ПРОВЕРКА GET -> /api/v1/groups/{groupId}/members")
    void getMembers_GroupIsFound_CorrectReturn(){

        Flux<ResponseMembersDto> result = webTestClient
                .get()
                .uri(requestMapping + group.getId()  + "/members")
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
    @DisplayName("ПРОВЕРКА GET -> /api/v1/groups/{groupId}/members (group is not found)")
    void getMembers_GroupIsNotFound_NotCorrectReturn(){

        webTestClient
                .get()
                .uri(requestMapping + 1000  + "/members")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("ПРОВЕРКА DELETE -> /api/v1/groups/{groupId}/members/{userId}")
    void deleteMember_UserIsFoundAndGroupIsFound(){
        webTestClient
                .delete()
                .uri(requestMapping + group.getId() + "/members/" + groupMember.getUserId())
                .exchange()
                .expectStatus().isNoContent();
        StepVerifier.create(groupMemberRepository.findById(groupMember.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("ПРОВЕРКА DELETE -> /api/v1/groups/{groupId}/members/{userId} (group is not found)")
    void deleteMember_UserIsFoundAndGroupIsNotFound(){
        webTestClient
                .delete()
                .uri(requestMapping + 1000 + "/members/" + groupMember.getUserId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("ПРОВЕРКА DELETE -> /api/v1/groups/{groupId}/members/{userId} (user is not found)")
    void deleteMember_UserIsNotFoundAndGroupIsFound(){
        webTestClient
                .delete()
                .uri(requestMapping + group.getId() + "/members/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }
}
