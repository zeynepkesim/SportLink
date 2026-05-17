package com.sportlink.booking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the Kafka topic(s) used by the Booking Service.
 *
 * Spring Boot's KafkaAdmin picks up any NewTopic bean and creates the topic
 * on the broker at application startup (if it does not already exist).
 * This means the topic is provisioned automatically alongside the service -
 * the same convenience RabbitMQ exchanges/queues used to give us via
 * AmqpAdmin in the original (now-replaced) design.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${sportlink.kafka.topics.booking-events}")
    private String bookingEventsTopic;

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name(bookingEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
