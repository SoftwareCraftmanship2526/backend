package com.uber.backend.driver.application;

import com.uber.backend.driver.application.command.AddVehicleCommand;
import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.driver.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddVehicleCommandHandler {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public VehicleDTO handle(AddVehicleCommand command) {
        DriverEntity driver = driverRepository.findById(command.driverId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + command.driverId()));

        // Check if license plate already exists
        if (vehicleRepository.findByLicensePlate(command.licensePlate()).isPresent()) {
            throw new IllegalArgumentException("Vehicle with license plate " + command.licensePlate() + " already exists");
        }

        VehicleEntity vehicle = VehicleEntity.builder()
                .licensePlate(command.licensePlate().toUpperCase())
                .model(command.model())
                .color(command.color())
                .type(command.type())
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

    private VehicleDTO mapToDTO(VehicleEntity vehicle) {
        return new VehicleDTO(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getType(),
                vehicle.getDriver() != null ? vehicle.getDriver().getId() : null
        );
    }
}
