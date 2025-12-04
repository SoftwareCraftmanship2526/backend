# Practical Guide: When to Use What

## The Real Question: Do I Always Need Commands/Queries?

**Short answer: NO!** Keep it simple for simple operations.

---

## Decision Tree

```
Is it a simple CRUD operation?
│
├─ YES → Use Query/Command with direct repository call (minimal wrapper)
│        Example: GetAllDriversQuery just calls repository.findAll()
│
└─ NO → Is there business logic, validation, or multiple steps?
         │
         ├─ YES → Use full Command/Query with domain logic
         │        Example: CreateRideCommand validates, calculates fare, etc.
         │
         └─ NO → You probably have a simple case, use minimal wrapper
```

---

## 3 Levels of Complexity

### Level 1: Simple Read (Just Repo + Mapping)

**When**: Getting data with no logic

**Example**: Get all drivers
```java
// application/query/GetAllDriversQuery.java
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

**Controller**:
```java
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {
    private final GetAllDriversQuery getAllDriversQuery;

    @GetMapping
    public List<Driver> getAll() {
        return getAllDriversQuery.execute();
    }
}
```

**Is this different from your old service?** Not really! It's just renamed from `DriverService.getAllDrivers()` to `GetAllDriversQuery.execute()`. The benefit is it's **clearly a read operation**.

---

### Level 2: Simple Write (Repo + Basic Validation)

**When**: Creating/updating with minimal logic

**Example**: Update driver availability
```java
// application/command/UpdateDriverAvailabilityCommand.java
@Service
@RequiredArgsConstructor
public class UpdateDriverAvailabilityCommand {
    private final DriverRepository driverRepository;

    @Transactional
    public void execute(Long driverId, boolean available) {
        DriverEntity driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new NotFoundException("Driver not found"));

        driver.setIsAvailable(available);
        driverRepository.save(driver);
    }
}
```

**Again, is this very different?** No! It's basically the same as your old service method, just separated by operation type.

---

### Level 3: Complex Operation (Multiple Steps + Business Logic)

**When**: Multiple validations, calculations, or coordinating multiple entities

**Example**: Create ride with validation and pricing
```java
// application/command/CreateRideCommand.java
@Service
@RequiredArgsConstructor
public class CreateRideCommand {
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final PassengerRepository passengerRepository;
    private final RideMapper rideMapper;
    private final PricingStrategy pricingStrategy;

    @Transactional
    public Ride execute(CreateRideRequest request) {
        // Validate passenger
        PassengerEntity passenger = passengerRepository
            .findById(request.getPassengerId())
            .orElseThrow(() -> new NotFoundException("Passenger not found"));

        // Find available driver nearby
        List<DriverEntity> availableDrivers = driverRepository
            .findByIsAvailable(true);

        DriverEntity driver = findNearestDriver(
            availableDrivers,
            request.getPickupLocation()
        );

        // Create domain model
        Ride ride = Ride.builder()
            .passengerId(passenger.getId())
            .driverId(driver.getId())
            .pickupLocation(request.getPickupLocation())
            .dropoffLocation(request.getDropoffLocation())
            .build();

        // Apply business logic
        ride.initializeRide();

        // Calculate fare using strategy pattern
        BigDecimal fare = pricingStrategy.calculateFare(
            request.getPickupLocation(),
            request.getDropoffLocation()
        );
        ride.setFare(fare);

        // Mark driver as unavailable
        driver.setIsAvailable(false);
        driverRepository.save(driver);

        // Save ride
        RideEntity entity = rideMapper.toEntity(ride);
        entity = rideRepository.save(entity);

        return rideMapper.toDomain(entity);
    }

    private DriverEntity findNearestDriver(
        List<DriverEntity> drivers,
        Location pickup
    ) {
        // Logic to find nearest driver
        return drivers.stream().findFirst()
            .orElseThrow(() -> new NoDriverAvailableException());
    }
}
```

**This is where Clean Architecture shines!** All this complex logic is testable, organized, and clear.

---

## Your Old Service vs New Structure

### Old Way:
```java
// service/DriverService.java
@Service
public class DriverService {
    private final DriverRepository driverRepository;

    // Mix of read and write operations
    public List<Driver> getAllDrivers() { ... }
    public Driver getDriverById(Long id) { ... }
    public Driver createDriver(Driver driver) { ... }
    public void updateDriver(Long id, Driver driver) { ... }
    public void deleteDriver(Long id) { ... }
}
```

### New Way:
```java
// application/query/GetAllDriversQuery.java
@Service
public class GetAllDriversQuery {
    public List<Driver> execute() { ... }
}

// application/query/GetDriverByIdQuery.java
@Service
public class GetDriverByIdQuery {
    public Driver execute(Long id) { ... }
}

// application/command/CreateDriverCommand.java
@Service
public class CreateDriverCommand {
    public Driver execute(CreateDriverRequest request) { ... }
}

// application/command/UpdateDriverCommand.java
@Service
public class UpdateDriverCommand {
    public void execute(Long id, UpdateDriverRequest request) { ... }
}

// application/command/DeleteDriverCommand.java
@Service
public class DeleteDriverCommand {
    public void execute(Long id) { ... }
}
```

**Key Difference**: Operations are separated by type (read vs write) instead of grouped by entity.

---

## Simplified Alternative: Keep Services, Add Queries/Commands for Complex Operations

If full CQRS feels like too much, you can **compromise**:

### Hybrid Approach:
```
application/
├── service/                    # Keep simple CRUD services
│   ├── DriverService.java     # Basic findAll, findById, save, delete
│   └── PassengerService.java
│
├── command/                    # Complex write operations only
│   ├── CreateRideCommand.java
│   └── ProcessPaymentCommand.java
│
└── query/                      # Complex read operations only
    ├── GetNearbyDriversQuery.java
    └── GetRideHistoryQuery.java
```

**Simple operations stay in services:**
```java
// application/service/DriverService.java
@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    public List<Driver> findAll() {
        return driverRepository.findAll().stream()
            .map(driverMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Driver findById(Long id) {
        return driverRepository.findById(id)
            .map(driverMapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Driver not found"));
    }
}
```

**Complex operations get their own classes:**
```java
// application/command/CreateRideCommand.java - Has complex logic
// application/query/GetNearbyDriversQuery.java - Has filtering logic
```

---

## My Recommendation

Start with **Hybrid Approach**:

1. **Keep it simple** for basic CRUD:
   - `DriverService.findAll()`
   - `DriverService.findById()`
   - `DriverService.save()`

2. **Use Commands** when you have:
   - Multiple validation steps
   - Business calculations
   - Coordinating multiple repositories
   - Transaction management

3. **Use Queries** when you have:
   - Complex filtering/searching
   - Aggregating data from multiple sources
   - Performance-critical reads with caching

---

## Examples from Your Project

### Keep Simple (Use Service):
```java
// application/service/PassengerService.java
public List<Passenger> getAllPassengers() {
    return passengerRepository.findAll()...
}

public Passenger getPassengerById(Long id) {
    return passengerRepository.findById(id)...
}
```

### Make It Command (Complex Logic):
```java
// application/command/CreateRideCommand.java
// - Validates passenger and driver
// - Calculates fare
// - Updates driver availability
// - Sends notifications
```

### Make It Query (Complex Read):
```java
// application/query/GetAvailableDriversNearbyQuery.java
// - Filters by availability
// - Calculates distance from pickup
// - Sorts by rating and distance
// - Returns top 5 matches
```

---

## Bottom Line

**You DON'T need to make everything a separate Command/Query class!**

- Simple operations → Keep them simple (minimal wrapper or service)
- Complex operations → Use Command/Query pattern

The architecture should **help you**, not **burden you** with unnecessary code. Start simple and refactor to Commands/Queries when you actually need the separation.
