package com.uber.backend.ride.application.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
public class DriverAcceptCommand {
    Long rideId;
}
