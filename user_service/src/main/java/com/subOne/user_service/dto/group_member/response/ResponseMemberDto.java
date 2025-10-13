package com.subOne.user_service.dto.group_member.response;

import com.subOne.user_service.dto.user.response.UserResponseDto;


public record ResponseMemberDto(UserResponseDto user, Boolean owner) {
}
