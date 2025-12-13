package com.uber.backend.ride.application.command;

import com.uber.backend.ride.domain.enums.RideType;
import com.uber.backend.shared.domain.valueobject.Location;

public record RequestRideCommand (
        RideType type,
        Location start,
        Location end,
        int durationMin,
        Double demandMultiplier
) {}