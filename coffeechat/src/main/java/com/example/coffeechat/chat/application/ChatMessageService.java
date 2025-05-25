package com.example.coffeechat.chat.application;


import com.example.coffeechat.chat.domain.ChatMessageRepository;
import com.example.coffeechat.chat.domain.entity.ChatMessage;
import com.example.coffeechat.chat.dto.request.ChatMessageRequest;
import com.example.coffeechat.chat.dto.response.ChatMessageResponse;
import com.example.coffeechat.chat.infrastructure.pubsub.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final RedisPublisher redisPublisher;
    private final ChatMessageRepository chatMessageRepository;

    public void sendMessage(ChatMessageRequest request) {
        ChatMessage newMessage = ChatMessage.create(
                request.chatRoomId(),
                request.senderId(),
                request.senderName(),
                request.message()
        );
        redisPublisher.publishMessage(newMessage);
    }

    public Page<ChatMessageResponse> getChatMessagesByChatroomId(Long chatroomId, Pageable pageable) {
        return chatMessageRepository.findByChatroomIdOrderByTimestampDesc(chatroomId, pageable)
                .map(ChatMessageResponse::from);
    }
}
