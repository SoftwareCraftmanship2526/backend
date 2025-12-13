package com.uber.backend.shared.application;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class CheckRoleService {
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public boolean isPassenger(Long id) {
        PassengerEntity passenger = passengerRepository.findById(id).orElse(null);
        return passenger != null;
    }

    public boolean isDriver(Long id) {
        DriverEntity driver = driverRepository.findById(id).orElse(null);
        return driver != null;
    }
}
