# Clean Architecture Guide

## What is Clean Architecture?

Clean Architecture is a software design pattern that separates the codebase into distinct layers, each with specific responsibilities. The main goal is to make the business logic **independent** of frameworks, databases, and external systems.

### Key Principles

1. **Independence**: Business logic doesn't depend on Spring, JPA, or any framework
2. **Testability**: Domain logic can be tested without databases or web servers
3. **Flexibility**: Easy to swap databases or frameworks without touching business logic
4. **Maintainability**: Clear boundaries make the code easier to understand and modify

---

## Our Project Structure

```
com.uber.backend/
├── domain/                          # Business Logic (Core)
│   ├── model/                       # Pure business entities
│   ├── embeddable/                  # Value objects
│   ├── enums/                       # Domain enums
│   └── strategy/                    # Business strategies
│
├── application/                     # Use Cases (Service Layer)
│   ├── command/                     # Write operations (Create, Update, Delete)
│   └── query/                       # Read operations (Get, List, Search)
│
├── infrastructure/                  # Technical Details
│   ├── persistence/
│   │   ├── entity/                 # JPA/Database entities
│   │   └── mapper/                 # Convert domain ↔ database
│   ├── repository/                  # Database access
│   └── seed/                        # Test data
│
└── api/                            # External Interface
    ├── web/                        # REST controllers
    ├── dto/                        # API request/response objects
    └── exception/                  # Error handling
```

---

## Layer Responsibilities

### 1. Domain Layer (`domain/`)

**What it is**: Pure business logic with NO framework dependencies

**Contains**:
- `model/` - Business entities (Account, Passenger, Driver, Ride, etc.)
- Business rules and validation
- Domain logic methods

**Example**:
```java
// domain/model/Ride.java
public class Ride {
    private Long id;
    private RideStatus status;
    private Long passengerId;  // Uses IDs, not entities!

    public void startRide() {
        if (this.status == RideStatus.REQUESTED) {
            this.status = RideStatus.IN_PROGRESS;
            this.startedAt = LocalDateTime.now();
        }
    }
}
```

**Rules**:
- ✅ Pure Java classes with Lombok annotations
- ✅ Business logic methods
- ✅ Uses IDs to reference other entities
- ❌ NO JPA annotations (@Entity, @Table, etc.)
- ❌ NO Spring annotations (@Service, @Component, etc.)
- ❌ NO database or framework code

---

### 2. Application Layer (`application/`)

**What it is**: Use cases / Service layer using CQRS pattern

**CQRS = Command Query Responsibility Segregation**
- **Commands**: Operations that CHANGE data (Create, Update, Delete)
- **Queries**: Operations that READ data (Get, List, Search)

#### 2a. Command Package (`application/command/`)

**Purpose**: Handle all write operations

**When to use**:
- Creating a new ride
- Updating driver availability
- Canceling a ride
- Processing payments

**Example Structure**:
```java
// application/command/CreateRideCommand.java
@Service
public class CreateRideCommand {
    private final RideRepository rideRepository;
    private final RideMapper rideMapper;

    public Ride execute(CreateRideRequest request) {
        // 1. Create domain model
        Ride ride = Ride.builder()
            .passengerId(request.getPassengerId())
            .pickupLocation(request.getPickupLocation())
            .dropoffLocation(request.getDropoffLocation())
            .build();

        // 2. Apply business logic
        ride.initializeRide();

        // 3. Convert to entity and save
        RideEntity entity = rideMapper.toEntity(ride);
        entity = rideRepository.save(entity);

        // 4. Return domain model
        return rideMapper.toDomain(entity);
    }
}
```

#### 2b. Query Package (`application/query/`)

**Purpose**: Handle all read operations

**When to use**:
- Getting driver details
- Listing available drivers
- Searching rides by passenger
- Getting ride history

**Example Structure**:
```java
// application/query/GetAvailableDriversQuery.java
@Service
public class GetAvailableDriversQuery {
    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    public List<Driver> execute() {
        // 1. Fetch from database
        List<DriverEntity> entities = driverRepository.findByIsAvailable(true);

        // 2. Convert to domain models
        return entities.stream()
            .map(driverMapper::toDomain)
            .collect(Collectors.toList());
    }
}
```

**Rules for application/ layer**:
- ✅ Contains @Service classes
- ✅ Uses repositories to access data
- ✅ Uses mappers to convert entity ↔ domain
- ✅ Returns domain models (NOT entities!)
- ❌ NO JPA entities exposed outside
- ❌ NO business logic (that goes in domain/)

---

### 3. Infrastructure Layer (`infrastructure/`)

**What it is**: Technical implementation details

#### 3a. Persistence (`infrastructure/persistence/`)

**entity/**: JPA entities with database annotations
```java
// infrastructure/persistence/entity/RideEntity.java
@Entity
@Table(name = "rides")
public class RideEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private PassengerEntity passenger;
}
```

**mapper/**: Convert between domain models and JPA entities
```java
// infrastructure/persistence/mapper/RideMapper.java
@Component
public class RideMapper {
    public Ride toDomain(RideEntity entity) { ... }
    public RideEntity toEntity(Ride domain) { ... }
}
```

#### 3b. Repository (`infrastructure/repository/`)

Spring Data JPA repositories
```java
// infrastructure/repository/DriverRepository.java
@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, Long> {
    List<DriverEntity> findByIsAvailable(Boolean isAvailable);
}
```

---

### 4. API Layer (`api/`)

**What it is**: External interface (REST endpoints)

#### Controllers (`api/web/`)

```java
// api/web/RideController.java
@RestController
@RequestMapping("/api/rides")
public class RideController {
    private final CreateRideCommand createRideCommand;
    private final GetRideQuery getRideQuery;

    @PostMapping
    public RideResponse createRide(@RequestBody CreateRideRequest request) {
        Ride ride = createRideCommand.execute(request);
        return RideResponse.from(ride);
    }

    @GetMapping("/{id}")
    public RideResponse getRide(@PathVariable Long id) {
        Ride ride = getRideQuery.execute(id);
        return RideResponse.from(ride);
    }
}
```

**Rules**:
- ✅ Uses DTOs for requests/responses
- ✅ Calls command/query services
- ✅ Handles HTTP concerns only
- ❌ NO business logic
- ❌ NO direct repository access

---

## How to Add New Features

### Example: Add "Get All Drivers" Feature

#### Step 1: Check if domain model exists
✅ `Driver` model already exists in `domain/model/Driver.java`

#### Step 2: Create Query Service
Create `application/query/GetAllDriversQuery.java`:
```java
package com.uber.backend.application.query;

import com.uber.backend.domain.model.Driver;
import com.uber.backend.infrastructure.persistence.mapper.DriverMapper;
import com.uber.backend.infrastructure.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllDriversQuery {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    public List<Driver> execute() {
        return driverRepository.findAll().stream()
            .map(driverMapper::toDomain)
            .collect(Collectors.toList());
    }
}
```

#### Step 3: Create DTO (optional)
Create `api/dto/DriverResponse.java`:
```java
package com.uber.backend.api.dto;

import com.uber.backend.domain.model.Driver;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private Double driverRating;
    private Boolean isAvailable;

    public static DriverResponse from(Driver driver) {
        return DriverResponse.builder()
            .id(driver.getId())
            .firstName(driver.getFirstName())
            .lastName(driver.getLastName())
            .driverRating(driver.getDriverRating())
            .isAvailable(driver.getIsAvailable())
            .build();
    }
}
```

#### Step 4: Create Controller
Create `api/web/DriverController.java`:
```java
package com.uber.backend.api.web;

import com.uber.backend.api.dto.DriverResponse;
import com.uber.backend.application.query.GetAllDriversQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final GetAllDriversQuery getAllDriversQuery;

    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        List<DriverResponse> drivers = getAllDriversQuery.execute().stream()
            .map(DriverResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(drivers);
    }
}
```

---

## Command vs Query: When to Use Which?

### Use Command (`application/command/`) when:
- ✅ Creating new records (POST)
- ✅ Updating existing records (PUT/PATCH)
- ✅ Deleting records (DELETE)
- ✅ Any operation that CHANGES state

**Examples**:
- `CreateRideCommand` - Create new ride
- `UpdateDriverAvailabilityCommand` - Change driver status
- `CancelRideCommand` - Cancel a ride
- `ProcessPaymentCommand` - Process payment

### Use Query (`application/query/`) when:
- ✅ Getting single record (GET /api/rides/1)
- ✅ Listing multiple records (GET /api/drivers)
- ✅ Searching/filtering (GET /api/rides?status=COMPLETED)
- ✅ Any operation that only READS data

**Examples**:
- `GetDriverQuery` - Get driver by ID
- `GetAllDriversQuery` - List all drivers
- `GetAvailableDriversQuery` - List available drivers
- `GetRidesByPassengerQuery` - Get passenger's rides

---

## Migration from Old Structure

### Old Way (Before Clean Architecture):
```
repository/     → Data access
service/        → Business logic + data access mixed
controller/     → API endpoints
domain/entity/  → JPA entities used everywhere
```

### New Way (After Clean Architecture):
```
domain/model/              → Pure business logic
application/command/       → Write operations (replaces service/ for writes)
application/query/         → Read operations (replaces service/ for reads)
infrastructure/repository/ → Data access
infrastructure/persistence/entity/ → JPA entities (hidden from outside)
infrastructure/persistence/mapper/ → Convert domain ↔ entity
api/web/                   → API endpoints
```

### What Changed:
1. **service/** folder split into:
   - `application/command/` for write operations
   - `application/query/` for read operations

2. **domain/entity/** moved to:
   - `domain/model/` (pure business models)
   - `infrastructure/persistence/entity/` (JPA entities)

3. **New concept**: Mappers convert between domain and entities

---

## Benefits

### 1. Testability
```java
// Test domain logic WITHOUT database
@Test
void shouldStartRide() {
    Ride ride = Ride.builder()
        .status(RideStatus.REQUESTED)
        .build();

    ride.startRide();

    assertEquals(RideStatus.IN_PROGRESS, ride.getStatus());
}
```

### 2. Framework Independence
- Want to switch from PostgreSQL to MongoDB? Only change `infrastructure/`
- Want to use GraphQL instead of REST? Only change `api/`
- Business logic stays the same!

### 3. Clear Responsibilities
- Controllers only handle HTTP
- Queries only read data
- Commands only write data
- Domain only has business logic

### 4. Team Collaboration
- Backend developer: Works on `domain/` and `application/`
- Database expert: Works on `infrastructure/persistence/`
- Frontend developer: Works with `api/` endpoints

---

## Common Questions

### Q: Do I always need both Command and Query?
**A**: For simple CRUD, you might only need one. Complex operations may need both.

### Q: Can a Command return data?
**A**: Yes! Commands can return the created/updated entity. Example: `CreateRideCommand` returns the new `Ride`.

### Q: Where do I put validation?
**A**:
- Business rules → `domain/model/` methods
- Input validation → `api/dto/` or controller
- Database constraints → `infrastructure/persistence/entity/`

### Q: Can Commands call Queries or vice versa?
**A**: Generally no. Both should use repositories directly. If needed, they can share helper services.

### Q: What about transactions?
**A**: Add `@Transactional` to Command/Query services:
```java
@Service
@RequiredArgsConstructor
public class CreateRideCommand {
    @Transactional
    public Ride execute(CreateRideRequest request) { ... }
}
```

---

## Summary

1. **domain/** = Pure business logic (no frameworks)
2. **application/command/** = Write operations (Create, Update, Delete)
3. **application/query/** = Read operations (Get, List, Search)
4. **infrastructure/** = Technical details (database, external APIs)
5. **api/** = External interface (REST controllers)

**Remember**: The application layer (command/query) is the NEW service layer, but organized by operation type (read vs write) instead of by entity.
