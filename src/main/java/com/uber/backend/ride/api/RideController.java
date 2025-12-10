package com.uber.backend.ride.api;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.ride.application.*;
import com.uber.backend.ride.application.command.*;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.application.exception.UnauthorizedException;
import com.uber.backend.ride.application.query.PriceRequestQuery;
import com.uber.backend.ride.application.query.RideQueryService;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.applicaition.CheckRoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/ride")
@RequiredArgsConstructor
public class RideController {

    private final JwtUtil jwtUtil;
    private final RideRepository rideRepository;
    private final CheckRoleService checkRoleService;
    private final RideQueryService rideQueryService;
    private final CancelRideCommandHandler cancelRideCommandHandler;
    private final CompleteRideCommandHandler completeRideCommandHandler;
    private final DriverAcceptCommandHandler driverAcceptCommandHandler;
    private final DriverAssignCommandHandler driverAssignCommandHandler;
    private final RequestRideCommandHandler requestRideCommandHandler;

    @PostMapping("/request")
    public ResponseEntity<RideRequestResult> requestRide(@RequestBody RequestRideCommand command, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isPassenger(passengerId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        RideRequestResult response = requestRideCommandHandler.handle(command, passengerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public RideAssignResult assignDriver(@RequestBody DriverAssignCommand command, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        return driverAssignCommandHandler.handle(command, passengerId);
    }

    @PostMapping("/cancel")
    public String cancelRide(@RequestBody CancelRideCommand command, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        Long rideId = command.rideId();
        if (!checkRoleService.isPassenger(passengerId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        RideEntity rideEntity = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId));

        if (!rideEntity.getPassenger().getId().equals(passengerId)) {
            throw new UnauthorizedException("You don't have access to cancel this ride");
        }

        return cancelRideCommandHandler.handle(command);
    }

    @PostMapping("/complete")
    public RideEntity completeRide(@RequestBody CompleteRideCommand command, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return completeRideCommandHandler.handle(command);
    }

    @PostMapping("/accept")
    public RideEntity acceptDriver(@RequestBody DriverAcceptCommand command, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return driverAcceptCommandHandler.handle(command);
    }

    @PostMapping("/price")
    public Map<String, BigDecimal> priceRequest(@RequestBody PriceRequestQuery request) {
        return rideQueryService.priceRequest(
                request.getStart(),
                request.getEnd(),
                request.getDurationMin(),
                request.getDemandMultiplier()
        );
    }

}
