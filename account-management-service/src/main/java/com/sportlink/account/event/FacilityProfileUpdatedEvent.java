package com.sportlink.account.event;

import com.sportlink.account.model.enums.FacilityStatus;
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
public class FacilityProfileUpdatedEvent implements Serializable {

    private UUID centerId;
    private String name;
    private String email;
    private String sportTypes;
    private String city;
    private String district;
    private FacilityStatus status;
    private LocalDateTime occurredAt;
}
