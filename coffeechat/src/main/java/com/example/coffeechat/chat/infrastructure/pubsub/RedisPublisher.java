package com.example.coffeechat.chat.infrastructure.pubsub;

import com.example.coffeechat.chat.domain.ChatMessageRepository;
import com.example.coffeechat.chat.domain.entity.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisPublisher {

    private final ChatMessageRepository chatMessageRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ChannelTopic userStatusTopic;

    public RedisPublisher(
            RedisTemplate<String, Object> redisTemplate,
            ChatMessageRepository chatMessageRepository,
            ObjectMapper objectMapper,
            ChannelTopic userStatusTopic
    ) {
        this.redisTemplate = redisTemplate;
        this.chatMessageRepository = chatMessageRepository;
        this.objectMapper = objectMapper;
        this.userStatusTopic = userStatusTopic;
    }

    @Transactional
    public void publishMessage(ChatMessage message) {
        try {
            saveMessage(message);
            publishToRedis(message);

        } catch (Exception e) {
            log.error("Failed to save message or publish to Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Message processing failed.", e);
        }
    }

    public void publishUserStatus(Long userId, Boolean isOnline) {
        String message = String.format("{\"userId\":%d,\"status\":%s}", userId, isOnline);
        redisTemplate.convertAndSend(userStatusTopic.getTopic(), message);
    }

    private void saveMessage(ChatMessage message) {
        chatMessageRepository.save(message);
        log.info("saved message at MongoDB: {}", message);
    }

    private void publishToRedis(ChatMessage message) {
        String messageJson = convertToJson(message);
        String topic = getChatRoomTopic(message.getChatroomId());
        redisTemplate.convertAndSend(topic, messageJson);
        log.info("Published message to Redis topic {}: {}", topic, messageJson);
    }

    private String convertToJson(ChatMessage message) {
        try{
            return objectMapper.writeValueAsString(message);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String getChatRoomTopic(Long chatRoomId) {
        return "chatroom:" + chatRoomId;
    }
}

