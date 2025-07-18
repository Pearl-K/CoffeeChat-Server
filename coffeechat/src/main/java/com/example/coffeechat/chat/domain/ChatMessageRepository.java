package com.example.coffeechat.chat.domain;

import com.example.coffeechat.chat.domain.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    Page<ChatMessage> findByChatroomIdOrderByTimestampDesc(Long chatroomId, Pageable pageable);
}

