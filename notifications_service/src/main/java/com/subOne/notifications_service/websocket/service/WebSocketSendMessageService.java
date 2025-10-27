package com.subOne.notifications_service.websocket.service;

@FunctionalInterface
public interface WebSocketSendMessageService {
    void sendMessage(String user, String destination, Object message);
}
