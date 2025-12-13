# Backend Documentation

Welcome to the Uber-like Backend API documentation!

Before doing any testing read the 'Seed Database' part of the readme

## Event Storming
You can find the event storming here:
https://miro.com/app/board/uXjVGex5fVY=/

## Architecture

This project implements **Screaming Architecture** - an architectural approach where the codebase structure immediately reveals what the application does.

📖 **[Read the full Screaming Architecture guide](SCREAMING_ARCHITECTURE.md)**

## Quick Overview

### What is this application?

Just look at the folder structure:

```
com.uber.backend/
├── ride/           # Ride management
├── payment/        # Payment processing
├── driver/         # Driver management
├── passenger/      # Passenger management
├── rating/         # Rating system
└── shared/         # Shared utilities
```

It's immediately clear: **This is a ride-sharing platform!**

## Key Features

### 🚗 Ride Management
- Request rides
- Track ride status
- Pricing strategies (UberX, UberBlack)
- Location: `ride/`

### 💳 Payment Processing
- Process payments
- Multiple payment methods
- Payment status tracking
- Location: `payment/`

### 👨‍✈️ Driver Management
- Driver profiles
- Vehicle management
- Availability tracking
- Location: `driver/`

### 👤 Passenger Management
- Passenger profiles
- Saved addresses
- Ride history
- Location: `passenger/`

### ⭐ Rating System
- Rate drivers and passengers
- Rating aggregation
- Location: `rating/`

## Project Structure

Each feature is **self-contained** with:

```
feature/
├── api/                 # REST endpoints
├── application/         # Business logic
│   ├── command/         # Write operations (CQRS)
│   └── query/           # Read operations (CQRS)
├── domain/              # Domain models
└── infrastructure/      # Data access
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or H2 for testing)

### Build & Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Run tests
mvn test
```

### API Endpoints

Once running, the API is available at `http://localhost:8080/api/`

- **Rides**: `/api/rides`
- **Payments**: `/api/payments`
- **Drivers**: `/api/drivers`
- **Passengers**: `/api/passengers`
- **Ratings**: `/api/ratings`
- **Seed Data**: `/api/seed`

### Seed Database

To populate the database with sample data:

```bash
POST http://localhost:8080/api/seed/reset
```

## Architecture Documentation

📖 **[Screaming Architecture Guide](SCREAMING_ARCHITECTURE.md)** - Detailed explanation of the architecture

## Development Guidelines

### Adding a New Feature

1. Create feature folder structure:
```bash
mkdir -p new-feature/{api,application/{command,query},domain/model,infrastructure/{persistence,repository}}
```

2. Follow the same pattern as existing features
3. Keep features independent
4. Use the `shared/` folder for common code

### CQRS Pattern

- **Commands** (`application/command/`) - Write operations
  - Create, Update, Delete
  - Business logic execution

- **Queries** (`application/query/`) - Read operations
  - Get, List, Search
  - Optimized for reading

### Domain-Driven Design

- Business logic lives in `domain/model/`
- Domain models are POJOs (no framework dependencies)
- Use value objects for concepts (Location, Money, etc.)

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: JPA/Hibernate with PostgreSQL
- **Build Tool**: Maven
- **Language**: Java 17
- **Architecture**: Screaming Architecture with CQRS

## Benefits of This Architecture

### ✅ Immediate Understanding
The folder structure tells you what the app does

### ✅ Independent Features
Each feature can be developed and tested separately

### ✅ Easy Refactoring
Want to extract a microservice? Just take one folder!

### ✅ Team Scalability
Multiple teams can work on different features without conflicts

### ✅ Framework Independence
Core business logic doesn't depend on Spring or JPA

## Contributing

When adding new features:
1. Follow the Screaming Architecture pattern
2. Keep features self-contained
3. Write tests for domain logic
4. Document major changes

## License

[Your License Here]
