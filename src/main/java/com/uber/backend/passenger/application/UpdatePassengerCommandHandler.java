package com.uber.backend.passenger.application;

import com.uber.backend.passenger.application.command.UpdatePassengerCommand;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePassengerCommandHandler {

    private final PassengerRepository passengerRepository;

    @Transactional
    public void handle(UpdatePassengerCommand command) {
        PassengerEntity passenger = passengerRepository.findById(command.passengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + command.passengerId()));

        if (command.firstName() != null) {
            passenger.setFirstName(command.firstName());
        }
        if (command.lastName() != null) {
            passenger.setLastName(command.lastName());
        }
        if (command.phoneNumber() != null) {
            passenger.setPhoneNumber(command.phoneNumber());
        }

        passengerRepository.save(passenger);
    }
}
