package com.uber.backend.service.passenger;

import com.uber.backend.passenger.application.*;
import com.uber.backend.passenger.application.command.AddSavedAddressCommand;
import com.uber.backend.passenger.application.command.RemoveSavedAddressCommand;
import com.uber.backend.passenger.application.command.UpdatePassengerCommand;
import com.uber.backend.passenger.application.dto.PassengerDTO;
import com.uber.backend.passenger.application.query.GetPassengerByIdQuery;
import com.uber.backend.passenger.application.query.GetPassengerStatsQuery;
import com.uber.backend.passenger.application.query.GetSavedAddressesQuery;
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
 * Comprehensive test suite for Passenger CQRS handlers.
 */
@ExtendWith(MockitoExtension.class)
class PassengerCommandQueryTest {

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private GetPassengerByIdQueryHandler getPassengerByIdHandler;

    @InjectMocks
    private GetSavedAddressesQueryHandler getSavedAddressesHandler;

    @InjectMocks
    private GetPassengerStatsQueryHandler getPassengerStatsHandler;

    @InjectMocks
    private UpdatePassengerCommandHandler updatePassengerHandler;

    @InjectMocks
    private AddSavedAddressCommandHandler addSavedAddressHandler;

    @InjectMocks
    private RemoveSavedAddressCommandHandler removeSavedAddressHandler;

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

    // Query Handler Tests

    @Test
    void givenPassengerId_whenGetPassengerById_thenSuccess() {
        // Arrange
        GetPassengerByIdQuery query = new GetPassengerByIdQuery(1L);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        PassengerDTO result = getPassengerByIdHandler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("john.doe@example.com", result.email());
        assertEquals(4.5, result.passengerRating());
        assertEquals(2, result.savedAddresses().size());
        assertEquals(3, result.totalRides());

        verify(passengerRepository).findById(1L);
    }

    @Test
    void givenPassengerId_whenGetPassengerById_thenNotFound_ThrowsException() {
        // Arrange
        GetPassengerByIdQuery query = new GetPassengerByIdQuery(999L);
        when(passengerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getPassengerByIdHandler.handle(query)
        );

        assertTrue(exception.getMessage().contains("Passenger not found"));
    }

    @Test
    void givenPassengerId_whenGetSavedAddresses_thenSuccess() {
        // Arrange
        GetSavedAddressesQuery query = new GetSavedAddressesQuery(1L);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        List<String> addresses = getSavedAddressesHandler.handle(query);

        // Assert
        assertNotNull(addresses);
        assertEquals(2, addresses.size());
        assertTrue(addresses.contains("123 Main St"));
        assertTrue(addresses.contains("456 Oak Ave"));
    }

    @Test
    void givenPassengerId_whenGetSavedAddresses_thenNullList_ReturnsEmptyList() {
        // Arrange
        GetSavedAddressesQuery query = new GetSavedAddressesQuery(1L);
        testPassenger.setSavedAddresses(null);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        List<String> addresses = getSavedAddressesHandler.handle(query);

        // Assert
        assertNotNull(addresses);
        assertTrue(addresses.isEmpty());
    }

    @Test
    void givenPassengerId_whenGetPassengerStats_thenSuccess() {
        // Arrange
        GetPassengerStatsQuery query = new GetPassengerStatsQuery(1L);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        PassengerDTO result = getPassengerStatsHandler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.totalRides());
        assertEquals(4.5, result.passengerRating());
    }

    @Test
    void givenPassengerId_whenGetPassengerStats_thenNoRides_ReturnsZero() {
        // Arrange
        GetPassengerStatsQuery query = new GetPassengerStatsQuery(1L);
        testPassenger.setRides(null);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        PassengerDTO result = getPassengerStatsHandler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.totalRides());
    }

    // Command Handler Tests

    @Test
    void givenPassengerId_whenUpdatePassenger_thenSuccess() {
        // Arrange
        UpdatePassengerCommand command = new UpdatePassengerCommand(
                1L,
                "Jane",
                "Smith",
                "+0987654321"
        );

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        updatePassengerHandler.handle(command);

        // Assert
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
        UpdatePassengerCommand command = new UpdatePassengerCommand(
                1L,
                "Jane",
                null,
                null
        );

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        updatePassengerHandler.handle(command);

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
        AddSavedAddressCommand command = new AddSavedAddressCommand(1L, "789 Elm St");
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        addSavedAddressHandler.handle(command);

        // Assert
        ArgumentCaptor<PassengerEntity> captor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(captor.capture());

        PassengerEntity saved = captor.getValue();
        assertTrue(saved.getSavedAddresses().contains("789 Elm St"));
        assertEquals(3, saved.getSavedAddresses().size());
    }

    @Test
    void givenPassengerId_whenAddSavedAddress_thenDuplicateAddress_DoesNotAdd() {
        // Arrange
        AddSavedAddressCommand command = new AddSavedAddressCommand(1L, "123 Main St");
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        addSavedAddressHandler.handle(command);

        // Assert
        verify(passengerRepository, never()).save(any());
    }

    @Test
    void givenPassengerId_whenAddSavedAddress_thenNullList_CreatesNewList() {
        // Arrange
        AddSavedAddressCommand command = new AddSavedAddressCommand(1L, "New Address");
        testPassenger.setSavedAddresses(null);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        addSavedAddressHandler.handle(command);

        // Assert
        ArgumentCaptor<PassengerEntity> captor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(captor.capture());

        assertNotNull(captor.getValue().getSavedAddresses());
        assertTrue(captor.getValue().getSavedAddresses().contains("New Address"));
    }

    @Test
    void givenPassengerId_whenRemoveSavedAddress_thenSuccess() {
        // Arrange
        RemoveSavedAddressCommand command = new RemoveSavedAddressCommand(1L, "123 Main St");
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(testPassenger);

        // Act
        removeSavedAddressHandler.handle(command);

        // Assert
        ArgumentCaptor<PassengerEntity> captor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(captor.capture());

        PassengerEntity saved = captor.getValue();
        assertFalse(saved.getSavedAddresses().contains("123 Main St"));
    }

    @Test
    void givenPassengerId_whenRemoveSavedAddress_thenNullList_HandledGracefully() {
        // Arrange
        RemoveSavedAddressCommand command = new RemoveSavedAddressCommand(1L, "Any Address");
        testPassenger.setSavedAddresses(null);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));

        // Act
        removeSavedAddressHandler.handle(command);

        // Assert
        verify(passengerRepository, never()).save(any());
    }
}
