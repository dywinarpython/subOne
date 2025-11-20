package com.subOne.user_service.mapper;

import com.subOne.user_service.entity.GroupMember;
import org.mapstruct.Mapper;

import java.util.UUID;


@Mapper(componentModel = "spring")
public interface MapperGroupMember {


    GroupMember userIdAndGroupIdToGroupMember(UUID userId, Long groupId);
}
