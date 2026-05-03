package com.sportlink.account.domain.model.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLabel {

    @Column(name = "behaviorLabel")
    private String behaviorLabel;

    @Column(name = "labelValue")
    private String labelValue;
}
