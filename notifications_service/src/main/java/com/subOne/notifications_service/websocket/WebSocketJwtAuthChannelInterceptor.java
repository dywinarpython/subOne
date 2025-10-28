package com.subOne.notifications_service.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketJwtAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if(accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())){
            String token = extractToken(accessor);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                String userId = jwt.getSubject();
                accessor.setUser(new StompPrincipal(userId));
            } catch (JwtException ex) {
                throw new AccessDeniedException("Access is denied");
            }
        }
        return message;
    }

    private String extractToken(StompHeaderAccessor accessor){
        List<String> auths = accessor.getNativeHeader("Authorization");
        if(auths != null && !auths.isEmpty()){
            String raw = auths.getFirst();
            if (raw.toLowerCase().startsWith("bearer ")) return raw.substring(7);
        }
        throw new AuthenticationCredentialsNotFoundException("Invalid or expired token");
    }
}
