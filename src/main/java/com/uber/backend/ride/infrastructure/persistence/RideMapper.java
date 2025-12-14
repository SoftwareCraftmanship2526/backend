package com.uber.backend.ride.infrastructure.persistence;

import com.uber.backend.ride.domain.model.Ride;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {

    public Ride toDomain(RideEntity entity) {
        if (entity == null) {
            return null;
        }

        return Ride.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .fareAmount(entity.getPayment() != null ? entity.getPayment().getAmount() : null)
                .pickupLocation(entity.getPickupLocation())
                .dropoffLocation(entity.getDropoffLocation())
                .passengerId(entity.getPassenger() != null ? entity.getPassenger().getId() : null)
                .driverId(entity.getDriver() != null ? entity.getDriver().getId() : null)
                .vehicleId(entity.getVehicle() != null ? entity.getVehicle().getId() : null)
                .paymentId(entity.getPayment() != null ? entity.getPayment().getId() : null)
                .build();
    }

    public RideEntity toEntity(Ride domain) {
        if (domain == null) {
            return null;
        }

        RideEntity entity = new RideEntity();
        entity.setId(domain.getId());
        entity.setStatus(domain.getStatus());
        entity.setRequestedAt(domain.getRequestedAt());
        entity.setStartedAt(domain.getStartedAt());
        entity.setCompletedAt(domain.getCompletedAt());
        // fareAmount is stored in PaymentEntity, not RideEntity
        entity.setPickupLocation(domain.getPickupLocation());
        entity.setDropoffLocation(domain.getDropoffLocation());
        return entity;
    }

    public void updateEntity(Ride domain, RideEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setStatus(domain.getStatus());
        entity.setRequestedAt(domain.getRequestedAt());
        entity.setStartedAt(domain.getStartedAt());
        entity.setCompletedAt(domain.getCompletedAt());
        entity.setPickupLocation(domain.getPickupLocation());
        entity.setDropoffLocation(domain.getDropoffLocation());
    }
}
