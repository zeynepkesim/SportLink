package com.sportlink.moderation.event;

import com.sportlink.moderation.model.enums.SanctionType;
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
public class UserRestrictedEvent implements Serializable {

    private UUID sanctionId;
    private UUID caseId;
    private UUID targetUserId;
    private SanctionType sanctionType;
    private LocalDateTime occurredAt;
}
