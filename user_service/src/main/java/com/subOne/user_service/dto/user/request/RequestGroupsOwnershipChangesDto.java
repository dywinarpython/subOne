package com.subOne.user_service.dto.user.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record RequestGroupsOwnershipChangesDto(
        @Size(max = 5, message = "The maximum number of groups for reassigning the owner is 5")
        List<RequestGroupOwnershipChangesDto> groupOwnershipChanges
) {
}
