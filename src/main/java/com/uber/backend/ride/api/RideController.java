package com.uber.backend.ride.api;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.ride.application.RideCommandService;
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

    private final RideCommandService rideCommandService;
    private final JwtUtil jwtUtil;
    private final RideRepository rideRepository;
    private final CheckRoleService checkRoleService;
    private final RideQueryService rideQueryService;

    @PostMapping("/request")
    public ResponseEntity<RideResponseCommand> requestRide(@RequestBody RequestRideCommand request, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isPassenger(passengerId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        RideResponseCommand response = rideCommandService.createRideRequest(request, passengerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public RideAssignCommand assignDriver(@RequestBody DriverAssignCommand dto, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        return rideCommandService.assignDriverToRide(dto.getRideId(), dto.getDriverId(), passengerId);
    }

    @PostMapping("/cancel")
    public String cancelRide(@RequestBody CancelRideCommand request, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        Long rideId = request.getRideId();
        if (!checkRoleService.isPassenger(passengerId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        RideEntity rideEntity = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId));

        if (!rideEntity.getPassenger().getId().equals(passengerId)) {
            throw new UnauthorizedException("You don't have access to cancel this ride");
        }

        return rideCommandService.cancelRide(rideId);
    }

    @PostMapping("/complete")
    public RideEntity completeRide(@RequestBody CompleteRideCommand dto, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return rideCommandService.completeRide(dto.getRideId());
    }

    @PostMapping("/accept")
    public RideEntity acceptDriver(@RequestBody DriverAcceptCommand driverAcceptDto, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return rideCommandService.acceptRide(driverAcceptDto.getRideId());
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
