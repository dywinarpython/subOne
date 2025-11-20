package com.subOne.user_service.dto.group_invite.response;

import java.util.UUID;

public record ResponseInviteCodeDto(UUID code, Long expiresInSeconds) {
}
