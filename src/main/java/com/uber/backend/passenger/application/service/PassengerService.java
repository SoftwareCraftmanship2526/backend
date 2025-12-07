package com.uber.backend.passenger.application.service;

import com.uber.backend.passenger.application.dto.PassengerDTO;
import com.uber.backend.passenger.application.dto.UpdatePassengerRequest;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for passenger operations.
 * Handles passenger profile management and saved addresses.
 */
@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;

    /**
     * Get passenger by ID.
     * 
     * @param passengerId Passenger ID
     * @return Passenger DTO
     * @throws IllegalArgumentException if passenger not found
     */
    public PassengerDTO getPassengerById(Long passengerId) {
        PassengerEntity passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + passengerId));
        
        return mapToDTO(passenger);
    }

    /**
     * Update passenger profile.
     * 
     * @param passengerId Passenger ID
     * @param request Update request
     * @return Updated passenger DTO
     */
    @Transactional
    public PassengerDTO updatePassenger(Long passengerId, UpdatePassengerRequest request) {
        PassengerEntity passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + passengerId));
        
        if (request.getFirstName() != null) {
            passenger.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            passenger.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            passenger.setPhoneNumber(request.getPhoneNumber());
        }
        
        passenger = passengerRepository.save(passenger);
        return mapToDTO(passenger);
    }

    /**
     * Add a saved address for a passenger.
     * 
     * @param passengerId Passenger ID
     * @param address Address to save
     * @return Updated passenger DTO
     */
    @Transactional
    public PassengerDTO addSavedAddress(Long passengerId, String address) {
        PassengerEntity passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + passengerId));
        
        if (passenger.getSavedAddresses() == null) {
            passenger.setSavedAddresses(new ArrayList<>());
        }
        
        if (!passenger.getSavedAddresses().contains(address)) {
            passenger.getSavedAddresses().add(address);
            passenger = passengerRepository.save(passenger);
        }
        
        return mapToDTO(passenger);
    }

    /**
     * Remove a saved address for a passenger.
     * 
     * @param passengerId Passenger ID
     * @param address Address to remove
     * @return Updated passenger DTO
     */
    @Transactional
    public PassengerDTO removeSavedAddress(Long passengerId, String address) {
        PassengerEntity passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + passengerId));
        
        if (passenger.getSavedAddresses() != null) {
            passenger.getSavedAddresses().remove(address);
        }
        
        passenger = passengerRepository.save(passenger);
        
        return mapToDTO(passenger);
    }

    /**
     * Get all saved addresses for a passenger.
     * 
     * @param passengerId Passenger ID
     * @return  List of saved addresses
     */
    public List<String> getSavedAddresses(Long passengerId) {
        PassengerEntity passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + passengerId));
        
        return passenger.getSavedAddresses() != null ? 
                new ArrayList<>(passenger.getSavedAddresses()) : new ArrayList<>();
    }

    /**
     * Get passenger statistics.
     * 
     * @param passengerId Passenger ID
     * @return Passenger DTO with statistics
     */
    public PassengerDTO getPassengerStats(Long passengerId) {
        PassengerEntity passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with ID: " + passengerId));
        
        return mapToDTO(passenger);
    }

    /**
     * Map passenger entity to DTO.
     */
    private PassengerDTO mapToDTO(PassengerEntity passenger) {
        return PassengerDTO.builder()
                .id(passenger.getId())
                .firstName(passenger.getFirstName())
                .lastName(passenger.getLastName())
                .email(passenger.getEmail())
                .phoneNumber(passenger.getPhoneNumber())
                .passengerRating(passenger.getPassengerRating())
                .savedAddresses(passenger.getSavedAddresses())
                .totalRides(passenger.getRides() != null ? passenger.getRides().size() : 0)
                .build();
    }
}
