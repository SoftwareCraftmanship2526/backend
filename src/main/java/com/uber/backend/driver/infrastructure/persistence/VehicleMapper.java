package com.uber.backend.driver.infrastructure.persistence;

import com.uber.backend.driver.domain.model.Vehicle;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class VehicleMapper {

    public Vehicle toDomain(VehicleEntity entity) {
        if (entity == null) {
            return null;
        }

        return Vehicle.builder()
                .id(entity.getId())
                .licensePlate(entity.getLicensePlate())
                .model(entity.getModel())
                .color(entity.getColor())
                .type(entity.getType())
                .driverId(entity.getDriver() != null ? entity.getDriver().getId() : null)
                .rideIds(entity.getRides().stream()
                        .map(RideEntity::getId)
                        .collect(Collectors.toList()))
                .build();
    }

    public VehicleEntity toEntity(Vehicle domain) {
        if (domain == null) {
            return null;
        }

        VehicleEntity entity = new VehicleEntity();
        entity.setId(domain.getId());
        entity.setLicensePlate(domain.getLicensePlate());
        entity.setModel(domain.getModel());
        entity.setColor(domain.getColor());
        entity.setType(domain.getType());
        return entity;
    }

    public void updateEntity(Vehicle domain, VehicleEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setLicensePlate(domain.getLicensePlate());
        entity.setModel(domain.getModel());
        entity.setColor(domain.getColor());
        entity.setType(domain.getType());
    }
}
