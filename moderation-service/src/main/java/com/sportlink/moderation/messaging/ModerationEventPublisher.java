package com.sportlink.moderation.messaging;

import com.sportlink.moderation.config.RabbitMQConfig;
import com.sportlink.moderation.event.UserRestrictedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModerationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRestricted(UserRestrictedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.MODERATION_EVENTS_EXCHANGE,
                    RabbitMQConfig.ROUTING_USER_RESTRICTED,
                    event);
            log.info("Published UserRestricted for user {} (sanction {})",
                    event.getTargetUserId(), event.getSanctionId());
        } catch (AmqpException ex) {
            log.warn("Failed to publish UserRestricted for user {}: {}",
                    event.getTargetUserId(), ex.getMessage());
        }
    }
}
