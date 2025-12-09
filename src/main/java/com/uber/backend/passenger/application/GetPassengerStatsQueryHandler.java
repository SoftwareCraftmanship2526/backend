package com.uber.backend.passenger.application;

import com.uber.backend.passenger.application.dto.PassengerDTO;
import com.uber.backend.passenger.application.query.GetPassengerStatsQuery;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPassengerStatsQueryHandler {

    private final PassengerRepository passengerRepository;

    public PassengerDTO handle(GetPassengerStatsQuery query) {
        PassengerEntity passenger = passengerRepository.findById(query.passengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + query.passengerId()));

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
