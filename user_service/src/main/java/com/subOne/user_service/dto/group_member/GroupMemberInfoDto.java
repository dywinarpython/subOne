package com.subOne.user_service.dto.group_member;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public record GroupMemberInfoDto(Boolean exist, Long count) {
}
