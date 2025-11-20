package com.subOne.notifications_service.client.service;

import org.springframework.lang.Nullable;

import java.util.UUID;

public interface RestTemplateService {
    @Nullable
    UUID getOwnerIdByGroupId(Long groupId);
}
