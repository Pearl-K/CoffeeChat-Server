package com.example.coffeechat.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${spring.rabbitmq.exchange.chat}")
    private String exchangeName;

    @Value("${spring.rabbitmq.routing-key.message}")
    private String messageRoutingKey;

    @Value("${spring.rabbitmq.queue.chat}")
    private String queueName;

    @Bean
    public Queue chatQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue())
                .to(chatExchange())
                .with(messageRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
