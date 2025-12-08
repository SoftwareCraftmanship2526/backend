package com.uber.backend.ride.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DriverAssignDto {
    private Long rideId;
    private Long driverId;
}
