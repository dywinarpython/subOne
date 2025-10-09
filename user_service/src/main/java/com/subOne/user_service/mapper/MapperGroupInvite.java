package com.subOne.user_service.mapper;

import com.subOne.user_service.entity.GroupInvite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;


@Mapper(componentModel = "spring")
public interface MapperGroupInvite {


    @Mapping(target = "expiresAt", expression = "java(java.time.LocalDateTime.now().plusMinutes(5))")
    GroupInvite codeAndGroupIDToGroupInvite(Long groupId, UUID code);
}
