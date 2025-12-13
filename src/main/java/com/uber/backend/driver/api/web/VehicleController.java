package com.uber.backend.driver.api.web;

import com.uber.backend.auth.infrastructure.security.JwtUtil;
import com.uber.backend.driver.application.*;
import com.uber.backend.driver.application.command.AddVehicleCommand;
import com.uber.backend.driver.application.command.DeleteVehicleCommand;
import com.uber.backend.driver.application.command.SetCurrentVehicleCommand;
import com.uber.backend.driver.application.command.UpdateVehicleCommand;
import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.application.query.GetDriverVehiclesQuery;
import com.uber.backend.driver.application.query.GetVehicleByIdQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Vehicle Management", description = "Endpoints for managing driver vehicles")
@SecurityRequirement(name = "bearerAuth")
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
     *
     * @param command Vehicle details (license plate, model, color, type)
     * @param httpRequest HTTP request containing JWT token
     * @return Created vehicle information
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    @Operation(
            summary = "Add New Vehicle",
            description = "Register a new vehicle for the authenticated driver. Driver can have multiple vehicles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Vehicle added successfully",
                    content = @Content(schema = @Schema(implementation = VehicleDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid vehicle data or duplicate license plate"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - driver role required"
            )
    })
    public ResponseEntity<VehicleDTO> addVehicle(
            @Valid @RequestBody AddVehicleCommand command,
            HttpServletRequest httpRequest
    ) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        AddVehicleCommand commandWithDriverId = new AddVehicleCommand(
                driverId,
                command.licensePlate(),
                command.model(),
                command.color(),
                command.type()
        );
        VehicleDTO vehicle = addVehicleHandler.handle(commandWithDriverId);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    /**
     * Get all vehicles for the authenticated driver.
     *
     * @param httpRequest HTTP request containing JWT token
     * @return List of all vehicles owned by the driver
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    @Operation(
            summary = "Get My Vehicles",
            description = "Retrieve all vehicles registered to the authenticated driver"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vehicles retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VehicleDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - driver role required"
            )
    })
    public ResponseEntity<List<VehicleDTO>> getMyVehicles(HttpServletRequest httpRequest) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        GetDriverVehiclesQuery query = new GetDriverVehiclesQuery(driverId);
        List<VehicleDTO> vehicles = getDriverVehiclesHandler.handle(query);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get a specific vehicle by ID.
     *
     * @param vehicleId ID of the vehicle to retrieve
     * @return Vehicle information
     */
    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    @Operation(
            summary = "Get Vehicle by ID",
            description = "Retrieve detailed information about a specific vehicle"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vehicle found",
                    content = @Content(schema = @Schema(implementation = VehicleDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - driver role required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found"
            )
    })
    public ResponseEntity<VehicleDTO> getVehicle(@PathVariable Long vehicleId) {
        GetVehicleByIdQuery query = new GetVehicleByIdQuery(vehicleId);
        VehicleDTO vehicle = getVehicleByIdHandler.handle(query);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Update vehicle information.
     *
     * @param vehicleId ID of the vehicle to update
     * @param command Updated vehicle details (model, color, type)
     * @param httpRequest HTTP request containing JWT token
     * @return Updated vehicle information
     */
    @PutMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    @Operation(
            summary = "Update Vehicle",
            description = "Update the details of an existing vehicle. Only the vehicle owner can update it."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vehicle updated successfully",
                    content = @Content(schema = @Schema(implementation = VehicleDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid vehicle data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - must be the vehicle owner"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found"
            )
    })
    public ResponseEntity<VehicleDTO> updateVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody UpdateVehicleCommand command,
            HttpServletRequest httpRequest
    ) {
        Long driverId = jwtUtil.extractUserIdFromRequest(httpRequest);
        UpdateVehicleCommand commandWithIds = new UpdateVehicleCommand(
                vehicleId,
                driverId,
                command.model(),
                command.color(),
                command.type()
        );
        VehicleDTO vehicle = updateVehicleHandler.handle(commandWithIds);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Delete a vehicle.
     *
     * @param vehicleId ID of the vehicle to delete
     * @param httpRequest HTTP request containing JWT token
     * @return No content on success
     */
    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    @Operation(
            summary = "Delete Vehicle",
            description = "Remove a vehicle from the driver's registered vehicles. Only the vehicle owner can delete it."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Vehicle deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - must be the vehicle owner"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found"
            )
    })
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
     *
     * @param vehicleId ID of the vehicle to set as current
     * @param httpRequest HTTP request containing JWT token
     * @return Updated vehicle information
     */
    @PutMapping("/{vehicleId}/set-current")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    @Operation(
            summary = "Set Current Vehicle",
            description = "Set a specific vehicle as the driver's active vehicle for accepting rides"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Current vehicle updated successfully",
                    content = @Content(schema = @Schema(implementation = VehicleDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - must be the vehicle owner"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found"
            )
    })
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
