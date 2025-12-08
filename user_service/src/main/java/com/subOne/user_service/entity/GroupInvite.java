package com.subOne.user_service.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("group_invites")
@Getter
@Setter
public class GroupInvite {
    @Id
    private Integer id;
    private UUID code;
    private Integer groupId;
    private LocalDateTime expiresAt;
}
