package com.uber.backend.driver.application;

import com.uber.backend.driver.application.dto.AddVehicleRequest;
import com.uber.backend.driver.application.dto.UpdateVehicleRequest;
import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.application.service.VehicleService;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.driver.infrastructure.repository.VehicleRepository;
import com.uber.backend.ride.domain.enums.RideType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for VehicleService.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private DriverEntity testDriver;
    private VehicleEntity testVehicle;
    private AddVehicleRequest addVehicleRequest;

    @BeforeEach
    void setUp() {
        testDriver = new DriverEntity();
        testDriver.setId(1L);
        testDriver.setFirstName("John");
        testDriver.setLastName("Driver");
        testDriver.setEmail("john.driver@example.com");

        testVehicle = VehicleEntity.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .model("Toyota Camry")
                .color("Black")
                .type(RideType.UBER_X)
                .driver(testDriver)
                .build();

        addVehicleRequest = AddVehicleRequest.builder()
                .licensePlate("XYZ-789")
                .model("Honda Accord")
                .color("White")
                .type(RideType.UBER_X)
                .build();
    }

    @Test
    void givenValidRequest_whenAddVehicle_thenReturnVehicleDTO() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByLicensePlate(anyString())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        VehicleDTO result = vehicleService.addVehicle(1L, addVehicleRequest);

        // Assert
        assertNotNull(result);
        assertEquals("ABC-123", result.getLicensePlate());
        assertEquals("Toyota Camry", result.getModel());
        assertEquals("Black", result.getColor());
        assertEquals(RideType.UBER_X, result.getType());

        ArgumentCaptor<VehicleEntity> vehicleCaptor = ArgumentCaptor.forClass(VehicleEntity.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        
        VehicleEntity savedVehicle = vehicleCaptor.getValue();
        assertEquals("XYZ-789", savedVehicle.getLicensePlate());
        assertEquals(testDriver, savedVehicle.getDriver());
    }

    @Test
    void givenDriverNotFound_whenAddVehicle_thenThrowException() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.addVehicle(1L, addVehicleRequest)
        );

        assertEquals("Driver not found with ID: 1", exception.getMessage());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void givenDuplicateLicensePlate_whenAddVehicle_thenThrowException() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByLicensePlate("XYZ-789")).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.addVehicle(1L, addVehicleRequest)
        );

        assertEquals("Vehicle with license plate XYZ-789 already exists", exception.getMessage());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void givenDriverHasNoCurrentVehicle_whenAddVehicle_thenSetAsCurrentVehicle() {
        // Arrange
        testDriver.setCurrentVehicle(null);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByLicensePlate(anyString())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        vehicleService.addVehicle(1L, addVehicleRequest);

        // Assert
        verify(driverRepository).save(testDriver);
        assertEquals(testVehicle, testDriver.getCurrentVehicle());
    }

    @Test
    void givenLowercaseLicensePlate_whenAddVehicle_thenConvertToUppercase() {
        // Arrange
        addVehicleRequest = AddVehicleRequest.builder()
                .licensePlate("abc-123")
                .model("Honda Accord")
                .color("White")
                .type(RideType.UBER_X)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByLicensePlate(anyString())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        vehicleService.addVehicle(1L, addVehicleRequest);

        // Assert
        ArgumentCaptor<VehicleEntity> vehicleCaptor = ArgumentCaptor.forClass(VehicleEntity.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals("ABC-123", vehicleCaptor.getValue().getLicensePlate());
    }

    @Test
    void givenValidRequest_whenGetDriverVehicles_thenReturnVehicleDTOList() {
        // Arrange
        VehicleEntity vehicle2 = VehicleEntity.builder()
                .id(2L)
                .licensePlate("DEF-456")
                .model("Tesla Model 3")
                .color("Red")
                .type(RideType.UBER_BLACK)
                .driver(testDriver)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByDriverId(1L)).thenReturn(Arrays.asList(testVehicle, vehicle2));

        // Act
        List<VehicleDTO> result = vehicleService.getDriverVehicles(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ABC-123", result.get(0).getLicensePlate());
        assertEquals("DEF-456", result.get(1).getLicensePlate());
    }

    @Test
    void givenDriverNotFound_whenGetDriverVehicles_thenThrowException() {
        // Arrange
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.getDriverVehicles(1L)
        );

        assertEquals("Driver not found with ID: 1", exception.getMessage());
    }

    @Test
    void givenValidRequest_whenGetVehicleById_thenReturnVehicleDTO() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        VehicleDTO result = vehicleService.getVehicleById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ABC-123", result.getLicensePlate());
    }

    @Test
    void givenVehicleNotFound_whenGetVehicleById_thenThrowException() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.getVehicleById(1L)
        );

        assertEquals("Vehicle not found with ID: 1", exception.getMessage());
    }

    @Test
    void givenValidRequest_whenUpdateVehicle_thenReturnVehicleDTO() {
        // Arrange
        UpdateVehicleRequest updateRequest = UpdateVehicleRequest.builder()
                .model("Toyota Camry 2024")
                .color("Silver")
                .type(RideType.UBER_BLACK)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        VehicleDTO result = vehicleService.updateVehicle(1L, 1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(vehicleRepository).save(testVehicle);
        assertEquals("Toyota Camry 2024", testVehicle.getModel());
        assertEquals("Silver", testVehicle.getColor());
        assertEquals(RideType.UBER_BLACK, testVehicle.getType());
    }

    @Test
    void givenVehicleNotFound_whenUpdateVehicle_thenThrowException() {
        // Arrange
        UpdateVehicleRequest updateRequest = UpdateVehicleRequest.builder()
                .model("New Model")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.updateVehicle(1L, 1L, updateRequest)
        );

        assertEquals("Vehicle not found with ID: 1", exception.getMessage());
    }

    @Test
    void givenWrongDriver_whenUpdateVehicle_thenThrowException() {
        // Arrange
        UpdateVehicleRequest updateRequest = UpdateVehicleRequest.builder()
                .model("New Model")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.updateVehicle(1L, 999L, updateRequest)
        );

        assertEquals("Vehicle does not belong to driver", exception.getMessage());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void givenPartialUpdate_whenUpdateVehicle_thenUpdateOnlyProvidedFields() {
        // Arrange
        UpdateVehicleRequest updateRequest = UpdateVehicleRequest.builder()
                .color("Blue")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        vehicleService.updateVehicle(1L, 1L, updateRequest);

        // Assert
        assertEquals("Blue", testVehicle.getColor());
        assertEquals("Toyota Camry", testVehicle.getModel()); // Unchanged
        assertEquals(RideType.UBER_X, testVehicle.getType()); // Unchanged
    }

    @Test
    void givenValidRequest_whenDeleteVehicle_thenReturnVehicleDTO() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        vehicleService.deleteVehicle(1L, 1L);

        // Assert
        verify(vehicleRepository).delete(testVehicle);
    }

    @Test
    void givenVehicleNotFound_whenDeleteVehicle_thenThrowException() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.deleteVehicle(1L, 1L)
        );

        assertEquals("Vehicle not found with ID: 1", exception.getMessage());
        verify(vehicleRepository, never()).delete(any());
    }

    @Test
    void givenWrongDriver_whenDeleteVehicle_thenThrowException() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.deleteVehicle(1L, 999L)
        );

        assertEquals("Vehicle does not belong to driver", exception.getMessage());
        verify(vehicleRepository, never()).delete(any());
    }

    @Test
    void givenCurrentVehicle_whenDeleteVehicle_thenUnsetCurrentVehicle() {
        // Arrange
        testDriver.setCurrentVehicle(testVehicle);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        vehicleService.deleteVehicle(1L, 1L);

        // Assert
        assertNull(testDriver.getCurrentVehicle());
        verify(driverRepository).save(testDriver);
        verify(vehicleRepository).delete(testVehicle);
    }

    @Test
    void givenValidRequest_whenSetCurrentVehicle_thenReturnVehicleDTO() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        VehicleDTO result = vehicleService.setCurrentVehicle(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testVehicle, testDriver.getCurrentVehicle());
        verify(driverRepository).save(testDriver);
    }

    @Test
    void givenVehicleNotFound_whenSetCurrentVehicle_thenThrowException() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.setCurrentVehicle(1L, 1L)
        );

        assertEquals("Vehicle not found with ID: 1", exception.getMessage());
    }

    @Test
    void givenWrongDriver_whenSetCurrentVehicle_thenThrowException() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.setCurrentVehicle(1L, 999L)
        );

        assertEquals("Vehicle does not belong to driver", exception.getMessage());
        verify(driverRepository, never()).save(any());
    }
}
