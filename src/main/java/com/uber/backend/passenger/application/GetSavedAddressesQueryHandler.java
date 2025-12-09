package com.uber.backend.passenger.application;

import com.uber.backend.passenger.application.query.GetSavedAddressesQuery;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSavedAddressesQueryHandler {

    private final PassengerRepository passengerRepository;

    public List<String> handle(GetSavedAddressesQuery query) {
        PassengerEntity passenger = passengerRepository.findById(query.passengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + query.passengerId()));

        return passenger.getSavedAddresses() != null
                ? new ArrayList<>(passenger.getSavedAddresses())
                : new ArrayList<>();
    }
}
