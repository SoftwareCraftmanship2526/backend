package com.uber.backend.driver.infrastructure.persistence;

import com.uber.backend.driver.domain.model.Driver;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DriverMapper {

    public Driver toDomain(DriverEntity entity) {
        if (entity == null) {
            return null;
        }

        return Driver.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .phoneNumber(entity.getPhoneNumber())
                .createdAt(entity.getCreatedAt())
                .driverRating(entity.getDriverRating())
                .isAvailable(entity.getIsAvailable())
                .licenseNumber(entity.getLicenseNumber())
                .currentLocation(entity.getCurrentLocation())
                .currentVehicleId(entity.getCurrentVehicle() != null ? entity.getCurrentVehicle().getId() : null)
                .rideIds(entity.getRides().stream()
                        .map(RideEntity::getId)
                        .collect(Collectors.toList()))
                .build();
    }

    public DriverEntity toEntity(Driver domain) {
        if (domain == null) {
            return null;
        }

        DriverEntity entity = new DriverEntity();
        entity.setId(domain.getId());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setDriverRating(domain.getDriverRating());
        entity.setIsAvailable(domain.getIsAvailable());
        entity.setLicenseNumber(domain.getLicenseNumber());
        entity.setCurrentLocation(domain.getCurrentLocation());
        return entity;
    }

    public void updateEntity(Driver domain, DriverEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setDriverRating(domain.getDriverRating());
        entity.setIsAvailable(domain.getIsAvailable());
        entity.setLicenseNumber(domain.getLicenseNumber());
        entity.setCurrentLocation(domain.getCurrentLocation());
    }
}
