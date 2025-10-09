package com.subOne.user_service.dto.group_invite;

import java.time.LocalDateTime;
import java.util.UUID;

public record CodeDto(UUID code, LocalDateTime expiresAt) {
}
