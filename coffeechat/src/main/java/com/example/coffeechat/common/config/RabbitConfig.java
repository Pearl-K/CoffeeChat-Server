package com.example.coffeechat.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitConfig {

    public static final int PARTITION_COUNT = 3;
    public static final String CHAT_QUEUE_NAME_PREFIX = "chat.queue.";

    @Value("${spring.rabbitmq.exchange.chat}")
    private String exchangeName;

    @Value("${spring.rabbitmq.exchange.dlx}")
    private String dlxExchangeName;

    @Value("${spring.rabbitmq.routing-key.dlq}")
    private String dlqRoutingKey;

    @Value("${spring.rabbitmq.queue.dlq}")
    private String chatDlqQueueName;

    /**
     * Consistent Hash Exchange
     * routing-key: chatroom_id
     */
    @Bean
    public CustomExchange chatExchange() {
        return new CustomExchange(exchangeName, "x-consistent-hash", true, false);
    }

    /**
     * PARTITION_COUNT 만큼 큐 생성 후 -> Exchange 바인딩
     * 각 큐마다 Dead Letter Exchange 설정
     */
    @Bean
    public Declarables partitionedQueuesAndBindings() {
        List<Declarable> declarables = new ArrayList<>();
        for (int i = 0; i < PARTITION_COUNT; i++) {
            Queue queue = QueueBuilder.durable(CHAT_QUEUE_NAME_PREFIX + i)
                    .withArgument("x-dead-letter-exchange", dlxExchangeName)
                    .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                    .build();
            declarables.add(queue);

            Binding binding = BindingBuilder.bind(queue)
                    .to(chatExchange())
                    .with(String.valueOf(i))
                    .noargs();
            declarables.add(binding);
        }
        return new Declarables(declarables);
    }

    @Bean
    public Queue chatDlqQueue() {
        return QueueBuilder.durable(chatDlqQueueName).build();
    }

    @Bean
    public DirectExchange chatDlxExchange() {
        return new DirectExchange(dlxExchangeName);
    }

    @Bean
    public Binding chatDlqBinding() {
        return BindingBuilder.bind(chatDlqQueue())
                .to(chatDlxExchange())
                .with(dlqRoutingKey);
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
