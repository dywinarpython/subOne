package com.subOne.user_service.dto.group.response;

import java.util.List;

public record ResponseGroupsDto(
        List<ResponseGroupDto> groups
) {
}
