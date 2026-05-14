package com.sportlink.account.messaging;

import com.sportlink.account.config.RabbitMQConfig;
import com.sportlink.account.event.FacilityProfileUpdatedEvent;
import com.sportlink.account.event.UserProfileUpdatedEvent;
import com.sportlink.account.event.UserStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserProfileUpdated(UserProfileUpdatedEvent event) {
        send(RabbitMQConfig.ROUTING_USER_UPDATED, event, "UserProfileUpdated for user " + event.getUserId());
    }

    public void publishUserStatusUpdated(UserStatusUpdatedEvent event) {
        send(RabbitMQConfig.ROUTING_USER_STATUS_UPDATED, event, "UserStatusUpdated for user " + event.getUserId() + " -> " + event.getStatus());
    }

    public void publishFacilityProfileUpdated(FacilityProfileUpdatedEvent event) {
        send(RabbitMQConfig.ROUTING_FACILITY_UPDATED, event, "FacilityProfileUpdated for facility " + event.getCenterId());
    }

    private void send(String routingKey, Object payload, String description) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.ACCOUNT_EVENTS_EXCHANGE, routingKey, payload);
            log.info("Published {} (routing key: {})", description, routingKey);
        } catch (AmqpException ex) {
            log.warn("Failed to publish event {} (routing key: {}): {}",
                    description, routingKey, ex.getMessage());
        }
    }
}
