package com.uber.backend.driver.application.service;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.shared.domain.port.GeocodingPort;
import com.uber.backend.shared.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing driver availability (online/offline status).
 */
@Service
@RequiredArgsConstructor
public class DriverAvailabilityService {

    private final DriverRepository driverRepository;
    private final GeocodingPort geocodingPort;

    /**
     * Set driver as online (available for rides).
     * @return The resolved address where driver went online
     */
    @Transactional
    public String goOnline(Long driverId, Double latitude, Double longitude) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));

        // Set driver as available
        driver.setIsAvailable(true);
        
        // Resolve address from coordinates and set initial location
        String address = geocodingPort.getAddressFromCoordinates(latitude, longitude);
        Location currentLocation = new Location(latitude, longitude, address);
        driver.setCurrentLocation(currentLocation);

        driverRepository.save(driver);
        return address;
    }

    /**
     * Set driver as offline (not available for rides).
     */
    @Transactional
    public void goOffline(Long driverId) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));

        // Set driver as unavailable
        driver.setIsAvailable(false);

        driverRepository.save(driver);
    }

    /**
     * Update driver's current location.
     * Only updates if driver is online.
     * @return The resolved address of the new location
     */
    @Transactional
    public String updateLocation(Long driverId, Double latitude, Double longitude) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));

        if (!driver.getIsAvailable()) {
            throw new IllegalArgumentException("Cannot update location while offline. Please go online first.");
        }

        // Resolve address from coordinates and update location
        String address = geocodingPort.getAddressFromCoordinates(latitude, longitude);
        Location currentLocation = new Location(latitude, longitude, address);
        driver.setCurrentLocation(currentLocation);

        driverRepository.save(driver);
        return address;
    }
}
