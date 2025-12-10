package com.uber.backend.passenger.application;

import com.uber.backend.passenger.application.dto.PassengerDTO;
import com.uber.backend.passenger.application.query.GetPassengerByIdQuery;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPassengerByIdQueryHandler {

    private final PassengerRepository passengerRepository;

    public PassengerDTO handle(GetPassengerByIdQuery query) {
        PassengerEntity passenger = passengerRepository.findById(query.passengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + query.passengerId()));

        return mapToDTO(passenger);
    }

    private PassengerDTO mapToDTO(PassengerEntity passenger) {
        return new PassengerDTO(
                passenger.getId(),
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getEmail(),
                passenger.getPhoneNumber(),
                passenger.getPassengerRating(),
                passenger.getSavedAddresses(),
                passenger.getRides() != null ? passenger.getRides().size() : 0
        );
    }
}
