package com.uber.backend.ride.application.command;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DriverAssignCommand {
    private Long rideId;
    private Long driverId;
}
