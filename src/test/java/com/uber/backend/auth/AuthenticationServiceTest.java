package com.uber.backend.auth;

import com.uber.backend.auth.application.dto.AuthResponse;
import com.uber.backend.auth.application.dto.LoginRequest;
import com.uber.backend.auth.application.dto.RegisterDriverRequest;
import com.uber.backend.auth.application.dto.RegisterRequest;
import com.uber.backend.auth.application.service.*;
import com.uber.backend.auth.domain.enums.Role;
import com.uber.backend.auth.infrastructure.security.JwtService;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for AuthenticationService.
 * Tests registration, login, and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest passengerRequest;
    private RegisterDriverRequest driverRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup passenger registration request
        passengerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("Password123")
                .phoneNumber("+1234567890")
                .build();

        // Setup driver registration request
        driverRequest = RegisterDriverRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password("Password123")
                .phoneNumber("+0987654321")
                .licenseNumber("DL-12345")
                .build();

        // Setup login request
        loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("Password123")
                .build();
    }

    @Test
    void givenRegisterRequest_whenRegisterPassenger_thenSuccess() {
        // Arrange
        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(driverRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        PassengerEntity savedPassenger = new PassengerEntity();
        savedPassenger.setId(1L);
        savedPassenger.setFirstName("John");
        savedPassenger.setLastName("Doe");
        savedPassenger.setEmail("john.doe@example.com");
        savedPassenger.setRole(Role.PASSENGER);
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(savedPassenger);

        UserDetails userDetails = User.builder()
                .username("john.doe@example.com")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        // Act
        authenticationService.registerPassenger(passengerRequest);

        // Assert - verification email should be sent
        verify(verificationService).generateAndSendVerificationCode("john.doe@example.com");

        // Verify interactions
        verify(passengerRepository).findByEmail("john.doe@example.com");
        verify(driverRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("Password123");
        
        ArgumentCaptor<PassengerEntity> passengerCaptor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(passengerCaptor.capture());
        
        PassengerEntity capturedPassenger = passengerCaptor.getValue();
        assertEquals("John", capturedPassenger.getFirstName());
        assertEquals("Doe", capturedPassenger.getLastName());
        assertEquals("encodedPassword", capturedPassenger.getPassword());
        assertEquals(Role.PASSENGER, capturedPassenger.getRole());
        assertEquals(5.0, capturedPassenger.getPassengerRating());
        assertFalse(capturedPassenger.getEmailVerified());
    }

    @Test
    void givenRegisterRequest_whenRegisterPassenger_EmailAlreadyExists_ThrowsException() {
        // Arrange
        PassengerEntity existingPassenger = new PassengerEntity();
        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.of(existingPassenger));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.registerPassenger(passengerRequest)
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(passengerRepository, never()).save(any());
    }

    @Test
    void givenRegisterRequest_whenRegisterDriver_thenSuccess() {
        // Arrange
        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(driverRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(driverRepository.findByLicenseNumber(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        DriverEntity savedDriver = new DriverEntity();
        savedDriver.setId(2L);
        savedDriver.setFirstName("Jane");
        savedDriver.setLastName("Smith");
        savedDriver.setEmail("jane.smith@example.com");
        savedDriver.setRole(Role.DRIVER);
        when(driverRepository.save(any(DriverEntity.class))).thenReturn(savedDriver);

        // Act
        authenticationService.registerDriver(driverRequest);

        // Assert - verification email should be sent
        verify(verificationService).generateAndSendVerificationCode("jane.smith@example.com");

        ArgumentCaptor<DriverEntity> driverCaptor = ArgumentCaptor.forClass(DriverEntity.class);
        verify(driverRepository).save(driverCaptor.capture());
        
        DriverEntity capturedDriver = driverCaptor.getValue();
        assertEquals("Jane", capturedDriver.getFirstName());
        assertEquals("Smith", capturedDriver.getLastName());
        assertEquals("DL-12345", capturedDriver.getLicenseNumber());
        assertEquals(Role.DRIVER, capturedDriver.getRole());
        assertEquals(5.0, capturedDriver.getDriverRating());
        assertFalse(capturedDriver.getIsAvailable());
        assertFalse(capturedDriver.getEmailVerified());
    }

    @Test
    void givenRegisterRequest_whenRegisterDriver_EmailAlreadyExists_ThrowsException() {
        // Arrange
        DriverEntity existingDriver = new DriverEntity();
        when(driverRepository.findByEmail(anyString())).thenReturn(Optional.of(existingDriver));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.registerDriver(driverRequest)
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void givenRegisterRequest_whenRegisterDriver_LicenseNumberAlreadyExists_ThrowsException() {
        // Arrange
        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(driverRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        DriverEntity existingDriver = new DriverEntity();
        existingDriver.setLicenseNumber("DL-12345");
        when(driverRepository.findByLicenseNumber("DL-12345")).thenReturn(Optional.of(existingDriver));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.registerDriver(driverRequest)
        );

        assertEquals("License number already registered", exception.getMessage());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void givenLoginRequest_whenLogin_PassengerSuccess() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetails userDetails = User.builder()
                .username("john.doe@example.com")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        PassengerEntity passenger = new PassengerEntity();
        passenger.setId(1L);
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setEmail("john.doe@example.com");
        passenger.setRole(Role.PASSENGER);
        passenger.setEmailVerified(true);
        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.of(passenger));

        when(jwtService.generateToken(any(UserDetails.class), anyLong(), anyString())).thenReturn("jwt.login.token");
        when(jwtService.getExpiration()).thenReturn(86400000L);

        // Act
        AuthResponse response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.login.token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(Role.PASSENGER, response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void givenLoginRequest_whenLogin_DriverSuccess() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetails userDetails = User.builder()
                .username("jane.smith@example.com")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        DriverEntity driver = new DriverEntity();
        driver.setId(2L);
        driver.setFirstName("Jane");
        driver.setLastName("Smith");
        driver.setEmail("jane.smith@example.com");
        driver.setRole(Role.DRIVER);
        driver.setEmailVerified(true);
        when(driverRepository.findByEmail(anyString())).thenReturn(Optional.of(driver));

        when(jwtService.generateToken(any(UserDetails.class), anyLong(), anyString())).thenReturn("jwt.driver.token");
        when(jwtService.getExpiration()).thenReturn(86400000L);

        // Act
        LoginRequest driverLogin = LoginRequest.builder()
                .email("jane.smith@example.com")
                .password("Password123")
                .build();
        AuthResponse response = authenticationService.login(driverLogin);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.driver.token", response.getToken());
        assertEquals(2L, response.getUserId());
        assertEquals(Role.DRIVER, response.getRole());
    }

    @Test
    void givenLoginRequest_whenLogin_InvalidCredentials_ThrowsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authenticationService.login(loginRequest));
    }

    @Test
    void givenLoginRequest_whenLogin_UserNotFoundAfterAuthentication_ThrowsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetails userDetails = User.builder()
                .username("unknown@example.com")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(driverRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        LoginRequest unknownLogin = LoginRequest.builder()
                .email("unknown@example.com")
                .password("Password123")
                .build();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> authenticationService.login(unknownLogin));
    }

    @Test
    void givenRegisterRequest_whenRegisterPassenger_PasswordIsEncoded() {
        // Arrange
        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(driverRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123")).thenReturn("super.encrypted.password");
        
        PassengerEntity savedPassenger = new PassengerEntity();
        savedPassenger.setId(1L);
        savedPassenger.setRole(Role.PASSENGER);
        savedPassenger.setEmail("john.doe@example.com");
        savedPassenger.setFirstName("John");
        savedPassenger.setLastName("Doe");
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(savedPassenger);

        // Act
        authenticationService.registerPassenger(passengerRequest);

        // Assert
        verify(passwordEncoder).encode("Password123");
        
        ArgumentCaptor<PassengerEntity> captor = ArgumentCaptor.forClass(PassengerEntity.class);
        verify(passengerRepository).save(captor.capture());
        assertEquals("super.encrypted.password", captor.getValue().getPassword());
    }

    @Test
    void givenLoginRequest_whenLogin_PassengerEmailNotVerified_ThrowsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetails userDetails = User.builder()
                .username("john.doe@example.com")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        PassengerEntity passenger = new PassengerEntity();
        passenger.setId(1L);
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setEmail("john.doe@example.com");
        passenger.setRole(Role.PASSENGER);
        passenger.setEmailVerified(false);
        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.of(passenger));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.login(loginRequest)
        );

        assertEquals("Email not verified. Please verify your email before logging in.", exception.getMessage());
        verify(jwtService, never()).generateToken(any(), anyLong(), anyString());
    }

    @Test
    void givenLoginRequest_whenLogin_DriverEmailNotVerified_ThrowsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetails userDetails = User.builder()
                .username("jane.smith@example.com")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        when(passengerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        DriverEntity driver = new DriverEntity();
        driver.setId(2L);
        driver.setFirstName("Jane");
        driver.setLastName("Smith");
        driver.setEmail("jane.smith@example.com");
        driver.setRole(Role.DRIVER);
        driver.setEmailVerified(false);
        when(driverRepository.findByEmail(anyString())).thenReturn(Optional.of(driver));

        LoginRequest driverLogin = LoginRequest.builder()
                .email("jane.smith@example.com")
                .password("Password123")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.login(driverLogin)
        );

        assertEquals("Email not verified. Please verify your email before logging in.", exception.getMessage());
        verify(jwtService, never()).generateToken(any(), anyLong(), anyString());
    }
}
