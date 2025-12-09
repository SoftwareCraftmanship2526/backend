package com.uber.backend.ride.application.command;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class CompleteRideCommand {
    Long rideId;
}
