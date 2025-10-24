package com.subOne.notifications_service.websocket;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
@RequiredArgsConstructor
public class StompPrincipal implements Principal {
    private final String name;

    @Override
    public String getName() {
        return this.name;
    }
}
