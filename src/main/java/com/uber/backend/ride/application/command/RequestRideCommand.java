package com.uber.backend.ride.application.command;

import com.uber.backend.shared.domain.valueobject.Location;

public record RequestRideCommand (
        Long passengerId,
        String type,
        Location start,
        Location end,
        int durationMin,
        Double demandMultiplier
) {}