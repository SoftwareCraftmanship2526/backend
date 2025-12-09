package com.uber.backend.driver.application;

import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.application.query.GetVehicleByIdQuery;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetVehicleByIdQueryHandler {

    private final VehicleRepository vehicleRepository;

    public VehicleDTO handle(GetVehicleByIdQuery query) {
        VehicleEntity vehicle = vehicleRepository.findById(query.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + query.vehicleId()));

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
