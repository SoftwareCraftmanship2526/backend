package com.uber.backend.driver.application;

import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.application.query.GetDriverVehiclesQuery;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.driver.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetDriverVehiclesQueryHandler {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public List<VehicleDTO> handle(GetDriverVehiclesQuery query) {
        // Verify driver exists
        driverRepository.findById(query.driverId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + query.driverId()));

        return vehicleRepository.findByDriverId(query.driverId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
