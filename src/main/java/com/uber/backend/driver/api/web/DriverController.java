package com.uber.backend.driver.api.web;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.driver.application.command.GoOnlineCommand;
import com.uber.backend.driver.application.command.UpdateLocationCommand;
import com.uber.backend.driver.application.service.DriverAvailabilityService;
import com.uber.backend.ride.application.exception.UnauthorizedException;
import com.uber.backend.shared.application.CheckRoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for driver availability management.
 */
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver", description = "Driver availability and location management APIs")
@SecurityRequirement(name = "bearerAuth")
public class DriverController {

    private final DriverAvailabilityService driverAvailabilityService;
    private final JwtUtil jwtUtil;
    private final CheckRoleService checkRoleService;

    @PostMapping("/go-online")
    @Operation(summary = "Go online", description = "Driver goes online and becomes available for ride requests. Requires initial location.")
    public ResponseEntity<String> goOnline(@Valid @RequestBody GoOnlineCommand command, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Only drivers can go online. Please log in as a driver.");
        }
        
        String address = driverAvailabilityService.goOnline(driverId, command.latitude(), command.longitude());
        return ResponseEntity.ok("Driver is now online and available for rides at " + address);
    }

    @PostMapping("/go-offline")
    @Operation(summary = "Go offline", description = "Driver goes offline and stops receiving ride requests")
    public ResponseEntity<String> goOffline(HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Only drivers can go offline. Please log in as a driver.");
        }
        
        driverAvailabilityService.goOffline(driverId);
        return ResponseEntity.ok("Driver is now offline");
    }

    @PostMapping("/update-location")
    @Operation(summary = "Update location", description = "Update driver's current location. Should be called every 5 seconds while online.")
    public ResponseEntity<String> updateLocation(@Valid @RequestBody UpdateLocationCommand command, HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        if (!checkRoleService.isDriver(driverId)) {
            throw new UnauthorizedException("Only drivers can update location. Please log in as a driver.");
        }
        
        String address = driverAvailabilityService.updateLocation(driverId, command.latitude(), command.longitude());
        return ResponseEntity.ok("Location updated to " + address);
    }
}
