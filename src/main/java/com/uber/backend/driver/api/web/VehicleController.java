package com.uber.backend.driver.api.web;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.driver.application.*;
import com.uber.backend.driver.application.command.AddVehicleCommand;
import com.uber.backend.driver.application.command.DeleteVehicleCommand;
import com.uber.backend.driver.application.command.SetCurrentVehicleCommand;
import com.uber.backend.driver.application.command.UpdateVehicleCommand;
import com.uber.backend.driver.application.dto.AddVehicleRequest;
import com.uber.backend.driver.application.dto.UpdateVehicleRequest;
import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.application.query.GetDriverVehiclesQuery;
import com.uber.backend.driver.application.query.GetVehicleByIdQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for vehicle management.
 * Allows drivers to manage their vehicles using CQRS pattern.
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final AddVehicleCommandHandler addVehicleHandler;
    private final UpdateVehicleCommandHandler updateVehicleHandler;
    private final DeleteVehicleCommandHandler deleteVehicleHandler;
    private final SetCurrentVehicleCommandHandler setCurrentVehicleHandler;
    private final GetDriverVehiclesQueryHandler getDriverVehiclesHandler;
    private final GetVehicleByIdQueryHandler getVehicleByIdHandler;
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
        AddVehicleCommand command = new AddVehicleCommand(
                driverId,
                request.getLicensePlate(),
                request.getModel(),
                request.getColor(),
                request.getType()
        );
        VehicleDTO vehicle = addVehicleHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    /**
     * Get all vehicles for the authenticated driver.
     */
    @GetMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<VehicleDTO>> getMyVehicles(HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        GetDriverVehiclesQuery query = new GetDriverVehiclesQuery(driverId);
        List<VehicleDTO> vehicles = getDriverVehiclesHandler.handle(query);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get a specific vehicle by ID.
     */
    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<VehicleDTO> getVehicle(@PathVariable Long vehicleId) {
        GetVehicleByIdQuery query = new GetVehicleByIdQuery(vehicleId);
        VehicleDTO vehicle = getVehicleByIdHandler.handle(query);
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
        UpdateVehicleCommand command = new UpdateVehicleCommand(
                vehicleId,
                driverId,
                request.getModel(),
                request.getColor(),
                request.getType()
        );
        VehicleDTO vehicle = updateVehicleHandler.handle(command);
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
        DeleteVehicleCommand command = new DeleteVehicleCommand(vehicleId, driverId);
        deleteVehicleHandler.handle(command);
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
        SetCurrentVehicleCommand command = new SetCurrentVehicleCommand(vehicleId, driverId);
        VehicleDTO vehicle = setCurrentVehicleHandler.handle(command);
        return ResponseEntity.ok(vehicle);
    }
}
