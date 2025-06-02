package com.example.coffeechat.chat.infrastructure.messaging.rabbitmq;

import com.example.coffeechat.chat.application.ChatMessageProducer;
import com.example.coffeechat.chat.domain.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitChatMessageProducer implements ChatMessageProducer {

    private final AmqpTemplate amqpTemplate;

    @Value("${spring.rabbitmq.exchange.chat}")
    private String exchange;

    @Value("${spring.rabbitmq.routing-key.chat}")
    private String routingKey;

    @Override
    public void send(ChatMessage message) {
        amqpTemplate.convertAndSend(exchange, routingKey, message);
    }
}
