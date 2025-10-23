package com.subOne.user_service.repository.group_member_repository.update;

import com.subOne.user_service.dto.user.request.RequestGroupOwnershipChangesDto;
import reactor.core.publisher.Mono;

import java.util.List;

@FunctionalInterface
public interface UpdateOwnerGroupRepository {
    Mono<Void> updateOwnerGroup(List<RequestGroupOwnershipChangesDto> requestGroupsOwnershipChangesDtos);
}
