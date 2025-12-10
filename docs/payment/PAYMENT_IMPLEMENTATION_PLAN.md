# Payment & Pricing Implementation Plan

## Overview
This plan implements the Payment & Pricing domain for the ride-sharing application, covering fare calculation and payment processing (without receipt sending).

---

## Phase 1: Calculate Fare

### Step 1.1: Create Pricing Configuration
**Purpose**: Define pricing rules and strategies

**Files to create**:
- `src/main/java/com/uber/backend/payment/domain/enums/PricingStrategy.java`
- `src/main/java/com/uber/backend/payment/domain/enums/RideType.java`
- `src/main/java/com/uber/backend/payment/infrastructure/persistence/PricingConfigEntity.java`
- `src/main/java/com/uber/backend/payment/infrastructure/repository/PricingConfigRepository.java`

**What to include**:
- UberX base fare, per km rate, per minute rate
- UberBlack premium rates
- UberPool discount rates
- Demand multiplier support

---

### Step 1.2: Create Fare Calculation DTOs
**Purpose**: Request/Response objects for fare calculation

**Files to create**:
- `src/main/java/com/uber/backend/payment/application/dto/FareCalculationRequest.java`
- `src/main/java/com/uber/backend/payment/application/dto/FareCalculationResponse.java`

**FareCalculationRequest fields**:
- distance (km)
- duration (minutes)
- rideType (UBER_X, UBER_BLACK, UBER_POOL)
- demandMultiplier (surge pricing)

**FareCalculationResponse fields**:
- baseFare
- distanceFare
- durationFare
- demandMultiplier
- discount
- totalFare
- breakdown (itemized costs)

---

### Step 1.3: Implement Fare Calculation Service
**Purpose**: Core business logic for calculating fares

**Files to create**:
- `src/main/java/com/uber/backend/payment/application/service/FareCalculationService.java`

**Methods to implement**:
- `calculateFare(FareCalculationRequest request)`: Main calculation method
- `getBaseFare(RideType rideType)`: Get base fare by ride type
- `calculateDistanceFare(double distance, RideType rideType)`: Calculate distance component
- `calculateDurationFare(int duration, RideType rideType)`: Calculate time component
- `applyDemandMultiplier(double fare, double multiplier)`: Apply surge pricing
- `applyDiscount(double fare, RideType rideType)`: Apply discounts (e.g., UberPool)

**Logic**:
```
totalFare = (baseFare + (distance * perKmRate) + (duration * perMinRate)) * demandMultiplier - discount
```

---

### Step 1.4: Create Fare Calculation Command/Query
**Purpose**: CQRS pattern for fare calculation

**Files to create**:
- `src/main/java/com/uber/backend/payment/application/command/CalculateFareCommand.java`
- `src/main/java/com/uber/backend/payment/application/command/CalculateFareCommandHandler.java`

---

### Step 1.5: Create Fare Calculation Controller
**Purpose**: REST API endpoint for fare calculation

**Files to create**:
- `src/main/java/com/uber/backend/payment/infrastructure/web/FareController.java`

**Endpoints**:
- `POST /api/fares/calculate`: Calculate fare before ride booking
- `GET /api/fares/estimate`: Estimate fare with pickup/dropoff locations

---

## Phase 2: Process Payment

### Step 2.1: Create Payment Domain Models
**Purpose**: Core payment entities

**Files to create**:
- `src/main/java/com/uber/backend/payment/domain/enums/PaymentMethod.java` (CREDIT_CARD, DEBIT_CARD, PAYPAL, WALLET)
- `src/main/java/com/uber/backend/payment/domain/enums/PaymentStatus.java` (PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED)
- `src/main/java/com/uber/backend/payment/infrastructure/persistence/PaymentEntity.java`
- `src/main/java/com/uber/backend/payment/infrastructure/repository/PaymentRepository.java`

**PaymentEntity fields**:
- id
- rideId (foreign key to rides table)
- passengerId
- driverId
- amount
- paymentMethod
- paymentStatus
- transactionId (from payment gateway)
- createdAt
- processedAt
- failureReason

---

### Step 2.2: Create Saved Payment Methods
**Purpose**: Allow users to save payment methods for auto-charge

**Files to create**:
- `src/main/java/com/uber/backend/payment/infrastructure/persistence/SavedPaymentMethodEntity.java`
- `src/main/java/com/uber/backend/payment/infrastructure/repository/SavedPaymentMethodRepository.java`

**SavedPaymentMethodEntity fields**:
- id
- userId (passenger or driver)
- paymentMethod
- isDefault
- cardLastFourDigits
- expiryDate
- paymentToken (tokenized card from gateway)

---

### Step 2.3: Create Payment DTOs
**Purpose**: Request/Response objects for payment processing

**Files to create**:
- `src/main/java/com/uber/backend/payment/application/dto/ProcessPaymentRequest.java`
- `src/main/java/com/uber/backend/payment/application/dto/ProcessPaymentResponse.java`
- `src/main/java/com/uber/backend/payment/application/dto/SavePaymentMethodRequest.java`
- `src/main/java/com/uber/backend/payment/application/dto/PaymentMethodDto.java`

---

### Step 2.4: Implement Payment Service
**Purpose**: Core business logic for payment processing

**Files to create**:
- `src/main/java/com/uber/backend/payment/application/service/PaymentService.java`
- `src/main/java/com/uber/backend/payment/infrastructure/gateway/PaymentGateway.java` (interface)
- `src/main/java/com/uber/backend/payment/infrastructure/gateway/MockPaymentGateway.java` (implementation)

**PaymentService methods**:
- `processPayment(ProcessPaymentRequest request)`: Process payment using saved method
- `autoChargeAfterRide(Long rideId, Double amount)`: Auto-charge passenger after ride
- `refundPayment(Long paymentId, String reason)`: Process refund
- `getPaymentHistory(Long userId)`: Get user's payment history
- `savePaymentMethod(SavePaymentMethodRequest request)`: Save new payment method
- `getDefaultPaymentMethod(Long userId)`: Get user's default payment method

---

### Step 2.5: Create Payment Commands
**Purpose**: CQRS pattern for payment operations

**Files to create**:
- `src/main/java/com/uber/backend/payment/application/command/ProcessPaymentCommand.java`
- `src/main/java/com/uber/backend/payment/application/command/ProcessPaymentCommandHandler.java`
- `src/main/java/com/uber/backend/payment/application/command/SavePaymentMethodCommand.java`
- `src/main/java/com/uber/backend/payment/application/command/SavePaymentMethodCommandHandler.java`

---

### Step 2.6: Create Payment Queries
**Purpose**: CQRS pattern for payment retrieval

**Files to create**:
- `src/main/java/com/uber/backend/payment/application/query/GetPaymentHistoryQuery.java`
- `src/main/java/com/uber/backend/payment/application/query/GetPaymentHistoryQueryHandler.java`
- `src/main/java/com/uber/backend/payment/application/query/GetSavedPaymentMethodsQuery.java`
- `src/main/java/com/uber/backend/payment/application/query/GetSavedPaymentMethodsQueryHandler.java`

---

### Step 2.7: Create Payment Controller
**Purpose**: REST API endpoints for payment operations

**Files to create**:
- `src/main/java/com/uber/backend/payment/infrastructure/web/PaymentController.java`

**Endpoints**:
- `POST /api/payments/process`: Process a payment
- `POST /api/payments/methods`: Save a payment method
- `GET /api/payments/methods`: Get saved payment methods
- `PUT /api/payments/methods/{id}/default`: Set default payment method
- `DELETE /api/payments/methods/{id}`: Remove payment method
- `GET /api/payments/history`: Get payment history
- `POST /api/payments/{id}/refund`: Request refund

---

## Phase 3: Integration with Rides

### Step 3.1: Update Ride Entity
**Purpose**: Link rides with payments

**Files to modify**:
- `src/main/java/com/uber/backend/ride/infrastructure/persistence/RideEntity.java`

**Add fields**:
- fareAmount
- paymentId (foreign key)
- paymentStatus

---

### Step 3.2: Update Ride Completion Flow
**Purpose**: Auto-charge payment when ride is completed

**Files to modify**:
- `src/main/java/com/uber/backend/ride/application/service/RideService.java`

**Add logic**:
- When ride status changes to COMPLETED
- Calculate final fare
- Auto-charge passenger's default payment method
- Update ride with payment details

---

## Phase 4: Exception Handling

### Step 4.1: Create Payment Exceptions
**Purpose**: Handle payment-specific errors

**Files to create**:
- `src/main/java/com/uber/backend/payment/domain/exception/InsufficientFundsException.java`
- `src/main/java/com/uber/backend/payment/domain/exception/PaymentProcessingException.java`
- `src/main/java/com/uber/backend/payment/domain/exception/PaymentMethodNotFoundException.java`
- `src/main/java/com/uber/backend/payment/domain/exception/InvalidPaymentMethodException.java`

---

## Phase 5: Testing

### Step 5.1: Service Tests
**Files to create**:
- `src/test/java/com/uber/backend/payment/application/FareCalculationServiceTest.java`
- `src/test/java/com/uber/backend/payment/application/PaymentServiceTest.java`

**Test cases for FareCalculationService**:
- Calculate fare with different ride types
- Apply surge pricing (demand multiplier)
- Apply discounts for UberPool
- Handle edge cases (zero distance, negative values)

**Test cases for PaymentService**:
- Process payment with valid payment method
- Handle payment failure
- Save and retrieve payment methods
- Auto-charge after ride completion
- Process refund

---

### Step 5.2: Controller Tests
**Files to create**:
- `src/test/java/com/uber/backend/payment/infrastructure/web/FareControllerTest.java`
- `src/test/java/com/uber/backend/payment/infrastructure/web/PaymentControllerTest.java`

---

## Phase 6: Configuration & Database

### Step 6.1: Database Schema
**Purpose**: Create payment tables

**Files to create**:
- `src/main/resources/db/migration/V6__create_payment_tables.sql` (if using Flyway)

**Tables to create**:
- `pricing_configs` (pricing rules)
- `payments` (payment records)
- `saved_payment_methods` (user payment methods)

---

### Step 6.2: Seed Pricing Data
**Purpose**: Initialize pricing configuration

**Add to application startup**:
- Default pricing for UberX, UberBlack, UberPool
- Base fares, per km rates, per minute rates

---

## Implementation Order Summary

1. **Phase 1**: Fare Calculation (Steps 1.1 → 1.5)
   - Start here: simplest, no external dependencies

2. **Phase 2**: Payment Processing (Steps 2.1 → 2.7)
   - Use mock payment gateway initially

3. **Phase 3**: Ride Integration (Steps 3.1 → 3.2)
   - Connect rides with payments

4. **Phase 4**: Exception Handling (Step 4.1)
   - Robust error handling

5. **Phase 5**: Testing (Steps 5.1 → 5.2)
   - Comprehensive test coverage

6. **Phase 6**: Configuration (Steps 6.1 → 6.2)
   - Database schema and seed data

---

## Key Design Decisions

### Pricing Strategy
- **Flexible pricing**: Store pricing configs in database for easy updates
- **Surge pricing**: Support demand multipliers
- **Ride type pricing**: Different rates for UberX, UberBlack, UberPool

### Payment Processing
- **Mock gateway**: Use mock payment gateway (can replace with Stripe/PayPal later)
- **Saved methods**: Support saved payment methods for auto-charge
- **Idempotency**: Ensure payments aren't processed twice

### CQRS Pattern
- **Commands**: Process payment, save payment method
- **Queries**: Get payment history, get saved methods

---

## Next Steps

Would you like me to:
1. Start implementing Phase 1 (Fare Calculation)?
2. Create the database schema first?
3. Set up the basic package structure?

Let me know which approach you prefer, and I'll begin implementation!
