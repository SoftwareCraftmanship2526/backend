# Screaming Architecture

This project implements **Screaming Architecture** - an architectural approach where the folder structure immediately tells you what the application does, not what framework it uses.

## What is Screaming Architecture?

> "The architecture should scream the intent of the system, not the framework."
> — Robert C. Martin (Uncle Bob)

When you look at this codebase, you immediately see:
- **Ride Management**
- **Payment Processing**
- **Driver Management**
- **Passenger Management**
- **Rating System**

Not "controllers", "services", "repositories" - but **what the application actually does**.

## Project Structure

```
com.uber.backend/
│
├── ride/                    # Ride Management Feature
│   ├── api/                 # REST controllers
│   ├── application/         # Use cases (CQRS)
│   │   ├── command/         # Write operations
│   │   └── query/           # Read operations
│   ├── domain/              # Business logic
│   │   ├── model/           # Ride aggregate
│   │   ├── enums/           # RideStatus, RideType
│   │   └── strategy/        # Pricing strategies
│   └── infrastructure/      # Technical details
│       ├── persistence/     # JPA entities, mappers
│       └── repository/      # Data access
│
├── payment/                 # Payment Processing Feature
│   ├── api/
│   ├── application/
│   │   ├── command/
│   │   └── query/
│   ├── domain/
│   │   ├── model/           # Payment aggregate
│   │   └── enums/           # PaymentStatus, PaymentMethod
│   └── infrastructure/
│       ├── persistence/
│       └── repository/
│
├── driver/                  # Driver Management Feature
│   ├── api/
│   ├── application/
│   │   ├── command/
│   │   └── query/
│   ├── domain/
│   │   └── model/           # Driver, Vehicle, Account
│   └── infrastructure/
│       ├── persistence/
│       └── repository/
│
├── passenger/               # Passenger Management Feature
│   ├── api/
│   ├── application/
│   │   ├── command/
│   │   └── query/
│   ├── domain/
│   │   └── model/           # Passenger aggregate
│   └── infrastructure/
│       ├── persistence/
│       └── repository/
│
├── rating/                  # Rating System Feature
│   ├── api/
│   ├── application/
│   │   ├── command/
│   │   └── query/
│   ├── domain/
│   │   ├── model/           # Rating aggregate
│   │   └── enums/           # RatingSource
│   └── infrastructure/
│       ├── persistence/
│       └── repository/
│
└── shared/                  # Shared Kernel
    ├── api/                 # Cross-cutting API concerns
    │   ├── seed/            # Database seeding
    │   └── web/             # Shared controllers
    └── domain/              # Shared domain objects
        └── valueobject/     # Location, etc.
```

## Key Principles

### 1. Package by Feature, Not by Layer

**❌ Traditional (Package by Layer):**
```
controllers/
  RideController.java
  PaymentController.java
services/
  RideService.java
  PaymentService.java
repositories/
  RideRepository.java
  PaymentRepository.java
```

**✅ Screaming Architecture (Package by Feature):**
```
ride/
  api/RideController.java
  application/RideService.java
  infrastructure/RideRepository.java
payment/
  api/PaymentController.java
  application/PaymentService.java
  infrastructure/PaymentRepository.java
```

### 2. Self-Contained Features

Each feature is **completely independent** and contains everything it needs:
- API endpoints
- Business logic
- Data access
- Domain models

You can understand and modify a feature without touching other features.

### 3. Clear Boundaries

Each feature has clear boundaries:
- **api/** - External interface (HTTP endpoints)
- **application/** - Use cases and orchestration
- **domain/** - Core business logic
- **infrastructure/** - Technical implementation details

### 4. CQRS-Ready

Each feature separates:
- **command/** - Write operations (create, update, delete)
- **query/** - Read operations (get, list, search)

This makes it easy to scale reads and writes independently.

## Benefits

### 1. Immediate Understanding
New developers can look at the structure and instantly understand:
- What does this application do? (Ride-sharing platform)
- Where is the payment logic? (In `payment/`)
- How do I add a new ride feature? (In `ride/`)

### 2. Independent Development
Teams can work on different features without conflicts:
- Payment team works in `payment/`
- Ride team works in `ride/`
- Rating team works in `rating/`

### 3. Easy Refactoring
Want to extract a microservice?
- Just take the `payment/` folder
- Add network boundaries
- Deploy independently

### 4. Testability
Each feature can be tested in isolation:
```
ride/
  test/
    domain/     # Unit tests for business logic
    api/        # Integration tests for endpoints
```

### 5. Framework Independence
The domain logic doesn't depend on Spring, JPA, or any framework:
- Domain models are POJOs
- Business logic is pure Java
- Framework code is in `infrastructure/`

## Comparison: Clean Architecture vs Screaming Architecture

### Clean Architecture (Package by Layer)
```
domain/
  Ride.java
  Payment.java
  Driver.java
application/
  RideService.java
  PaymentService.java
infrastructure/
  RideRepository.java
  PaymentRepository.java
api/
  RideController.java
  PaymentController.java
```
**Problem**: You can't tell what the app does. Is it an e-commerce site? A CRM? A blog?

### Screaming Architecture (Package by Feature)
```
ride/           ← "This handles rides!"
payment/        ← "This handles payments!"
driver/         ← "This handles drivers!"
```
**Benefit**: Instantly clear this is a ride-sharing platform!

## Adding a New Feature

To add a new feature (e.g., `notification/`):

1. **Create the feature structure:**
```bash
mkdir -p notification/{api,application/{command,query},domain/model,infrastructure/{persistence,repository}}
```

2. **Add domain model:**
```java
// notification/domain/model/Notification.java
public class Notification {
    private Long id;
    private String message;
    private NotificationStatus status;
}
```

3. **Add application logic:**
```java
// notification/application/query/NotificationQueryService.java
@Service
public class NotificationQueryService {
    public List<Notification> getAllNotifications() { ... }
}
```

4. **Add API controller:**
```java
// notification/api/NotificationController.java
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @GetMapping
    public ResponseEntity<List<Notification>> getAll() { ... }
}
```

That's it! The feature is self-contained and doesn't affect other features.

## Best Practices

### 1. Keep Features Independent
- Avoid direct dependencies between features
- Use events for cross-feature communication
- Share common code via `shared/`

### 2. Follow CQRS Within Features
- **Commands** in `application/command/` for writes
- **Queries** in `application/query/` for reads
- Keep them separate

### 3. Domain-Driven Design
- Put business logic in domain models
- Keep domain models framework-agnostic
- Use value objects for concepts like Location

### 4. Infrastructure Isolation
- All Spring/JPA code goes in `infrastructure/`
- Domain models should be POJOs
- Use mappers to convert between domain and persistence

### 5. Shared Kernel
- Only put truly shared code in `shared/`
- Value objects (Location)
- Common utilities
- Cross-cutting concerns (logging, security)

## Example: Ride Feature Walkthrough

### 1. Domain Layer
```java
// ride/domain/model/Ride.java
public class Ride {
    private Long id;
    private RideStatus status;
    private BigDecimal fareAmount;
    private Location pickupLocation;

    public void completeRide() {
        if (this.status == RideStatus.IN_PROGRESS) {
            this.status = RideStatus.COMPLETED;
        }
    }
}
```

### 2. Application Layer
```java
// ride/application/query/RideQueryService.java
@Service
public class RideQueryService {
    public List<Ride> getAllRides() {
        return rideRepository.findAll()
            .stream()
            .map(rideMapper::toDomain)
            .collect(Collectors.toList());
    }
}
```

### 3. API Layer
```java
// ride/api/RideController.java
@RestController
@RequestMapping("/api/rides")
public class RideController {
    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(rideQueryService.getAllRides());
    }
}
```

### 4. Infrastructure Layer
```java
// ride/infrastructure/persistence/RideEntity.java
@Entity
@Table(name = "rides")
public class RideEntity {
    @Id
    private Long id;
    private String status;
    private BigDecimal fareAmount;
}

// ride/infrastructure/persistence/RideMapper.java
public class RideMapper {
    public Ride toDomain(RideEntity entity) { ... }
    public RideEntity toEntity(Ride domain) { ... }
}
```

## Migration from Traditional Architecture

If you have a traditional layered architecture:

1. **Identify features** - What are the main capabilities?
2. **Create feature folders** - One per capability
3. **Move code** - Group by feature, not layer
4. **Refactor** - Make features independent
5. **Test** - Ensure everything still works

## Resources

- [The Clean Architecture (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Screaming Architecture (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2011/09/30/Screaming-Architecture.html)
- [Package by Feature, not Layer](https://phauer.com/2020/package-by-feature/)

## Summary

Screaming Architecture makes your codebase:
- **Readable** - Anyone can understand what it does
- **Maintainable** - Features are isolated and independent
- **Scalable** - Easy to extract into microservices
- **Testable** - Each feature can be tested separately

When someone opens this codebase, it **screams** "I'm a ride-sharing platform!" 🚗
