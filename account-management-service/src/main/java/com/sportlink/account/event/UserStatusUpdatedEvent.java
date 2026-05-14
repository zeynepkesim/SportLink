package com.sportlink.account.event;

import com.sportlink.account.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdatedEvent implements Serializable {

    private UUID userId;
    private UserStatus status;
    private LocalDateTime occurredAt;
}
