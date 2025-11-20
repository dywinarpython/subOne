package com.subOne.user_service.repository.group_repository.select;

import com.subOne.user_service.dto.user.response.ResponseUserDto;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface SelectOwnerGroupRepository {
    Mono<ResponseUserDto> selectOwnerByGroupId(Long groupId);
}
