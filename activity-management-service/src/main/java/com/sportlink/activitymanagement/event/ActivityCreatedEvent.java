package com.sportlink.activitymanagement.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCreatedEvent {
    private UUID activityId;
    private UUID organizerId;
    private String title;
    private String sportType;
    private LocalDateTime createdAt;
}
