package com.uber.backend.driver.application;

import com.uber.backend.driver.application.command.SetCurrentVehicleCommand;
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
public class SetCurrentVehicleCommandHandler {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public VehicleDTO handle(SetCurrentVehicleCommand command) {
        VehicleEntity vehicle = vehicleRepository.findById(command.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + command.vehicleId()));

        // Verify vehicle belongs to driver
        if (!vehicle.getDriver().getId().equals(command.driverId())) {
            throw new IllegalArgumentException("Vehicle does not belong to driver");
        }

        DriverEntity driver = vehicle.getDriver();
        driver.setCurrentVehicle(vehicle);
        driverRepository.save(driver);

        return mapToDTO(vehicle);
    }

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
