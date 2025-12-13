package com.uber.backend.ride.api.web;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.ride.application.*;
import com.uber.backend.ride.application.command.*;
import com.uber.backend.ride.application.exception.UnauthorizedException;
import com.uber.backend.ride.application.query.GetRideQuery;
import com.uber.backend.ride.application.query.RideResult;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.application.CheckRoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for ride operations.
 */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Ride", description = "Ride management APIs")
@SecurityRequirement(name = "bearerAuth")
public class RideController {

    private final JwtUtil jwtUtil;
    private final CheckRoleService checkRoleService;
    private final RideRepository rideRepository;
    private final GetRideQueryHandler getRideQueryHandler;
    private final RequestRideCommandHandler requestRideCommandHandler;
    private final DriverAcceptCommandHandler driverAcceptCommandHandler;
    private final DenyRideCommandHandler denyRideCommandHandler;
    private final StartRideCommandHandler startRideCommandHandler;
    private final CompleteRideCommandHandler completeRideCommandHandler;
    private final CancelRideCommandHandler cancelRideCommandHandler;

    @GetMapping("/{id}")
    @Operation(summary = "Get ride", description = "Get a ride by ID with payment information (if completed)")
    public ResponseEntity<RideResult> getRide(@PathVariable Long id) {
        RideResult result = getRideQueryHandler.handle(new GetRideQuery(id));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/request")
    @Operation(summary = "Request ride", description = "Passenger requests a new ride")
    public ResponseEntity<RideRequestResult> requestRide(@RequestBody RequestRideCommand command, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isPassenger(passengerId)) {
            throw new UnauthorizedException("Only passengers can request rides. Please log in as a passenger.");
        }
        RideRequestResult response = requestRideCommandHandler.handle(command, passengerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accept")
    @Operation(summary = "Accept ride", description = "Driver accepts a ride (REQUESTED/INVITED → ACCEPTED)")
    public ResponseEntity<RideResult> acceptRide(@RequestBody DriverAcceptCommand command, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Only drivers can accept rides. Please log in as a driver.");
        }
        RideResult result = driverAcceptCommandHandler.handle(command, driverId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/deny")
    @Operation(summary = "Deny ride", description = "Driver denies/declines an invited ride (INVITED → DENIED). Poller will find a new driver.")
    public ResponseEntity<RideResult> denyRide(@RequestBody DenyRideCommand command, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Only drivers can deny rides. Please log in as a driver.");
        }
        RideResult result = denyRideCommandHandler.handle(command, driverId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/start")
    @Operation(summary = "Start ride", description = "Driver starts a ride after arriving at pickup location (ACCEPTED → IN_PROGRESS)")
    public ResponseEntity<RideResult> startRide(@RequestBody StartRideCommand command, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Only drivers can start rides. Please log in as a driver.");
        }
        RideResult result = startRideCommandHandler.handle(command, driverId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/complete")
    @Operation(summary = "Complete ride", description = "Driver completes a ride (IN_PROGRESS → COMPLETED). Automatically calculates fare and creates pending payment.")
    public ResponseEntity<RideResult> completeRide(@RequestBody CompleteRideCommand command, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Only drivers can complete rides. Please log in as a driver.");
        }
        RideResult result = completeRideCommandHandler.handle(command, driverId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel ride", description = "Passenger cancels a ride. Fee policy: cancel before acceptance = free, cancel within 5 min after acceptance = free, cancel after 5+ min = €5 base fee + €1 per additional minute")
    public ResponseEntity<CancelRideResult> cancelRide(@RequestBody CancelRideCommand command, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isPassenger(passengerId)) {
            throw new UnauthorizedException("Only passengers can cancel rides. Please log in as a passenger.");
        }
        CancelRideResult result = cancelRideCommandHandler.handle(command, passengerId);
        return ResponseEntity.ok(result);
    }
}
