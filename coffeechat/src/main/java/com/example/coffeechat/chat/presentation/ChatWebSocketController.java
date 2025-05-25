package com.example.coffeechat.chat.presentation;

import com.example.coffeechat.chat.domain.entity.ChatMessage;
import com.example.coffeechat.chat.infrastructure.pubsub.RedisPublisher;
import com.example.coffeechat.user.application.UserStatusService;
import com.example.coffeechat.user.dto.response.UserStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final RedisPublisher redisPublisher;
    private final UserStatusService userStatusService;

    @MessageMapping("/chat/sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        if (message.getChatroomId() == null) {
            log.error("🚨 chatRoomId is NULL!");
        }
        redisPublisher.publishMessage(message);
        log.info("WebSocket Message sent: {}", message);
    }

    @MessageMapping("/user/ping")
    public void handlePing(@Payload UserStatusResponse response) {
        redisPublisher.publishUserStatus(response.userId(), response.status());
        userStatusService.updateOnlineStatus(response.userId());
    }
}

