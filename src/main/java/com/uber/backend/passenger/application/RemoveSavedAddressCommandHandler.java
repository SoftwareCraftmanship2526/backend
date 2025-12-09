package com.uber.backend.passenger.application;

import com.uber.backend.passenger.application.command.RemoveSavedAddressCommand;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveSavedAddressCommandHandler {

    private final PassengerRepository passengerRepository;

    @Transactional
    public void handle(RemoveSavedAddressCommand command) {
        PassengerEntity passenger = passengerRepository.findById(command.passengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + command.passengerId()));

        if (passenger.getSavedAddresses() != null) {
            passenger.getSavedAddresses().remove(command.address());
            passengerRepository.save(passenger);
        }
    }
}
