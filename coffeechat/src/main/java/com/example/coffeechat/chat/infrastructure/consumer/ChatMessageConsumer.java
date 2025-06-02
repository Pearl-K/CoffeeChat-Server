package com.example.coffeechat.chat.infrastructure.consumer;

import com.example.coffeechat.chat.domain.ChatMessageRepository;
import com.example.coffeechat.chat.domain.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "${queue.chat}")
    public void receiveMessage(ChatMessage message) {
        log.info("🟢 Received ChatMessage with RMQ: {}", message);

        // TODO: 추후 비동기 처리 등, 확장 전략 구현
        chatMessageRepository.save(message);

        String destination = "/topic/chatroom/" + message.getChatroomId();
        messagingTemplate.convertAndSend(destination, message);
    }
}

