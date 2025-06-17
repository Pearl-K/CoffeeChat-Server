package com.example.coffeechat.chat.presentation;

import com.example.coffeechat.chat.application.ChatMessageProducer;
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

    private final ChatMessageProducer chatMessageProducer;
    private final RedisPublisher redisPublisher;
    private final UserStatusService userStatusService;

    @MessageMapping("/chat/sendMessage")
    public void sendMessage(
            @Payload ChatMessage message
    ) {
        chatMessageProducer.send(message);
        log.info("\uD83D\uDCE8 Sent message via MQ: {}", message.toString());
    }

    @MessageMapping("/user/ping")
    public void handlePing(
            @Payload UserStatusResponse response
    ) {
        redisPublisher.publishUserStatus(response.userId(), response.status());
        userStatusService.updateOnlineStatus(response.userId());
    }
}

