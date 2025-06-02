package com.example.coffeechat.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${spring.rabbitmq.exchange.chat}")
    private String exchangeName;

    @Value("${spring.rabbitmq.routing-key.chat}")
    private String routingKey;

    @Value("${spring.rabbitmq.queue.chat}")
    private String queueName;

    @Bean
    public Queue chatQueue() {
        return new Queue(queueName, true); // durable
    }

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue())
                .to(chatExchange())
                .with(routingKey);
    }
}
