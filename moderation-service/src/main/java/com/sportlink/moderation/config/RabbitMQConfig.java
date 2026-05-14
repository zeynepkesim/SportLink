package com.sportlink.moderation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MODERATION_EVENTS_EXCHANGE = "moderation-events";
    public static final String ROUTING_USER_RESTRICTED = "moderation.user.restricted";

    public static final String ACCOUNT_EVENTS_EXCHANGE = "account-events";
    public static final String ACCOUNT_STATUS_QUEUE = "moderation.account-status-updated.queue";
    public static final String ACCOUNT_STATUS_BINDING = "account.user.status.updated";

    @Bean
    public TopicExchange moderationEventsExchange() {
        return new TopicExchange(MODERATION_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange accountEventsExchange() {
        return new TopicExchange(ACCOUNT_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue accountStatusQueue() {
        return QueueBuilder.durable(ACCOUNT_STATUS_QUEUE).build();
    }

    @Bean
    public Binding accountStatusBinding(Queue accountStatusQueue, TopicExchange accountEventsExchange) {
        return BindingBuilder.bind(accountStatusQueue)
                .to(accountEventsExchange)
                .with(ACCOUNT_STATUS_BINDING);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
