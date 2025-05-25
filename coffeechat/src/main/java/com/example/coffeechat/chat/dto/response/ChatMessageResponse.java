package com.example.coffeechat.chat.dto.response;

import com.example.coffeechat.chat.domain.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        String id,
        Long chatroomId,
        Long senderId,
        String senderName,
        String message,
        LocalDateTime timestamp
) {
    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getChatroomId(),
                chatMessage.getSenderId(),
                chatMessage.getSenderName(),
                chatMessage.getMessage(),
                chatMessage.getTimestamp()
        );
    }
}

