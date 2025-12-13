package com.uber.backend.ride.api;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.ride.application.*;
import com.uber.backend.ride.application.command.*;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.application.exception.UnauthorizedException;
import com.uber.backend.ride.application.query.PriceRequestQuery;
import com.uber.backend.ride.application.query.RideQueryService;
import com.uber.backend.ride.domain.enums.RideType;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.applicaition.CheckRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "Ride", description = "Ride management APIs")
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

    /**
     * Request a new ride by an authenticated passenger.
     *
     * @param command Ride request details
     * @param httpRequest HTTP request containing JWT token
     * @return Ride request result including ride ID and status
     */
    @Operation(
            summary = "Request a ride",
            description = "Allows a passenger to request a new ride",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride requested successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or not a passenger"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/request")
    public ResponseEntity<RideRequestResult> requestRide(
            @RequestBody RequestRideCommand command,
            HttpServletRequest httpRequest
    ) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isPassenger(passengerId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        RideRequestResult response = requestRideCommandHandler.handle(command, passengerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Assign a driver to a ride.
     *
     * @param command Driver assignment details
     * @param httpRequest HTTP request containing JWT token
     * @return Driver assignment result
     */
    @Operation(
            summary = "Assign driver to ride",
            description = "Allows a passenger to assign a driver to their ride",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver assigned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    @PostMapping("/assign")
    public RideAssignResult assignDriver(
            @RequestBody DriverAssignCommand command,
            HttpServletRequest httpRequest
    ) {
        Long passengerId = jwtUtil.extractUserIdFromRequest(httpRequest);
        return driverAssignCommandHandler.handle(command, passengerId);
    }

    /**
     * Cancel an existing ride.
     *
     * @param command Ride cancellation details
     * @param httpRequest HTTP request containing JWT token
     * @return Cancellation confirmation message
     */
    @Operation(
            summary = "Cancel ride",
            description = "Allows a passenger to cancel their own ride",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or access denied"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    @PostMapping("/cancel")
    public String cancelRide(
            @RequestBody CancelRideCommand command,
            HttpServletRequest httpRequest
    ) {
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

    /**
     * Complete a ride.
     *
     * @param command Ride completion details
     * @param httpRequest HTTP request containing JWT token
     * @return Completed ride entity
     */
    @Operation(
            summary = "Complete ride",
            description = "Allows a driver to complete a ride",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or not a driver"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    @PostMapping("/complete")
    public RideEntity completeRide(
            @RequestBody CompleteRideCommand command,
            HttpServletRequest httpRequest
    ) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return completeRideCommandHandler.handle(command);
    }

    /**
     * Accept a ride.
     *
     * @param command Ride acceptance details
     * @param httpRequest HTTP request containing JWT token
     * @return Updated ride entity
     */
    @Operation(
            summary = "Accept ride",
            description = "Allows a driver to accept an assigned ride",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride accepted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or not a driver"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    @PostMapping("/accept")
    public RideEntity acceptDriver(
            @RequestBody DriverAcceptCommand command,
            HttpServletRequest httpRequest
    ) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return driverAcceptCommandHandler.handle(command);
    }

    /**
     * Calculate ride price estimation.
     *
     * @param request Pricing request details
     * @return Price estimation breakdown
     */
    @Operation(
            summary = "Get ride price estimation",
            description = "Calculates the estimated price of a ride"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Price calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pricing parameters")
    })
    @PostMapping("/price")
    public Map<RideType, BigDecimal> priceRequest(
            @RequestBody PriceRequestQuery request
    ) {
        return rideQueryService.priceRequest(
                request.getStart(),
                request.getEnd(),
                request.getDurationMin(),
                request.getDemandMultiplier()
        );
    }
}
