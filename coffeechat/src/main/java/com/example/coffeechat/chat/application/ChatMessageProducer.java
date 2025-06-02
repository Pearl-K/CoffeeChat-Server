package com.example.coffeechat.chat.application;

import com.example.coffeechat.chat.domain.entity.ChatMessage;

public interface ChatMessageProducer {
    void send(ChatMessage message);
}
