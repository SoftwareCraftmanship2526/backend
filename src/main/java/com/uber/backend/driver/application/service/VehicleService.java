package com.uber.backend.driver.application.service;

import com.uber.backend.driver.application.dto.AddVehicleRequest;
import com.uber.backend.driver.application.dto.UpdateVehicleRequest;
import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.driver.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for vehicle operations.
 * Handles vehicle management for drivers.
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    /**
     * Add a new vehicle for a driver.
     */
    @Transactional
    public VehicleDTO addVehicle(Long driverId, AddVehicleRequest request) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));

        // Check if license plate already exists
        if (vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new IllegalArgumentException("Vehicle with license plate " + request.getLicensePlate() + " already exists");
        }

        VehicleEntity vehicle = VehicleEntity.builder()
                .licensePlate(request.getLicensePlate().toUpperCase())
                .model(request.getModel())
                .color(request.getColor())
                .type(request.getType())
                .driver(driver)
                .build();

        vehicle = vehicleRepository.save(vehicle);

        // Set as current vehicle if driver doesn't have one
        if (driver.getCurrentVehicle() == null) {
            driver.setCurrentVehicle(vehicle);
            driverRepository.save(driver);
        }

        return mapToDTO(vehicle);
    }

    /**
     * Get all vehicles for a driver.
     */
    public List<VehicleDTO> getDriverVehicles(Long driverId) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));

        return vehicleRepository.findByDriverId(driverId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle by ID.
     */
    public VehicleDTO getVehicleById(Long vehicleId) {
        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));
        
        return mapToDTO(vehicle);
    }

    /**
     * Update vehicle information.
     */
    @Transactional
    public VehicleDTO updateVehicle(Long vehicleId, Long driverId, UpdateVehicleRequest request) {
        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));

        // Verify vehicle belongs to driver
        if (!vehicle.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("Vehicle does not belong to driver");
        }

        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }
        if (request.getColor() != null) {
            vehicle.setColor(request.getColor());
        }
        if (request.getType() != null) {
            vehicle.setType(request.getType());
        }

        vehicle = vehicleRepository.save(vehicle);
        return mapToDTO(vehicle);
    }

    /**
     * Delete a vehicle.
     */
    @Transactional
    public void deleteVehicle(Long vehicleId, Long driverId) {
        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));

        // Verify vehicle belongs to driver
        if (!vehicle.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("Vehicle does not belong to driver");
        }

        DriverEntity driver = vehicle.getDriver();
        
        // If this is the current vehicle, unset it
        if (driver.getCurrentVehicle() != null && driver.getCurrentVehicle().getId().equals(vehicleId)) {
            driver.setCurrentVehicle(null);
            driverRepository.save(driver);
        }

        vehicleRepository.delete(vehicle);
    }

    /**
     * Set a vehicle as the driver's current vehicle.
     */
    @Transactional
    public VehicleDTO setCurrentVehicle(Long vehicleId, Long driverId) {
        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));

        // Verify vehicle belongs to driver
        if (!vehicle.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("Vehicle does not belong to driver");
        }

        DriverEntity driver = vehicle.getDriver();
        driver.setCurrentVehicle(vehicle);
        driverRepository.save(driver);

        return mapToDTO(vehicle);
    }

    /**
     * Map vehicle entity to DTO.
     */
    private VehicleDTO mapToDTO(VehicleEntity vehicle) {
        return VehicleDTO.builder()
                .id(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .type(vehicle.getType())
                .driverId(vehicle.getDriver() != null ? vehicle.getDriver().getId() : null)
                .build();
    }
}
