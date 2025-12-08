package com.uber.backend.ride.api.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class RideCancelDto {
    private Long rideId;
}
