package com.example.coffeechat.chat.domain.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "chat_messages")
@CompoundIndex(name = "chatroom_timestamp_idx", def = "{'chatroomId': 1, 'timestamp': -1}")
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private String id;

    private Long chatroomId;

    private Long senderId;

    private String senderName;

    private String message;

    @CreatedDate
    private LocalDateTime timestamp;

    public static ChatMessage create(
            Long chatroomId,
            Long senderId,
            String senderName,
            String message
    ){
        return new ChatMessage(
                chatroomId,
                senderId,
                senderName,
                message
        );
    }

    private ChatMessage(
            Long chatroomId,
            Long senderId,
            String senderName,
            String message
    ){
        this.chatroomId = chatroomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}

