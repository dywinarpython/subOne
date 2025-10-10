package com.subOne.user_service.dto.group_member.response;

import java.util.UUID;

public record ResponseMemberDto(UUID userId, Boolean owner) {
}
