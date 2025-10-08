package com.subOne.group_service.dto.group_member.response;

import java.util.UUID;

public record ResponseMemberDto(UUID userId, Boolean owner) {
}
