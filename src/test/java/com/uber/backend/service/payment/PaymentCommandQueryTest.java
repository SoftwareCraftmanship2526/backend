package com.uber.backend.service.payment;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.payment.application.GetPaymentHistoryQueryHandler;
import com.uber.backend.payment.application.ProcessPaymentCommandHandler;
import com.uber.backend.payment.application.command.ProcessPaymentCommand;
import com.uber.backend.payment.application.command.ProcessPaymentResult;
import com.uber.backend.payment.application.query.GetPaymentHistoryQuery;
import com.uber.backend.payment.application.query.PaymentHistoryResult;
import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import com.uber.backend.payment.infrastructure.repository.PaymentRepository;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test suite for payment command and query handlers.
 * Tests payment processing and payment history retrieval.
 */
@ExtendWith(MockitoExtension.class)
class PaymentCommandQueryTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ProcessPaymentCommandHandler processPaymentHandler;

    @InjectMocks
    private GetPaymentHistoryQueryHandler getPaymentHistoryHandler;

    @Nested
    class ProcessPaymentCommandTests {

        private ProcessPaymentCommand command;
        private PaymentEntity payment;
        private RideEntity ride;
        private PassengerEntity passenger;

        @BeforeEach
        void setUp() {
            // Setup passenger
            passenger = new PassengerEntity();
            passenger.setId(1L);

            // Setup ride
            ride = new RideEntity();
            ride.setId(100L);
            ride.setPassenger(passenger);

            // Setup payment
            payment = new PaymentEntity();
            payment.setId(1L);
            payment.setRide(ride);
            payment.setAmount(new BigDecimal("25.50"));
            payment.setStatus(PaymentStatus.PENDING);

            // Setup command
            command = new ProcessPaymentCommand(100L, PaymentMethod.CREDIT_CARD);
        }

        @Test
        void givenValidPayment_whenProcessing_thenPaymentCompleted() {
            // Given
            when(paymentRepository.findByRideId(100L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ProcessPaymentResult result = processPaymentHandler.handle(command, 1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.paymentId());
            assertEquals(100L, result.rideId());
            assertEquals(new BigDecimal("25.50"), result.amount());
            assertEquals(PaymentMethod.CREDIT_CARD, result.paymentMethod());
            assertEquals(PaymentStatus.COMPLETED, result.status());
            assertNotNull(result.transactionId());
            assertTrue(result.transactionId().startsWith("TXN-"));

            verify(paymentRepository).findByRideId(100L);
            verify(paymentRepository).save(payment);
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
            assertEquals(PaymentMethod.CREDIT_CARD, payment.getMethod());
        }

        @Test
        void givenPaymentNotFound_whenProcessing_thenThrowsException() {
            // Given
            when(paymentRepository.findByRideId(100L)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> processPaymentHandler.handle(command, 1L)
            );
            assertEquals("No pending payment found for ride: 100", exception.getMessage());
            verify(paymentRepository).findByRideId(100L);
            verify(paymentRepository, never()).save(any());
        }

        @Test
        void givenAlreadyProcessedPayment_whenProcessing_thenThrowsException() {
            // Given
            payment.setStatus(PaymentStatus.COMPLETED);
            when(paymentRepository.findByRideId(100L)).thenReturn(Optional.of(payment));

            // When & Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> processPaymentHandler.handle(command, 1L)
            );
            assertEquals("Payment already processed with status: COMPLETED", exception.getMessage());
            verify(paymentRepository).findByRideId(100L);
            verify(paymentRepository, never()).save(any());
        }

        @Test
        void givenWrongPassenger_whenProcessing_thenThrowsException() {
            // Given
            when(paymentRepository.findByRideId(100L)).thenReturn(Optional.of(payment));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> processPaymentHandler.handle(command, 999L) // Different passenger ID
            );
            assertTrue(exception.getMessage().contains("This ride belongs to a different passenger"));
            verify(paymentRepository).findByRideId(100L);
            verify(paymentRepository, never()).save(any());
        }

        @Test
        void givenPaymentWithoutRide_whenProcessing_thenThrowsException() {
            // Given
            payment.setRide(null);
            when(paymentRepository.findByRideId(100L)).thenReturn(Optional.of(payment));

            // When & Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> processPaymentHandler.handle(command, 1L)
            );
            assertEquals("Payment has no associated ride or passenger", exception.getMessage());
            verify(paymentRepository).findByRideId(100L);
            verify(paymentRepository, never()).save(any());
        }

        @Test
        void givenPaymentWithWallet_whenProcessing_thenMethodUpdated() {
            // Given
            ProcessPaymentCommand walletCommand = new ProcessPaymentCommand(100L, PaymentMethod.WALLET);
            when(paymentRepository.findByRideId(100L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ProcessPaymentResult result = processPaymentHandler.handle(walletCommand, 1L);

            // Then
            assertEquals(PaymentMethod.WALLET, result.paymentMethod());
            assertEquals(PaymentMethod.WALLET, payment.getMethod());
        }

        @Test
        void givenPaymentWithCash_whenProcessing_thenCashMethodUsed() {
            // Given
            ProcessPaymentCommand cashCommand = new ProcessPaymentCommand(100L, PaymentMethod.CASH);
            when(paymentRepository.findByRideId(100L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ProcessPaymentResult result = processPaymentHandler.handle(cashCommand, 1L);

            // Then
            assertEquals(PaymentMethod.CASH, result.paymentMethod());
            assertEquals(PaymentMethod.CASH, payment.getMethod());
        }

        @Test
        void givenFailedPaymentStatus_whenChecking_thenThrowsException() {
            // Given
            payment.setStatus(PaymentStatus.FAILED);
            when(paymentRepository.findByRideId(100L)).thenReturn(Optional.of(payment));

            // When & Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> processPaymentHandler.handle(command, 1L)
            );
            assertEquals("Payment already processed with status: FAILED", exception.getMessage());
        }
    }

    @Nested
    class GetPaymentHistoryQueryTests {

        private PassengerEntity passenger;
        private DriverEntity driver;
        private RideEntity ride1;
        private RideEntity ride2;
        private RideEntity ride3;

        @BeforeEach
        void setUp() {
            // Setup passenger
            passenger = new PassengerEntity();
            passenger.setId(1L);

            // Setup driver
            driver = new DriverEntity();
            driver.setId(2L);

            // Setup ride 1 (passenger's ride)
            ride1 = new RideEntity();
            ride1.setId(100L);
            ride1.setPassenger(passenger);
            ride1.setDriver(driver);

            // Setup ride 2 (passenger's ride)
            ride2 = new RideEntity();
            ride2.setId(101L);
            ride2.setPassenger(passenger);
            ride2.setDriver(driver);

            // Setup ride 3 (different passenger's ride)
            PassengerEntity otherPassenger = new PassengerEntity();
            otherPassenger.setId(3L);
            ride3 = new RideEntity();
            ride3.setId(102L);
            ride3.setPassenger(otherPassenger);
            ride3.setDriver(driver);
        }

        @Test
        void givenPassengerWithPayments_whenGettingHistory_thenReturnsPassengerPayments() {
            // Given
            PaymentEntity payment1 = createPayment(1L, ride1, new BigDecimal("25.50"), PaymentStatus.COMPLETED);
            PaymentEntity payment2 = createPayment(2L, ride2, new BigDecimal("30.00"), PaymentStatus.COMPLETED);
            PaymentEntity payment3 = createPayment(3L, ride3, new BigDecimal("40.00"), PaymentStatus.COMPLETED);

            when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment1, payment2, payment3));

            GetPaymentHistoryQuery query = new GetPaymentHistoryQuery(1L); // Passenger ID

            // When
            PaymentHistoryResult result = getPaymentHistoryHandler.handle(query);

            // Then
            assertNotNull(result);
            assertEquals(2, result.payments().size());

            List<Long> paymentIds = result.payments().stream()
                    .map(PaymentHistoryResult.PaymentRecord::paymentId)
                    .toList();
            assertTrue(paymentIds.contains(1L));
            assertTrue(paymentIds.contains(2L));
            assertFalse(paymentIds.contains(3L)); // Other passenger's payment

            verify(paymentRepository).findAll();
        }

        @Test
        void givenDriverWithPayments_whenGettingHistory_thenReturnsDriverPayments() {
            // Given
            PaymentEntity payment1 = createPayment(1L, ride1, new BigDecimal("25.50"), PaymentStatus.COMPLETED);
            PaymentEntity payment2 = createPayment(2L, ride2, new BigDecimal("30.00"), PaymentStatus.COMPLETED);
            PaymentEntity payment3 = createPayment(3L, ride3, new BigDecimal("40.00"), PaymentStatus.COMPLETED);

            when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment1, payment2, payment3));

            GetPaymentHistoryQuery query = new GetPaymentHistoryQuery(2L); // Driver ID

            // When
            PaymentHistoryResult result = getPaymentHistoryHandler.handle(query);

            // Then
            assertNotNull(result);
            assertEquals(3, result.payments().size()); // Driver sees all 3 rides they drove

            verify(paymentRepository).findAll();
        }

        @Test
        void givenUserWithNoPayments_whenGettingHistory_thenReturnsEmptyList() {
            // Given
            when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

            GetPaymentHistoryQuery query = new GetPaymentHistoryQuery(999L);

            // When
            PaymentHistoryResult result = getPaymentHistoryHandler.handle(query);

            // Then
            assertNotNull(result);
            assertTrue(result.payments().isEmpty());

            verify(paymentRepository).findAll();
        }

        @Test
        void givenPaymentsWithDifferentStatuses_whenGettingHistory_thenReturnsAllStatuses() {
            // Given
            PaymentEntity completedPayment = createPayment(1L, ride1, new BigDecimal("25.50"), PaymentStatus.COMPLETED);

            RideEntity ride4 = new RideEntity();
            ride4.setId(103L);
            ride4.setPassenger(passenger);
            ride4.setDriver(driver);
            PaymentEntity pendingPayment = createPayment(2L, ride4, new BigDecimal("30.00"), PaymentStatus.PENDING);

            when(paymentRepository.findAll()).thenReturn(Arrays.asList(completedPayment, pendingPayment));

            GetPaymentHistoryQuery query = new GetPaymentHistoryQuery(1L);

            // When
            PaymentHistoryResult result = getPaymentHistoryHandler.handle(query);

            // Then
            assertNotNull(result);
            assertEquals(2, result.payments().size());

            boolean hasCompleted = result.payments().stream()
                    .anyMatch(p -> p.status() == PaymentStatus.COMPLETED);
            boolean hasPending = result.payments().stream()
                    .anyMatch(p -> p.status() == PaymentStatus.PENDING);

            assertTrue(hasCompleted);
            assertTrue(hasPending);

            verify(paymentRepository).findAll();
        }

        @Test
        void givenPaymentsWithDifferentMethods_whenGettingHistory_thenReturnsAllMethods() {
            // Given
            PaymentEntity creditCardPayment = createPaymentWithMethod(1L, ride1, PaymentMethod.CREDIT_CARD);

            RideEntity ride4 = new RideEntity();
            ride4.setId(103L);
            ride4.setPassenger(passenger);
            ride4.setDriver(driver);
            PaymentEntity cashPayment = createPaymentWithMethod(2L, ride4, PaymentMethod.CASH);

            when(paymentRepository.findAll()).thenReturn(Arrays.asList(creditCardPayment, cashPayment));

            GetPaymentHistoryQuery query = new GetPaymentHistoryQuery(1L);

            // When
            PaymentHistoryResult result = getPaymentHistoryHandler.handle(query);

            // Then
            assertNotNull(result);
            assertEquals(2, result.payments().size());

            boolean hasCreditCard = result.payments().stream()
                    .anyMatch(p -> p.paymentMethod() == PaymentMethod.CREDIT_CARD);
            boolean hasCash = result.payments().stream()
                    .anyMatch(p -> p.paymentMethod() == PaymentMethod.CASH);

            assertTrue(hasCreditCard);
            assertTrue(hasCash);

            verify(paymentRepository).findAll();
        }

        @Test
        void givenPaymentsWithTransactionIds_whenGettingHistory_thenReturnsTransactionIds() {
            // Given
            PaymentEntity payment = createPayment(1L, ride1, new BigDecimal("25.50"), PaymentStatus.COMPLETED);
            payment.setTransactionId("TXN-123456");

            when(paymentRepository.findAll()).thenReturn(Collections.singletonList(payment));

            GetPaymentHistoryQuery query = new GetPaymentHistoryQuery(1L);

            // When
            PaymentHistoryResult result = getPaymentHistoryHandler.handle(query);

            // Then
            assertNotNull(result);
            assertEquals(1, result.payments().size());
            assertEquals("TXN-123456", result.payments().get(0).transactionId());

            verify(paymentRepository).findAll();
        }

        private PaymentEntity createPayment(Long paymentId, RideEntity ride, BigDecimal amount, PaymentStatus status) {
            PaymentEntity payment = new PaymentEntity();
            payment.setId(paymentId);
            payment.setRide(ride);
            payment.setAmount(amount);
            payment.setStatus(status);
            payment.setMethod(PaymentMethod.CREDIT_CARD);
            return payment;
        }

        private PaymentEntity createPaymentWithMethod(Long paymentId, RideEntity ride, PaymentMethod method) {
            PaymentEntity payment = new PaymentEntity();
            payment.setId(paymentId);
            payment.setRide(ride);
            payment.setAmount(new BigDecimal("25.50"));
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setMethod(method);
            return payment;
        }
    }
}
