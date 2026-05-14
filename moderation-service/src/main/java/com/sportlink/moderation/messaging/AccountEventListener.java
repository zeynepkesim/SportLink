package com.sportlink.moderation.messaging;

import com.sportlink.moderation.config.RabbitMQConfig;
import com.sportlink.moderation.event.AccountUserStatusUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventListener {

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_STATUS_QUEUE)
    public void onUserStatusUpdated(AccountUserStatusUpdatedEvent event) {
        log.info("Received UserStatusUpdated event from Account Management — user {} is now {}",
                event.getUserId(), event.getStatus());
    }
}
