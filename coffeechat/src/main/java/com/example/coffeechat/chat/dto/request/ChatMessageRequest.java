package com.example.coffeechat.chat.dto.request;

public record ChatMessageRequest(
        Long chatRoomId,
        Long senderId,
        String senderName,
        String message
) {
}

