package com.subOne.user_service.dto.group_member.response;

import com.subOne.user_service.dto.user.response.ResponseUserDto;


public record ResponseMemberDto(ResponseUserDto user, Boolean owner) {
}
