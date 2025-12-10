# Quick Reference Guide

## Project Structure at a Glance

```
backend/
├── ride/              # 🚗 Ride Management
├── payment/           # 💳 Payment Processing
├── driver/            # 👨‍✈️ Driver Management
├── passenger/         # 👤 Passenger Management
├── rating/            # ⭐ Rating System
└── shared/            # 🔧 Shared Utilities
```

## Finding Things

| I want to... | Look in... |
|--------------|-----------|
| Add a new ride endpoint | `ride/api/` |
| Change payment business logic | `payment/domain/model/` |
| Query driver data | `driver/application/query/` |
| Add a new rating command | `rating/application/command/` |
| Change database schema | `*/infrastructure/persistence/` |
| Add shared utilities | `shared/` |

## Feature Structure Template

Every feature follows this pattern:

```
feature-name/
├── api/                      # REST Controllers
│   └── FeatureController.java
│
├── application/              # Use Cases (CQRS)
│   ├── command/              # Write operations
│   │   └── CreateFeatureCommand.java
│   └── query/                # Read operations
│       └── FeatureQueryService.java
│
├── domain/                   # Business Logic
│   ├── model/                # Domain models
│   │   └── Feature.java
│   └── enums/                # Enumerations
│       └── FeatureStatus.java
│
└── infrastructure/           # Technical Details
    ├── persistence/          # JPA & Mappers
    │   ├── FeatureEntity.java
    │   └── FeatureMapper.java
    └── repository/           # Data Access
        └── FeatureRepository.java
```

## Common Tasks

### Add a New Feature

```bash
# 1. Create folder structure
cd src/main/java/com/uber/backend
mkdir -p new-feature/{api,application/{command,query},domain/model,infrastructure/{persistence,repository}}

# 2. Add domain model (new-feature/domain/model/)
# 3. Add repository (new-feature/infrastructure/repository/)
# 4. Add query service (new-feature/application/query/)
# 5. Add controller (new-feature/api/)
```

### Add a New Endpoint

```java
// In feature/api/FeatureController.java

@RestController
@RequestMapping("/api/features")
public class FeatureController {

    @GetMapping
    public ResponseEntity<List<Feature>> getAll() {
        return ResponseEntity.ok(queryService.getAll());
    }

    @PostMapping
    public ResponseEntity<Feature> create(@RequestBody CreateFeatureRequest request) {
        return ResponseEntity.ok(commandService.create(request));
    }
}
```

### Add Business Logic

```java
// In feature/domain/model/Feature.java

public class Feature {
    private Long id;
    private FeatureStatus status;

    // Business method
    public void activate() {
        if (this.status == FeatureStatus.PENDING) {
            this.status = FeatureStatus.ACTIVE;
        }
    }
}
```

### Add a Query

```java
// In feature/application/query/FeatureQueryService.java

@Service
public class FeatureQueryService {

    public List<Feature> getAllActive() {
        return repository.findAll()
            .stream()
            .filter(Feature::isActive)
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
```

### Add a Command

```java
// In feature/application/command/FeatureCommandService.java

@Service
public class FeatureCommandService {

    public Feature create(CreateFeatureRequest request) {
        Feature feature = Feature.builder()
            .name(request.getName())
            .status(FeatureStatus.PENDING)
            .build();

        FeatureEntity entity = mapper.toEntity(feature);
        FeatureEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

## Package Names

| Feature | Package |
|---------|---------|
| Ride | `com.uber.backend.ride` |
| Payment | `com.uber.backend.payment` |
| Driver | `com.uber.backend.driver` |
| Passenger | `com.uber.backend.passenger` |
| Rating | `com.uber.backend.rating` |
| Shared | `com.uber.backend.shared` |

## API Endpoints

| Feature | Endpoint | Description |
|---------|----------|-------------|
| Rides | `/api/rides` | Ride management |
| Payments | `/api/payments` | Payment processing |
| Drivers | `/api/drivers` | Driver management |
| Passengers | `/api/passengers` | Passenger management |
| Ratings | `/api/ratings` | Rating system |
| Seed | `/api/seed` | Database seeding |

## Key Principles

### ✅ DO
- Put business logic in domain models
- Keep features independent
- Use CQRS (separate reads and writes)
- Follow the existing folder structure
- Test domain logic thoroughly

### ❌ DON'T
- Mix features in the same package
- Put business logic in controllers
- Access other features directly
- Break the folder structure convention
- Depend on frameworks in domain models

## Running & Testing

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Test
mvn test

# Seed database
curl -X POST http://localhost:8080/api/seed/reset
```

## IDE Tips

### IntelliJ IDEA
- Right-click on a feature folder → "Mark Directory as" → "Sources Root"
- Use "Navigate → File Structure" (Cmd+F12) to see all methods in a class
- Use "Navigate → Go to Class" (Cmd+O) to quickly find classes

### VS Code
- Install "Java Extension Pack"
- Use "Go to Symbol in Workspace" (Cmd+T) to find classes
- Use folder search to stay within one feature

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Can't find a class | Check the feature's package structure |
| Build fails | Run `mvn clean install` |
| Tests fail | Check database connection |
| Feature not loading | Ensure `@SpringBootApplication` scans the package |

## Further Reading

- [SCREAMING_ARCHITECTURE.md](SCREAMING_ARCHITECTURE.md) - Full architecture guide
- [README.md](README.md) - Project overview

---

**Remember**: The structure should **scream** what the app does! 🚗💳👨‍✈️
