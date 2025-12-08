package com.subOne.notifications_service.client.service;

import java.util.UUID;

public interface RestTemplateService {
    UUID getOwnerIdByGroupId(Integer groupId);
}
