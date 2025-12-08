package com.uber.backend.passenger.application;

import com.uber.backend.passenger.application.dto.PassengerDTO;
import com.uber.backend.passenger.application.dto.UpdatePassengerRequest;
import com.uber.backend.passenger.application.service.PassengerService;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for PassengerService.
 */
@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private PassengerService passengerService;

    private PassengerEntity testPassenger;

    @BeforeEach
    void setUp() {
        testPassenger = new PassengerEntity();
        testPassenger.setId(1L);
        testPassenger.setFirstName("John");
        testPassenger.setLastName("Doe");
        testPassenger.setEmail("john.doe@example.com");
        testPassenger.setPhoneNumber("+1234567890");
        testPassenger.setPassengerRating(4.5);
        testPassenger.setSavedAddresses(new ArrayList<>(Arrays.asList(
                "123 Main St",
                "456 Oak Ave"
        )));
        // Create mock rides using RideEntity
        RideEntity ride1 = new RideEntity();
        RideEntity ride2 = new RideEntity();
        RideEntity ride3 = new RideEntity();
        testPassenger.setRides(new ArrayList<>(Arrays.asList(ride1, ride2, ride3)));
    }

    @Test
    void givenPassengerId_whenGetPassengerById_thenSuccess() {
        // Arrange
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        PassengerDTO result = passengerService.getPassengerById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(4.5, result.getPassengerRating());
        assertEquals(2, result.getSavedAddresses().size());
        assertEquals(3, result.getTotalRides());

        verify(passengerRepository).findById(1L);
    }

    @Test
    void givenPassengerId_whenGetPassengerById_thenNotFound_ThrowsException() {
        // Arrange
        when(passengerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> passengerService.getPassengerById(999L)
        );

        assertTrue(exception.getMessage().contains("Passenger not found"));
    }

    @Test
    void givenPassengerId_whenUpdatePassenger_thenSuccess() {
        // Arrange
        UpdatePassengerRequest request = UpdatePassengerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+0987654321")
                .build();

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        PassengerDTO result = passengerService.updatePassenger(1L, request);

        // Assert
        assertNotNull(result);
        
        ArgumentCaptor<PassengerEntity> captor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(captor.capture());
        
        PassengerEntity saved = captor.getValue();
        assertEquals("Jane", saved.getFirstName());
        assertEquals("Smith", saved.getLastName());
        assertEquals("+0987654321", saved.getPhoneNumber());
    }

    @Test
    void givenPassengerId_whenUpdatePassenger_thenPartialUpdate() {
        // Arrange
        UpdatePassengerRequest request = UpdatePassengerRequest.builder()
                .firstName("Jane")
                .build();

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        passengerService.updatePassenger(1L, request);

        // Assert
        ArgumentCaptor<PassengerEntity> captor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(captor.capture());
        
        PassengerEntity saved = captor.getValue();
        assertEquals("Jane", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertEquals("+1234567890", saved.getPhoneNumber());
    }

    @Test
    void givenPassengerId_whenAddSavedAddress_thenSuccess() {
        // Arrange
        String newAddress = "789 Elm St";
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        PassengerDTO result = passengerService.addSavedAddress(1L, newAddress);

        // Assert
        assertNotNull(result);
        
        ArgumentCaptor<PassengerEntity> captor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(captor.capture());
        
        PassengerEntity saved = captor.getValue();
        assertTrue(saved.getSavedAddresses().contains(newAddress));
        assertEquals(3, saved.getSavedAddresses().size());
    }

    @Test
    void givenPassengerId_whenAddSavedAddress_thenDuplicateAddress_DoesNotAdd() {
        // Arrange
        String existingAddress = "123 Main St";
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        passengerService.addSavedAddress(1L, existingAddress);

        // Assert
        verify(passengerRepository, never()).save(any());
    }

    @Test
    void givenPassengerId_whenAddSavedAddress_thenNullList_CreatesNewList() {
        // Arrange
        testPassenger.setSavedAddresses(null);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        passengerService.addSavedAddress(1L, "New Address");

        // Assert
        ArgumentCaptor<PassengerEntity> captor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(captor.capture());
        
        assertNotNull(captor.getValue().getSavedAddresses());
        assertTrue(captor.getValue().getSavedAddresses().contains("New Address"));
    }

    @Test
    void givenPassengerId_whenRemoveSavedAddress_thenSuccess() {
        // Arrange
        String addressToRemove = "123 Main St";
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        PassengerDTO result = passengerService.removeSavedAddress(1L, addressToRemove);

        // Assert
        assertNotNull(result);
        
        ArgumentCaptor<PassengerEntity> captor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(captor.capture());
        
        PassengerEntity saved = captor.getValue();
        assertFalse(saved.getSavedAddresses().contains(addressToRemove));
    }

    @Test
    void givenPassengerId_whenRemoveSavedAddress_thenNullList_HandledGracefully() {
        // Arrange
        testPassenger.setSavedAddresses(null);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        PassengerDTO result = passengerService.removeSavedAddress(1L, "Any Address");

        // Assert
        assertNotNull(result);
        verify(passengerRepository).save(testPassenger);
    }

    @Test
    void givenPassengerId_whenGetSavedAddresses_thenSuccess() {
        // Arrange
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        List<String> addresses = passengerService.getSavedAddresses(1L);

        // Assert
        assertNotNull(addresses);
        assertEquals(2, addresses.size());
        assertTrue(addresses.contains("123 Main St"));
        assertTrue(addresses.contains("456 Oak Ave"));
    }

    @Test
    void givenPassengerId_whenGetSavedAddresses_thenNullList_ReturnsEmptyList() {
        // Arrange
        testPassenger.setSavedAddresses(null);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        List<String> addresses = passengerService.getSavedAddresses(1L);

        // Assert
        assertNotNull(addresses);
        assertTrue(addresses.isEmpty());
    }

    @Test
    void givenPassengerId_whenGetPassengerStats_thenSuccess() {
        // Arrange
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        PassengerDTO result = passengerService.getPassengerStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalRides());
        assertEquals(4.5, result.getPassengerRating());
    }

    @Test
    void givenPassengerId_whenGetPassengerStats_thenNoRides_ReturnsZero() {
        // Arrange
        testPassenger.setRides(null);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        PassengerDTO result = passengerService.getPassengerStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalRides());
    }
}
