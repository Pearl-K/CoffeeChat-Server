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
    private static final String PATH_PREFIX = "/topic/chatroom/";

    @RabbitListener(queues = "${spring.rabbitmq.queue.chat}")
    public void receiveMessage(ChatMessage message) {
        log.info("\uD83D\uDCE9 Received ChatMessage with RMQ: {}", message.toString());

        chatMessageRepository.save(message);
        messagingTemplate.convertAndSend(PATH_PREFIX + message.getChatroomId(), message);
    }
}
