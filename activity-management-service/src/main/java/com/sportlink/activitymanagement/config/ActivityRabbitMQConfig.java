package com.sportlink.activitymanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivityRabbitMQConfig {

    public static final String EXCHANGE_NAME = "interaction-events";
    public static final String ACTIVITY_CREATED_ROUTING_KEY = "activity.created";

    @Bean
    public TopicExchange activityExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter activityMessageConverter() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(om);
    }

    @Bean
    public RabbitTemplate activityRabbitTemplate(ConnectionFactory cf,
                                                  Jackson2JsonMessageConverter activityMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(activityMessageConverter);
        return template;
    }
}
