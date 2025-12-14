# MJT Rides Backend

> A modern, enterprise-grade ride-sharing platform built with Spring Boot and Clean Architecture principles.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

## 📋 Event Storming

View our complete event storming board: **[Miro Board](https://miro.com/app/board/uXjVGbH4Fzw=/?share_link_id=948262413817)**

---

## Architecture

This project implements **Screaming Architecture** - an architectural approach where the codebase structure immediately reveals what the application does.

📖 **[Read the full Screaming Architecture guide](docs/architecture/SCREAMING_ARCHITECTURE.md)**

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

- **Java 21** or higher (for local development and tests)
- **Maven 3.8+** (for local development and tests)
- **Docker & Docker Compose**

### Quick Start with Docker

1. **Configure Environment Variables**

Create a `docker-compose.secrets.yml` file or `.env` file with your configuration:

```bash
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=uber_db
DDL_AUTO=update
JWT_SECRET=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW5zLW1pbmltdW0tMjU2LWJpdHMtcmVxdWlyZWQtZm9yLWhzMjU2LWFsZ29yaXRobQ==
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

2. **Start All Services**

```bash
# Start PostgreSQL database + Backend application
docker-compose up -d

# View logs
docker-compose logs -f

# View only backend logs
docker-compose logs -f backend
```

This will:

- Start PostgreSQL 16 container on port 5432
- Build and start the Spring Boot application on port 8080
- Automatically create database schema

The application will be available at `http://localhost:8080`

3. **Seed the Database**

Once the containers are running, populate with test data:

```bash
# Using curl
curl -X POST http://localhost:8080/api/seed/reset

# Or using your API client (Postman, etc.)
POST http://localhost:8080/api/seed/reset
```

This creates sample passengers, drivers and vehicles for testing.

4. **Stop Services**

```bash
# Stop containers
docker-compose down

# Stop and remove volumes (full cleanup)
docker-compose down -v
```

### Local Development (Without Docker)

If you want to run the backend locally for development:

1. **Start only the database**:

```bash
docker-compose up -d db
```

2. **Set environment variables** pointing to localhost:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/uber_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
# ... (other variables)
```

3. **Run the application**:

```bash
mvn spring-boot:run
```

### Run Tests

```bash
# Run all tests (includes E2E tests with Testcontainers)
mvn test

# Run specific test
mvn test -Dtest=RideE2ETest
```

**Note**: Docker must be running for E2E tests (Testcontainers will automatically start a PostgreSQL container).

### API Endpoints

Once running, the API is available at `http://localhost:8080/api/`

- **Rides**: `/api/rides`
- **Payments**: `/api/payments`
- **Drivers**: `/api/drivers`
- **Passengers**: `/api/passengers`
- **Ratings**: `/api/ratings`
- **Seed Data**: `/api/seed`

## Architecture Documentation

📖 **[Screaming Architecture Guide](docs/architecture/SCREAMING_ARCHITECTURE.md)** - Detailed explanation of the architecture

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

- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL 16 (Docker)
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven 3.8+
- **Language**: Java 21
- **Security**: Spring Security + JWT
- **Testing**: JUnit 5, Mockito, Testcontainers
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
