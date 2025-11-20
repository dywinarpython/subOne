package com.subOne.user_service.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("group_members")
@Setter
@Getter
public class GroupMember {
    @Id
    private Long id;
    private Long groupId;
    private UUID userId;
}
