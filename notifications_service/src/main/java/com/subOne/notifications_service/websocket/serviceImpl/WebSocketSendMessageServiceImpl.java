package com.subOne.notifications_service.websocket.serviceImpl;

import com.subOne.notifications_service.websocket.service.WebSocketSendMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketSendMessageServiceImpl implements WebSocketSendMessageService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void sendMessage(String user, String destination, Object message) {
        simpMessagingTemplate.convertAndSendToUser(user, destination, message);
    }
}
