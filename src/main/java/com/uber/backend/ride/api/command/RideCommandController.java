package com.uber.backend.ride.api.command;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.ride.api.dto.*;
import com.uber.backend.ride.application.command.RideCommandService;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.application.exception.UnauthorizedException;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ride/command")
@RequiredArgsConstructor
public class RideCommandController {

    private final RideCommandService rideCommandService;
    private final JwtUtil jwtUtil;
    private final RideRepository rideRepository;

    @PostMapping("/request")
    public ResponseEntity<RideResponseDto> requestRide(@RequestBody RideRequestDto request, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        RideResponseDto response = rideCommandService.createRideRequest(request, passengerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public RideAssignDto assignDriver(@RequestBody DriverAssignDto dto, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        return rideCommandService.assignDriverToRide(dto.getRideId(), dto.getDriverId(), passengerId);
    }

    @PostMapping("/cancel")
    public String cancelRide(@RequestBody RideCancelDto request, HttpServletRequest httpRequest) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        Long rideId = request.getRideId();

        RideEntity rideEntity = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId));

        if (!rideEntity.getPassenger().getId().equals(passengerId)) {
            throw new UnauthorizedException("You don't have access to cancel this ride");
        }

        rideCommandService.cancelRide(rideId);
        return "Ride cancelled";
    }

}
