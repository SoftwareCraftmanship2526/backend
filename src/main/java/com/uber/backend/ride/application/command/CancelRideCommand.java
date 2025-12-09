package com.uber.backend.ride.application.command;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class CancelRideCommand {
    private Long rideId;
}
