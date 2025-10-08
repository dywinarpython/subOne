package com.subOne.group_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("group_members")
public class GroupMember {
    @Id
    private Long id;
    private Long groupId;
    private UUID userId;
}
