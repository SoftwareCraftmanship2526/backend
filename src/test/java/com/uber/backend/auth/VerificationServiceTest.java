package com.uber.backend.auth;

import com.uber.backend.auth.application.dto.AuthResponse;
import com.uber.backend.auth.application.service.EmailService;
import com.uber.backend.auth.application.service.VerificationService;
import com.uber.backend.auth.domain.enums.Role;
import com.uber.backend.auth.infrastructure.persistence.VerificationCodeEntity;
import com.uber.backend.auth.infrastructure.repository.VerificationCodeRepository;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for VerificationService.
 * Tests email verification code generation, verification, and resending.
 */
@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private VerificationService verificationService;

    private PassengerEntity passenger;
    private DriverEntity driver;
    private VerificationCodeEntity verificationCode;

    @BeforeEach
    void setUp() {
        passenger = new PassengerEntity();
        passenger.setId(1L);
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setEmail("john.doe@example.com");
        passenger.setRole(Role.PASSENGER);
        passenger.setEmailVerified(false);

        driver = new DriverEntity();
        driver.setId(2L);
        driver.setFirstName("Jane");
        driver.setLastName("Smith");
        driver.setEmail("jane.smith@example.com");
        driver.setRole(Role.DRIVER);
        driver.setEmailVerified(false);

        verificationCode = VerificationCodeEntity.builder()
                .id(1L)
                .email("john.doe@example.com")
                .code("123456")
                .verified(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
    }

    @Test
    void givenEmail_whenGenerateAndSendVerificationCode_thenSuccess() {
        // Arrange
        when(verificationCodeRepository.save(any(VerificationCodeEntity.class))).thenReturn(verificationCode);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act
        verificationService.generateAndSendVerificationCode("john.doe@example.com");

        // Assert
        ArgumentCaptor<VerificationCodeEntity> codeCaptor = ArgumentCaptor.forClass(VerificationCodeEntity.class);
        verify(verificationCodeRepository).save(codeCaptor.capture());

        VerificationCodeEntity capturedCode = codeCaptor.getValue();
        assertEquals("john.doe@example.com", capturedCode.getEmail());
        assertNotNull(capturedCode.getCode());
        assertEquals(6, capturedCode.getCode().length());
        assertFalse(capturedCode.getVerified());

        verify(emailService).sendVerificationEmail(eq("john.doe@example.com"), anyString());
    }

    @Test
    void givenValidCode_whenVerifyEmailForPassenger_thenSuccess() {
        // Arrange
        when(verificationCodeRepository.findByEmailAndCodeAndVerifiedFalse("john.doe@example.com", "123456"))
                .thenReturn(Optional.of(verificationCode));
        when(passengerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(passenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(passenger);

        UserDetails userDetails = User.builder()
                .username("john.doe@example.com")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class), anyLong(), anyString())).thenReturn("jwt.token");
        when(jwtService.getExpiration()).thenReturn(86400000L);

        // Act
        AuthResponse response = verificationService.verifyEmail("john.doe@example.com", "123456");

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(Role.PASSENGER, response.getRole());
        assertTrue(response.getEmailVerified());

        verify(verificationCodeRepository).save(argThat(code -> code.getVerified()));
        verify(passengerRepository).save(argThat(p -> p.getEmailVerified()));
    }

    @Test
    void givenValidCode_whenVerifyEmailForDriver_thenSuccess() {
        // Arrange
        VerificationCodeEntity driverCode = VerificationCodeEntity.builder()
                .id(2L)
                .email("jane.smith@example.com")
                .code("654321")
                .verified(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        when(verificationCodeRepository.findByEmailAndCodeAndVerifiedFalse("jane.smith@example.com", "654321"))
                .thenReturn(Optional.of(driverCode));
        when(passengerRepository.findByEmail("jane.smith@example.com")).thenReturn(Optional.empty());
        when(driverRepository.findByEmail("jane.smith@example.com")).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(DriverEntity.class))).thenReturn(driver);

        UserDetails userDetails = User.builder()
                .username("jane.smith@example.com")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class), anyLong(), anyString())).thenReturn("jwt.driver.token");
        when(jwtService.getExpiration()).thenReturn(86400000L);

        // Act
        AuthResponse response = verificationService.verifyEmail("jane.smith@example.com", "654321");

        // Assert
        assertNotNull(response);
        assertEquals("jwt.driver.token", response.getToken());
        assertEquals(2L, response.getUserId());
        assertEquals("jane.smith@example.com", response.getEmail());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals(Role.DRIVER, response.getRole());
        assertTrue(response.getEmailVerified());

        verify(verificationCodeRepository).save(argThat(code -> code.getVerified()));
        verify(driverRepository).save(argThat(d -> d.getEmailVerified()));
    }

    @Test
    void givenInvalidCode_whenVerifyEmail_thenThrowsException() {
        // Arrange
        when(verificationCodeRepository.findByEmailAndCodeAndVerifiedFalse(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> verificationService.verifyEmail("john.doe@example.com", "wrong-code")
        );

        assertEquals("Invalid or already used verification code", exception.getMessage());
        verify(passengerRepository, never()).save(any());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void givenExpiredCode_whenVerifyEmail_thenThrowsException() {
        // Arrange
        VerificationCodeEntity expiredCode = VerificationCodeEntity.builder()
                .id(1L)
                .email("john.doe@example.com")
                .code("123456")
                .verified(false)
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .expiresAt(LocalDateTime.now().minusMinutes(5)) // Expired 5 minutes ago
                .build();

        when(verificationCodeRepository.findByEmailAndCodeAndVerifiedFalse("john.doe@example.com", "123456"))
                .thenReturn(Optional.of(expiredCode));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> verificationService.verifyEmail("john.doe@example.com", "123456")
        );

        assertEquals("Verification code has expired. Please request a new one.", exception.getMessage());
        verify(passengerRepository, never()).save(any());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void givenNonExistentUser_whenVerifyEmail_thenThrowsException() {
        // Arrange
        when(verificationCodeRepository.findByEmailAndCodeAndVerifiedFalse("unknown@example.com", "123456"))
                .thenReturn(Optional.of(verificationCode));
        when(passengerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(driverRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> verificationService.verifyEmail("unknown@example.com", "123456")
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void givenUnverifiedPassenger_whenResendVerificationCode_thenSuccess() {
        // Arrange
        when(passengerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(passenger));
        when(driverRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        when(verificationCodeRepository.save(any(VerificationCodeEntity.class))).thenReturn(verificationCode);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act
        verificationService.resendVerificationCode("john.doe@example.com");

        // Assert
        verify(verificationCodeRepository).save(any(VerificationCodeEntity.class));
        verify(emailService).sendVerificationEmail(eq("john.doe@example.com"), anyString());
    }

    @Test
    void givenUnverifiedDriver_whenResendVerificationCode_thenSuccess() {
        // Arrange
        when(passengerRepository.findByEmail("jane.smith@example.com")).thenReturn(Optional.empty());
        when(driverRepository.findByEmail("jane.smith@example.com")).thenReturn(Optional.of(driver));
        when(verificationCodeRepository.save(any(VerificationCodeEntity.class))).thenReturn(verificationCode);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act
        verificationService.resendVerificationCode("jane.smith@example.com");

        // Assert
        verify(verificationCodeRepository).save(any(VerificationCodeEntity.class));
        verify(emailService).sendVerificationEmail(eq("jane.smith@example.com"), anyString());
    }

    @Test
    void givenAlreadyVerifiedPassenger_whenResendVerificationCode_thenThrowsException() {
        // Arrange
        passenger.setEmailVerified(true);
        when(passengerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(passenger));
        when(driverRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> verificationService.resendVerificationCode("john.doe@example.com")
        );

        assertEquals("Email is already verified", exception.getMessage());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void givenNonExistentEmail_whenResendVerificationCode_thenThrowsException() {
        // Arrange
        when(passengerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(driverRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> verificationService.resendVerificationCode("unknown@example.com")
        );

        assertTrue(exception.getMessage().contains("No account found"));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void givenValidCode_whenVerifyEmail_thenCodeMarkedAsVerified() {
        // Arrange
        when(verificationCodeRepository.findByEmailAndCodeAndVerifiedFalse("john.doe@example.com", "123456"))
                .thenReturn(Optional.of(verificationCode));
        when(passengerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(passenger));
        when(passengerRepository.save(any(PassengerEntity.class))).thenReturn(passenger);

        UserDetails userDetails = User.builder()
                .username("john.doe@example.com")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class), anyLong(), anyString())).thenReturn("jwt.token");
        when(jwtService.getExpiration()).thenReturn(86400000L);

        // Act
        verificationService.verifyEmail("john.doe@example.com", "123456");

        // Assert
        ArgumentCaptor<VerificationCodeEntity> codeCaptor = ArgumentCaptor.forClass(VerificationCodeEntity.class);
        verify(verificationCodeRepository).save(codeCaptor.capture());

        VerificationCodeEntity capturedCode = codeCaptor.getValue();
        assertTrue(capturedCode.getVerified());
    }
}
