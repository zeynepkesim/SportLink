package com.sportlink.account.event;

import com.sportlink.account.model.enums.SkillLevel;
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
public class UserProfileUpdatedEvent implements Serializable {

    private UUID userId;
    private String name;
    private String email;
    private SkillLevel skillLevel;
    private String language;
    private String sportPreferences;
    private String city;
    private String district;
    private LocalDateTime occurredAt;
}
