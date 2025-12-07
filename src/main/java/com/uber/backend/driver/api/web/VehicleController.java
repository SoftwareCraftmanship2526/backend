package com.uber.backend.driver.api.web;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.driver.application.dto.AddVehicleRequest;
import com.uber.backend.driver.application.dto.UpdateVehicleRequest;
import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.application.service.VehicleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for vehicle management.
 * Allows drivers to manage their vehicles.
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final JwtUtil jwtUtil;

    /**
     * Add a new vehicle for the authenticated driver.
     */
    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<VehicleDTO> addVehicle(
            @Valid @RequestBody AddVehicleRequest request,
            HttpServletRequest httpRequest
    ) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        VehicleDTO vehicle = vehicleService.addVehicle(driverId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    /**
     * Get all vehicles for the authenticated driver.
     */
    @GetMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<VehicleDTO>> getMyVehicles(HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        List<VehicleDTO> vehicles = vehicleService.getDriverVehicles(driverId);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get a specific vehicle by ID.
     */
    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<VehicleDTO> getVehicle(@PathVariable Long vehicleId) {
        VehicleDTO vehicle = vehicleService.getVehicleById(vehicleId);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Update vehicle information.
     */
    @PutMapping("/{vehicleId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<VehicleDTO> updateVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody UpdateVehicleRequest request,
            HttpServletRequest httpRequest
    ) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        VehicleDTO vehicle = vehicleService.updateVehicle(vehicleId, driverId, request);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Delete a vehicle.
     */
    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable Long vehicleId,
            HttpServletRequest httpRequest
    ) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        vehicleService.deleteVehicle(vehicleId, driverId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set a vehicle as the current vehicle.
     */
    @PutMapping("/{vehicleId}/set-current")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<VehicleDTO> setCurrentVehicle(
            @PathVariable Long vehicleId,
            HttpServletRequest httpRequest
    ) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        VehicleDTO vehicle = vehicleService.setCurrentVehicle(vehicleId, driverId);
        return ResponseEntity.ok(vehicle);
    }
}
