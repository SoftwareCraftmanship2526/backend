package com.uber.backend.passenger.infrastructure.persistence;

import com.uber.backend.passenger.domain.model.Passenger;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PassengerMapper {

    public Passenger toDomain(PassengerEntity entity) {
        if (entity == null) {
            return null;
        }

        return Passenger.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .phoneNumber(entity.getPhoneNumber())
                .createdAt(entity.getCreatedAt())
                .passengerRating(entity.getPassengerRating())
                .savedAddresses(entity.getSavedAddresses())
                .rideIds(entity.getRides().stream()
                        .map(RideEntity::getId)
                        .collect(Collectors.toList()))
                .build();
    }

    public PassengerEntity toEntity(Passenger domain) {
        if (domain == null) {
            return null;
        }

        PassengerEntity entity = new PassengerEntity();
        entity.setId(domain.getId());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setPassengerRating(domain.getPassengerRating());
        entity.setSavedAddresses(domain.getSavedAddresses());
        return entity;
    }

    public void updateEntity(Passenger domain, PassengerEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setPassengerRating(domain.getPassengerRating());
        entity.setSavedAddresses(domain.getSavedAddresses());
    }
}
