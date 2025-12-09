package com.uber.backend.passenger.application;

import com.uber.backend.passenger.application.command.AddSavedAddressCommand;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AddSavedAddressCommandHandler {

    private final PassengerRepository passengerRepository;

    @Transactional
    public void handle(AddSavedAddressCommand command) {
        PassengerEntity passenger = passengerRepository.findById(command.passengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + command.passengerId()));

        if (passenger.getSavedAddresses() == null) {
            passenger.setSavedAddresses(new ArrayList<>());
        }

        if (!passenger.getSavedAddresses().contains(command.address())) {
            passenger.getSavedAddresses().add(command.address());
            passengerRepository.save(passenger);
        }
    }
}
