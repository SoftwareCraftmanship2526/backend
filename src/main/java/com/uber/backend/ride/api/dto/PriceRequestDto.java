package com.uber.backend.ride.api.dto;

import com.uber.backend.shared.domain.valueobject.Location;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PriceRequestDto {
    private Location start;
    private Location end;
    private int durationMin;
    private double demandMutiplier;
}
