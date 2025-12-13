package com.uber.backend.driver.infrastructure.pollers;

import com.uber.backend.driver.application.service.PollAvailableDriversService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DriverPoller {
    private final PollAvailableDriversService pollAvailableDriversService;

    @Scheduled(fixedRate = 5000)
    public void pollDrivers() {
        pollAvailableDriversService.pollForAvailableDriversForAllRides();
    }
}
