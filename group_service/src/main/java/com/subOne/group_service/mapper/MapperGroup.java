package com.subOne.group_service.mapper;

import com.subOne.group_service.dto.group.request.RequestGroupDto;
import com.subOne.group_service.dto.group.response.ResponseGroupDto;
import com.subOne.group_service.entity.Group;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;


@Mapper(componentModel = "spring")
public interface MapperGroup {


    @Mappings(value = {
            @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())"),
            @Mapping(target = "ownerId", expression = "java(java.util.UUID.fromString(userId))")
    })
    Group requestGroupDtotoGroup(RequestGroupDto requestGroupDto, @Context String userId);

    ResponseGroupDto groupToResponseGroupDto(Group group);
}
