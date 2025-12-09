package com.uber.backend.driver.application;

import com.uber.backend.driver.application.command.DeleteVehicleCommand;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.driver.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteVehicleCommandHandler {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public void handle(DeleteVehicleCommand command) {
        VehicleEntity vehicle = vehicleRepository.findById(command.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + command.vehicleId()));

        // Verify vehicle belongs to driver
        if (!vehicle.getDriver().getId().equals(command.driverId())) {
            throw new IllegalArgumentException("Vehicle does not belong to driver");
        }

        DriverEntity driver = vehicle.getDriver();

        // If this is the current vehicle, unset it
        if (driver.getCurrentVehicle() != null && driver.getCurrentVehicle().getId().equals(command.vehicleId())) {
            driver.setCurrentVehicle(null);
            driverRepository.save(driver);
        }

        vehicleRepository.delete(vehicle);
    }
}
