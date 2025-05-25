package com.example.coffeechat.chat.infrastructure.pubsub;

import com.example.coffeechat.chat.domain.entity.ChatMessage;
import com.example.coffeechat.user.dto.response.UserStatusResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String receivedMessage = new String(message.getBody());
        log.info("📩 Redis received message: {}", receivedMessage);

        try {
            if (receivedMessage.contains("chatroomId")) {
                ChatMessage chatMessage = objectMapper
                        .readValue(receivedMessage, ChatMessage.class);
                String destination = "/sub/chatroom/" + chatMessage.getChatroomId();
                messagingTemplate.convertAndSend(destination, chatMessage);
                log.info("✅ 채팅 메시지 전송: {} → {}", destination, receivedMessage);
            }
            else if (receivedMessage.contains("status")) {
                UserStatusResponse statusResponse = objectMapper
                        .readValue(receivedMessage, UserStatusResponse.class);
                String destination = "/sub/user/status";
                messagingTemplate.convertAndSend(destination, statusResponse);
                log.info("✅ 사용자 상태 전송: {} → {}", destination, receivedMessage);
            }
        } catch (JsonProcessingException e) {
            log.error("❌ Redis 메시지 파싱 실패: {}", e.getMessage(), e);
        }
    }
}

