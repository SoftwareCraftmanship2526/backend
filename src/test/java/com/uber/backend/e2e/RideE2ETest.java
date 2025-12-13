package com.uber.backend.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.backend.auth.application.dto.RegisterRequest;
import com.uber.backend.auth.application.dto.RegisterDriverRequest;
import com.uber.backend.auth.application.dto.VerifyEmailRequest;
import com.uber.backend.auth.infrastructure.repository.VerificationCodeRepository;
import com.uber.backend.driver.application.command.GoOnlineCommand;
import com.uber.backend.payment.application.command.ProcessPaymentCommand;
import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.ride.application.command.CompleteRideCommand;
import com.uber.backend.ride.application.command.DriverAcceptCommand;
import com.uber.backend.ride.application.command.RequestRideCommand;
import com.uber.backend.ride.application.command.StartRideCommand;
import com.uber.backend.ride.domain.enums.RideType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End Test for complete ride lifecycle.
 * Tests the full flow from passenger registration to payment completion via HTTP API.
 * Only validates HTTP responses, does not check database state.
 * Uses Testcontainers to automatically start a PostgreSQL database for testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RideE2ETest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW5zLW1pbmltdW0tMjU2LWJpdHMtcmVxdWlyZWQtZm9yLWhzMjU2LWFsZ29yaXRobQ==");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Test
    void completeRideLifecycle_fromRegistrationToPayment() throws Exception {
        // ========== STEP 1: Register Passenger ==========
        RegisterRequest passengerRegister = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("passenger.e2e@test.com")
                .password("Password123")
                .phoneNumber("+32471234567")
                .build();

        mockMvc.perform(post("/api/auth/register/passenger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passengerRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful! A verification code has been sent to passenger.e2e@test.com"));

        // ========== STEP 2: Verify Passenger Email ==========
        String passengerVerificationCode = verificationCodeRepository
                .findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc("passenger.e2e@test.com")
                .orElseThrow()
                .getCode();

        VerifyEmailRequest passengerVerify = new VerifyEmailRequest(
                "passenger.e2e@test.com",
                passengerVerificationCode
        );

        MvcResult passengerVerifyResult = mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passengerVerify)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andReturn();

        String passengerResponse = passengerVerifyResult.getResponse().getContentAsString();
        String passengerToken = objectMapper.readTree(passengerResponse).get("token").asText();

        // ========== STEP 3: Register Driver ==========
        RegisterDriverRequest driverRegister = RegisterDriverRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("driver.e2e@test.com")
                .password("Password123")
                .phoneNumber("+32471234568")
                .licenseNumber("DL123456")
                .build();

        mockMvc.perform(post("/api/auth/register/driver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful! A verification code has been sent to driver.e2e@test.com"));

        // ========== STEP 4: Verify Driver Email ==========
        String driverVerificationCode = verificationCodeRepository
                .findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc("driver.e2e@test.com")
                .orElseThrow()
                .getCode();

        VerifyEmailRequest driverVerify = new VerifyEmailRequest(
                "driver.e2e@test.com",
                driverVerificationCode
        );

        MvcResult driverVerifyResult = mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverVerify)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andReturn();

        String driverResponse = driverVerifyResult.getResponse().getContentAsString();
        String driverToken = objectMapper.readTree(driverResponse).get("token").asText();

        // ========== STEP 5: Driver Goes Online ==========
        GoOnlineCommand goOnline = new GoOnlineCommand(50.8503, 4.3517);

        MvcResult goOnlineResult = mockMvc.perform(post("/api/drivers/go-online")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goOnline)))
                .andExpect(status().isOk())
                .andReturn();

        String goOnlineMessage = goOnlineResult.getResponse().getContentAsString();
        assertTrue(goOnlineMessage.contains("Driver is now online"));

        // ========== STEP 6: Passenger Requests Ride ==========
        RequestRideCommand requestRide = new RequestRideCommand(
                50.8503,
                4.3517,
                50.8467,
                4.3525,
                RideType.UBER_X
        );

        MvcResult rideRequestResult = mockMvc.perform(post("/api/rides/request")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestRide)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").exists())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn();

        String rideResponse = rideRequestResult.getResponse().getContentAsString();
        Long rideId = objectMapper.readTree(rideResponse).get("rideId").asLong();

        // ========== STEP 7: Driver Accepts Ride ==========
        DriverAcceptCommand acceptCommand = new DriverAcceptCommand(rideId);

        mockMvc.perform(post("/api/rides/accept")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // ========== STEP 8: Driver Starts Ride ==========
        StartRideCommand startCommand = new StartRideCommand(rideId);

        mockMvc.perform(post("/api/rides/start")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startCommand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // ========== STEP 9: Driver Completes Ride ==========
        CompleteRideCommand completeRide = new CompleteRideCommand(
                rideId,
                5.2,
                15,
                1.0
        );

        mockMvc.perform(post("/api/rides/complete")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRide)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // ========== STEP 10: Passenger Processes Payment ==========
        ProcessPaymentCommand processPayment = new ProcessPaymentCommand(
                rideId,
                PaymentMethod.CREDIT_CARD
        );

        mockMvc.perform(post("/api/payments/process")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processPayment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").exists())
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.transactionId").exists());
    }
}
