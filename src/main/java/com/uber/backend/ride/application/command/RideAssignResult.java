package com.uber.backend.ride.application.command;

import com.uber.backend.driver.domain.model.Driver;
import com.uber.backend.driver.domain.model.Vehicle;
import com.uber.backend.ride.domain.enums.RideStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideAssignResult  (
        Long rideId,
        Long passengerId,
        RideStatus status,
        LocalDateTime requestedAt,
        BigDecimal price,
        Driver driver,
        Vehicle vehicle,
        LocalDateTime startTime
) {}
