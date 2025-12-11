package com.uber.backend.ride.api.web;

import com.uber.backend.ride.application.CreateRideCommandHandler;
import com.uber.backend.ride.application.GetRideQueryHandler;
import com.uber.backend.ride.application.UpdateRideStatusCommandHandler;
import com.uber.backend.ride.application.command.CreateRideCommand;
import com.uber.backend.ride.application.command.CreateRideResult;
import com.uber.backend.ride.application.command.UpdateRideStatusCommand;
import com.uber.backend.ride.application.command.UpdateRideStatusResult;
import com.uber.backend.ride.application.query.GetRideQuery;
import com.uber.backend.ride.application.query.RideResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for ride operations.
 */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Ride", description = "Ride management APIs")
public class RideController {

    private final CreateRideCommandHandler createRideCommandHandler;
    private final UpdateRideStatusCommandHandler updateRideStatusCommandHandler;
    private final GetRideQueryHandler getRideQueryHandler;

    @GetMapping("/{id}")
    @Operation(summary = "Get ride", description = "Get a ride by ID with payment information (if completed)")
    public ResponseEntity<RideResult> getRide(@PathVariable Long id) {
        RideResult result = getRideQueryHandler.handle(new GetRideQuery(id));
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Create ride", description = "Create a new ride (usually with REQUESTED status)")
    public ResponseEntity<CreateRideResult> createRide(@Valid @RequestBody CreateRideCommand command) {
        CreateRideResult result = createRideCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update ride status", description = "Update ride status. When status changes to COMPLETED, a pending payment is automatically created.")
    public ResponseEntity<UpdateRideStatusResult> updateRideStatus(
            @PathVariable Long id,
            @RequestBody UpdateRideStatusCommand command) {
        // Build command with rideId from path variable
        UpdateRideStatusCommand updatedCommand = new UpdateRideStatusCommand(
                id,
                command.newStatus(),
                command.driverId(),
                command.distanceKm(),
                command.durationMin(),
                command.demandMultiplier()
        );
        UpdateRideStatusResult result = updateRideStatusCommandHandler.handle(updatedCommand);
        return ResponseEntity.ok(result);
    }
}
