# Quick Start Guide - Clean Architecture

## TL;DR - Where Do I Put My Code?

| I want to... | Put it in... | Example |
|-------------|-------------|---------|
| Add business logic | `domain/model/` | `ride.startRide()` |
| Get/List data (READ) | `application/query/` | `GetAllDriversQuery.java` |
| Create/Update/Delete (WRITE) | `application/command/` | `CreateRideCommand.java` |
| Add REST endpoint | `api/web/` | `DriverController.java` |
| Add request/response objects | `api/dto/` | `CreateRideRequest.java` |
| Add database access | `infrastructure/repository/` | `DriverRepository.java` |

## Common Scenarios

### Scenario 1: Add "Get All Drivers" endpoint

1. **Create Query** in `application/query/GetAllDriversQuery.java`:
```java
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

2. **Create Controller** in `api/web/DriverController.java`:
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

### Scenario 2: Add "Create Ride" endpoint

1. **Create Command** in `application/command/CreateRideCommand.java`:
```java
@Service
@RequiredArgsConstructor
public class CreateRideCommand {
    private final RideRepository rideRepository;
    private final RideMapper rideMapper;

    @Transactional
    public Ride execute(Long passengerId, Location pickup, Location dropoff) {
        Ride ride = Ride.builder()
            .passengerId(passengerId)
            .pickupLocation(pickup)
            .dropoffLocation(dropoff)
            .build();

        ride.initializeRide();

        RideEntity entity = rideMapper.toEntity(ride);
        entity = rideRepository.save(entity);

        return rideMapper.toDomain(entity);
    }
}
```

2. **Create DTO** in `api/dto/CreateRideRequest.java`:
```java
@Data
public class CreateRideRequest {
    private Long passengerId;
    private Location pickupLocation;
    private Location dropoffLocation;
}
```

3. **Create Controller** in `api/web/RideController.java`:
```java
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {
    private final CreateRideCommand createRideCommand;

    @PostMapping
    public Ride create(@RequestBody CreateRideRequest request) {
        return createRideCommand.execute(
            request.getPassengerId(),
            request.getPickupLocation(),
            request.getDropoffLocation()
        );
    }
}
```

### Scenario 3: Add business logic to Driver

Add method to `domain/model/Driver.java`:
```java
public boolean canAcceptPoolRides() {
    return isAvailable && hasGoodRating() && currentVehicleId != null;
}
```

## Key Rules

### ✅ DO

- Put READ operations in `application/query/`
- Put WRITE operations in `application/command/`
- Return domain models from commands/queries
- Use mappers to convert entity ↔ domain
- Add business logic to domain models
- Use DTOs for API requests/responses

### ❌ DON'T

- Don't put JPA annotations in `domain/model/`
- Don't expose entities outside `infrastructure/`
- Don't mix business logic in controllers
- Don't access repositories from controllers
- Don't skip the mapper step

## File Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Query | `Get{Entity}{Filter}Query.java` | `GetAvailableDriversQuery.java` |
| Command | `{Action}{Entity}Command.java` | `CreateRideCommand.java` |
| Controller | `{Entity}Controller.java` | `DriverController.java` |
| Request DTO | `{Action}{Entity}Request.java` | `CreateRideRequest.java` |
| Response DTO | `{Entity}Response.java` | `DriverResponse.java` |
| Mapper | `{Entity}Mapper.java` | `DriverMapper.java` |

## Cheat Sheet: Command vs Query

**Use QUERY when:**
- GET request
- Only reading data
- No state changes
- Examples: `GetDriverQuery`, `ListRidesQuery`, `SearchDriversQuery`

**Use COMMAND when:**
- POST, PUT, PATCH, DELETE request
- Creating, updating, or deleting data
- Changes system state
- Examples: `CreateRideCommand`, `UpdateDriverCommand`, `CancelRideCommand`

## The Old vs New

| Old (Before) | New (After) |
|-------------|------------|
| `service/DriverService.java` | Split into `GetDriverQuery` + `UpdateDriverCommand` |
| `domain/entity/Driver.java` | `domain/model/Driver.java` + `infrastructure/persistence/entity/DriverEntity.java` |
| Controller calls repository directly | Controller → Command/Query → Repository |
| JPA entities everywhere | Domain models everywhere, entities hidden |

## Quick Reference: Layer Dependencies

```
api/web/          →  application/command/
                  →  application/query/

application/      →  domain/model/
                  →  infrastructure/repository/
                  →  infrastructure/persistence/mapper/

infrastructure/   →  domain/model/ (for mappers)
```

**Rule**: Higher layers can depend on lower layers, but NOT the reverse!

---

For full details, see [CLEAN_ARCHITECTURE.md](CLEAN_ARCHITECTURE.md)
