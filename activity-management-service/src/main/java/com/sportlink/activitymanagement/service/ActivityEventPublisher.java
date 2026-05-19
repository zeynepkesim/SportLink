package com.sportlink.activitymanagement.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sportlink.activitymanagement.config.ActivityRabbitMQConfig;
import com.sportlink.activitymanagement.config.KafkaTopicConfiguration;
import com.sportlink.activitymanagement.event.ActivityCancelledEvent;
import com.sportlink.activitymanagement.event.ActivityCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityEventPublisher {

    private final KafkaTemplate<String, ActivityCancelledEvent> kafkaTemplate;
    private final RabbitTemplate activityRabbitTemplate;

    public void publishActivityCancelled(ActivityCancelledEvent event) {
        log.info("Publishing ActivityCancelledEvent for activity {} (slot={})",
                event.getActivityId(), event.getPreferredTimeSlotId());
        kafkaTemplate.send(KafkaTopicConfiguration.ACTIVITY_CANCELLED_TOPIC, event);
    }

    public void publishActivityCreated(ActivityCreatedEvent event) {
        log.info("Publishing ActivityCreatedEvent for activity {} by organizer {}",
                event.getActivityId(), event.getOrganizerId());
        activityRabbitTemplate.convertAndSend(
                ActivityRabbitMQConfig.EXCHANGE_NAME,
                ActivityRabbitMQConfig.ACTIVITY_CREATED_ROUTING_KEY,
                event);
    }
}