package com.uber.backend.service.driver;

import com.uber.backend.driver.application.*;
import com.uber.backend.driver.application.command.AddVehicleCommand;
import com.uber.backend.driver.application.command.DeleteVehicleCommand;
import com.uber.backend.driver.application.command.SetCurrentVehicleCommand;
import com.uber.backend.driver.application.command.UpdateVehicleCommand;
import com.uber.backend.driver.application.dto.VehicleDTO;
import com.uber.backend.driver.application.query.GetDriverVehiclesQuery;
import com.uber.backend.driver.application.query.GetVehicleByIdQuery;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for Vehicle CQRS handlers.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private AddVehicleCommandHandler addVehicleHandler;

    @InjectMocks
    private UpdateVehicleCommandHandler updateVehicleHandler;

    @InjectMocks
    private DeleteVehicleCommandHandler deleteVehicleHandler;

    @InjectMocks
    private SetCurrentVehicleCommandHandler setCurrentVehicleHandler;

    @InjectMocks
    private GetDriverVehiclesQueryHandler getDriverVehiclesHandler;

    @InjectMocks
    private GetVehicleByIdQueryHandler getVehicleByIdHandler;

    private DriverEntity testDriver;
    private VehicleEntity testVehicle;

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
    }

    // Command Handler Tests

    @Test
    void givenValidCommand_whenAddVehicle_thenReturnVehicleDTO() {
        // Arrange
        AddVehicleCommand command = new AddVehicleCommand(
                1L, "XYZ-789", "Honda Accord", "White", RideType.UBER_X
        );
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByLicensePlate(anyString())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        VehicleDTO result = addVehicleHandler.handle(command);

        // Assert
        assertNotNull(result);
        assertEquals("ABC-123", result.licensePlate());
        assertEquals("Toyota Camry", result.model());
        assertEquals("Black", result.color());
        assertEquals(RideType.UBER_X, result.type());

        ArgumentCaptor<VehicleEntity> vehicleCaptor = ArgumentCaptor.forClass(VehicleEntity.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());

        VehicleEntity savedVehicle = vehicleCaptor.getValue();
        assertEquals("XYZ-789", savedVehicle.getLicensePlate());
        assertEquals(testDriver, savedVehicle.getDriver());
    }

    @Test
    void givenDriverNotFound_whenAddVehicle_thenThrowException() {
        // Arrange
        AddVehicleCommand command = new AddVehicleCommand(
                1L, "XYZ-789", "Honda Accord", "White", RideType.UBER_X
        );
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> addVehicleHandler.handle(command)
        );

        assertEquals("Driver not found with ID: 1", exception.getMessage());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void givenDuplicateLicensePlate_whenAddVehicle_thenThrowException() {
        // Arrange
        AddVehicleCommand command = new AddVehicleCommand(
                1L, "XYZ-789", "Honda Accord", "White", RideType.UBER_X
        );
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByLicensePlate("XYZ-789")).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> addVehicleHandler.handle(command)
        );

        assertEquals("Vehicle with license plate XYZ-789 already exists", exception.getMessage());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void givenDriverHasNoCurrentVehicle_whenAddVehicle_thenSetAsCurrentVehicle() {
        // Arrange
        AddVehicleCommand command = new AddVehicleCommand(
                1L, "XYZ-789", "Honda Accord", "White", RideType.UBER_X
        );
        testDriver.setCurrentVehicle(null);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByLicensePlate(anyString())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        addVehicleHandler.handle(command);

        // Assert
        verify(driverRepository).save(testDriver);
        assertEquals(testVehicle, testDriver.getCurrentVehicle());
    }

    @Test
    void givenLowercaseLicensePlate_whenAddVehicle_thenConvertToUppercase() {
        // Arrange
        AddVehicleCommand command = new AddVehicleCommand(
                1L, "abc-123", "Honda Accord", "White", RideType.UBER_X
        );
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByLicensePlate(anyString())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        addVehicleHandler.handle(command);

        // Assert
        ArgumentCaptor<VehicleEntity> vehicleCaptor = ArgumentCaptor.forClass(VehicleEntity.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals("ABC-123", vehicleCaptor.getValue().getLicensePlate());
    }

    @Test
    void givenValidCommand_whenUpdateVehicle_thenReturnVehicleDTO() {
        // Arrange
        UpdateVehicleCommand command = new UpdateVehicleCommand(
                1L, 1L, "Toyota Camry 2024", "Silver", RideType.UBER_BLACK
        );
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        VehicleDTO result = updateVehicleHandler.handle(command);

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
        UpdateVehicleCommand command = new UpdateVehicleCommand(
                1L, 1L, "New Model", null, null
        );
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateVehicleHandler.handle(command)
        );

        assertEquals("Vehicle not found with ID: 1", exception.getMessage());
    }

    @Test
    void givenWrongDriver_whenUpdateVehicle_thenThrowException() {
        // Arrange
        UpdateVehicleCommand command = new UpdateVehicleCommand(
                1L, 999L, "New Model", null, null
        );
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateVehicleHandler.handle(command)
        );

        assertEquals("Vehicle does not belong to driver", exception.getMessage());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void givenPartialUpdate_whenUpdateVehicle_thenUpdateOnlyProvidedFields() {
        // Arrange
        UpdateVehicleCommand command = new UpdateVehicleCommand(
                1L, 1L, null, "Blue", null
        );
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(testVehicle);

        // Act
        updateVehicleHandler.handle(command);

        // Assert
        assertEquals("Blue", testVehicle.getColor());
        assertEquals("Toyota Camry", testVehicle.getModel()); // Unchanged
        assertEquals(RideType.UBER_X, testVehicle.getType()); // Unchanged
    }

    @Test
    void givenValidCommand_whenDeleteVehicle_thenDeleteVehicle() {
        // Arrange
        DeleteVehicleCommand command = new DeleteVehicleCommand(1L, 1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        deleteVehicleHandler.handle(command);

        // Assert
        verify(vehicleRepository).delete(testVehicle);
    }

    @Test
    void givenVehicleNotFound_whenDeleteVehicle_thenThrowException() {
        // Arrange
        DeleteVehicleCommand command = new DeleteVehicleCommand(1L, 1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deleteVehicleHandler.handle(command)
        );

        assertEquals("Vehicle not found with ID: 1", exception.getMessage());
        verify(vehicleRepository, never()).delete(any());
    }

    @Test
    void givenWrongDriver_whenDeleteVehicle_thenThrowException() {
        // Arrange
        DeleteVehicleCommand command = new DeleteVehicleCommand(1L, 999L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deleteVehicleHandler.handle(command)
        );

        assertEquals("Vehicle does not belong to driver", exception.getMessage());
        verify(vehicleRepository, never()).delete(any());
    }

    @Test
    void givenCurrentVehicle_whenDeleteVehicle_thenUnsetCurrentVehicle() {
        // Arrange
        DeleteVehicleCommand command = new DeleteVehicleCommand(1L, 1L);
        testDriver.setCurrentVehicle(testVehicle);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        deleteVehicleHandler.handle(command);

        // Assert
        assertNull(testDriver.getCurrentVehicle());
        verify(driverRepository).save(testDriver);
        verify(vehicleRepository).delete(testVehicle);
    }

    @Test
    void givenValidCommand_whenSetCurrentVehicle_thenReturnVehicleDTO() {
        // Arrange
        SetCurrentVehicleCommand command = new SetCurrentVehicleCommand(1L, 1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        VehicleDTO result = setCurrentVehicleHandler.handle(command);

        // Assert
        assertNotNull(result);
        assertEquals(testVehicle, testDriver.getCurrentVehicle());
        verify(driverRepository).save(testDriver);
    }

    @Test
    void givenVehicleNotFound_whenSetCurrentVehicle_thenThrowException() {
        // Arrange
        SetCurrentVehicleCommand command = new SetCurrentVehicleCommand(1L, 1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> setCurrentVehicleHandler.handle(command)
        );

        assertEquals("Vehicle not found with ID: 1", exception.getMessage());
    }

    @Test
    void givenWrongDriver_whenSetCurrentVehicle_thenThrowException() {
        // Arrange
        SetCurrentVehicleCommand command = new SetCurrentVehicleCommand(1L, 999L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> setCurrentVehicleHandler.handle(command)
        );

        assertEquals("Vehicle does not belong to driver", exception.getMessage());
        verify(driverRepository, never()).save(any());
    }

    // Query Handler Tests

    @Test
    void givenValidQuery_whenGetDriverVehicles_thenReturnVehicleDTOList() {
        // Arrange
        GetDriverVehiclesQuery query = new GetDriverVehiclesQuery(1L);
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
        List<VehicleDTO> result = getDriverVehiclesHandler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ABC-123", result.get(0).licensePlate());
        assertEquals("DEF-456", result.get(1).licensePlate());
    }

    @Test
    void givenDriverNotFound_whenGetDriverVehicles_thenThrowException() {
        // Arrange
        GetDriverVehiclesQuery query = new GetDriverVehiclesQuery(1L);
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getDriverVehiclesHandler.handle(query)
        );

        assertEquals("Driver not found with ID: 1", exception.getMessage());
    }

    @Test
    void givenValidQuery_whenGetVehicleById_thenReturnVehicleDTO() {
        // Arrange
        GetVehicleByIdQuery query = new GetVehicleByIdQuery(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        VehicleDTO result = getVehicleByIdHandler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ABC-123", result.licensePlate());
    }

    @Test
    void givenVehicleNotFound_whenGetVehicleById_thenThrowException() {
        // Arrange
        GetVehicleByIdQuery query = new GetVehicleByIdQuery(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getVehicleByIdHandler.handle(query)
        );

        assertEquals("Vehicle not found with ID: 1", exception.getMessage());
    }
}
