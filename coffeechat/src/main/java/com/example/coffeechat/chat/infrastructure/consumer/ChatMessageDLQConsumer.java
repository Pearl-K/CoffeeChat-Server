package com.example.coffeechat.chat.infrastructure.consumer;

import com.example.coffeechat.chat.domain.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageDLQConsumer {

    @RabbitListener(queues = "${spring.rabbitmq.queue.dlq}")
    public void handleFailedMessage(ChatMessage failedMessage) {
        log.warn("[DLQ] Failed to handle chat message: {}", failedMessage);

        // TODO: DLQ 전략 구현
    }
}
