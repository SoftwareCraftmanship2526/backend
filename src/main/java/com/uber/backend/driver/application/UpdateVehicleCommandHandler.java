package com.uber.backend.driver.application;

import com.uber.backend.driver.application.command.UpdateVehicleCommand;
import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateVehicleCommandHandler {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleDTO handle(UpdateVehicleCommand command) {
        VehicleEntity vehicle = vehicleRepository.findById(command.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + command.vehicleId()));

        // Verify vehicle belongs to driver
        if (!vehicle.getDriver().getId().equals(command.driverId())) {
            throw new IllegalArgumentException("Vehicle does not belong to driver");
        }

        if (command.model() != null) {
            vehicle.setModel(command.model());
        }
        if (command.color() != null) {
            vehicle.setColor(command.color());
        }
        if (command.type() != null) {
            vehicle.setType(command.type());
        }

        vehicle = vehicleRepository.save(vehicle);
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
