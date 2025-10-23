package com.subOne.user_service.controller;

import com.subOne.user_service.dto.group.request.RequestGroupDto;
import com.subOne.user_service.dto.group_invite.response.ResponseInviteCodeDto;
import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.Group;
import com.subOne.user_service.entity.GroupInvite;
import com.subOne.user_service.entity.GroupMember;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.mapper.MapperGroup;
import com.subOne.user_service.repository.group_invite_repository.GroupInviteRepository;
import com.subOne.user_service.repository.group_member_repository.GroupMemberRepository;
import com.subOne.user_service.repository.group_repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

public class InviteControllerTest extends AbstractControllerTest{


    @Autowired
    private GroupInviteRepository groupInviteRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MapperGroup mapperGroup;

    private GroupInvite groupInvite;

    private Group group;

    private User userMember;


    private final String requestMapping = "/api/v1/invitations";

    @BeforeEach
    void setUpInvite(){
        User user1 = new User();
        user1.setEmail("email" + UUID.randomUUID());
        user1.setName("name");
        user1.setSurname("surname");
        user1.setVerifyEmail(true);
        user1.setUserId(UUID.randomUUID());
        StepVerifier.create(userRepository.save(user1))
                .assertNext(userSave -> this.userMember = userSave)
                .verifyComplete();

        StepVerifier.create(groupRepository.save(mapperGroup
                        .requestGroupDtotoGroup(new RequestGroupDto("groupTests"), user.getUserId().toString())))
                .assertNext(group -> this.group = group)
                .verifyComplete();
        GroupInvite groupInvite = new GroupInvite();
        groupInvite.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        groupInvite.setCode(UUID.randomUUID());
        groupInvite.setGroupId(group.getId());

        StepVerifier.create(groupInviteRepository.save(groupInvite))
                .assertNext(gi -> this.groupInvite = gi)
                .verifyComplete();
    }

    @Test
    @DisplayName("POST -> /api/v1/invitations/join")
    void joinGroupByCode_UserIsFoundAndUserIsNotMemberGroup_CorrectSave(){

        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", userMember.getUserId())
                .build();

        Flux<ResponseMembersDto> result = webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .post()
                .uri(requestMapping + "/join?code=" + groupInvite.getCode())
                .exchange()
                .expectStatus().isCreated()
                .returnResult(ResponseMembersDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(responseMembersDto -> {
                    assertEquals(2, responseMembersDto.users().size());
                    List<ResponseMemberDto> users = responseMembersDto.users();
                    assertEquals(users.getFirst().user().userId(), UUID.fromString(jwt.getSubject()));
                    assertEquals(true, users.getFirst().owner());
                    assertEquals(false, users.get(1).owner());
                    List<UUID> expectedUserId = users.stream().map(ResponseMemberDto::user).map(ResponseUserDto::userId).toList();
                    StepVerifier.create(groupMemberRepository
                                    .findMembersIdByGroupId(groupInvite.getGroupId()).collectList())
                            .assertNext(memberIds -> assertEquals(memberIds.stream().map(ResponseUserDto::userId).toList(), expectedUserId));
                });
    }

    @Test
    @DisplayName("POST -> /api/v1/invitations/join (user not verify email)")
    void joinGroupByCode_UserNotVerifyEmail_NotCorrectSave(){
        user.setVerifyEmail(false);
        userRepository.save(user).block();

        webTestClient
                .post()
                .uri(requestMapping + "/join?code=" + groupInvite.getCode())
                .exchange()
                .expectStatus().isEqualTo(403);
    }

    @Test
    @DisplayName("POST -> /api/v1/invitations/join (user is owner)")
    void joinGroupByCode_UserIsOwnerGroup_NotCorrectSave(){
       webTestClient
                .post()
                .uri(requestMapping + "/join?code=" + groupInvite.getCode())
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("POST -> /api/v1/invitations/join (user is memberGroup)")
    void joinGroupByCode_UserIsMemberGroup_NotCorrectSave(){
        jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", userMember.getUserId())
                .build();
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupInvite.getGroupId());
        groupMember.setUserId(UUID.fromString(jwt.getSubject()));
        StepVerifier.create(groupMemberRepository.save(groupMember))
                .assertNext(saved -> {
                    assertEquals(groupMember.getGroupId(), saved.getGroupId());
                    assertEquals(groupMember.getUserId(), saved.getUserId());
                })
                .verifyComplete();


        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .post()
                .uri(requestMapping + "/join?code=" + groupInvite.getCode())
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("POST -> /api/v1/invitations/join (code is not valid)")
    void joinGroupByCode_CodeIsNotValid_NotCorrectSave(){
        webTestClient
                .post()
                .uri(requestMapping + "/join?code=" + UUID.randomUUID())
                .exchange()
                .expectStatus().isEqualTo(410);
    }


    @Test
    @DisplayName("GET -> /api/v1/invitations/{groupId}")
    void getCodeByGroupId_CodeIsNotCreatedAndGroupIsFound_CorrectReturnAndSave(){

        StepVerifier.create(groupInviteRepository.deleteById(groupInvite.getGroupId()))
                        .verifyComplete();

        Flux<ResponseInviteCodeDto> result = webTestClient
                .get()
                .uri(requestMapping + "/" + group.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseInviteCodeDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(code -> StepVerifier.create(groupInviteRepository.findGroupIdByCode(code.code()))
                        .assertNext(groupId  -> assertEquals(groupInvite.getGroupId(), groupId))
                        .verifyComplete())
                .verifyComplete();
    }


    @Test
    @DisplayName("GET -> /api/v1/invitations/{groupId} (код просрочен)")
    void getCodeByGroupId_CodeIsCreatedButOverdueAndGroupIsFound_CorrectReturnAndSave(){
        groupInvite.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        StepVerifier.create(groupInviteRepository.save(groupInvite))
                .assertNext(groupInvite -> {})
                .verifyComplete();

        Flux<ResponseInviteCodeDto> result = webTestClient
                .get()
                .uri(requestMapping + "/" + groupInvite.getGroupId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseInviteCodeDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(code -> StepVerifier.create(groupInviteRepository.findGroupIdByCode(code.code()))
                        .assertNext(groupId  -> assertEquals(groupInvite.getGroupId(), groupId))
                        .verifyComplete())
                .verifyComplete();
    }

    @Test
    @DisplayName("GET -> /api/v1/invitations/{groupId} (group is not found)")
    void getCodeByGroupId_GroupIsNotFound_NotCorrectReturnAndSave(){
        webTestClient
                .get()
                .uri(requestMapping + "/" + 1000)
                .exchange()
                .expectStatus().isNotFound();
    }
}
