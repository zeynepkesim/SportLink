package com.sportlink.account.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ACCOUNT_EVENTS_EXCHANGE = "account-events";

    public static final String ROUTING_USER_UPDATED = "account.user.updated";
    public static final String ROUTING_USER_STATUS_UPDATED = "account.user.status.updated";
    public static final String ROUTING_FACILITY_UPDATED = "account.facility.updated";

    @Bean
    public TopicExchange accountEventsExchange() {
        return new TopicExchange(ACCOUNT_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
