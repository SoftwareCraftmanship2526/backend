# Authentication Implementation

## Overview

This document explains how user registration and login are implemented in the Uber backend project.

---

## Registration Flow

### Passenger Registration

**Endpoint**: `POST /api/auth/register/passenger`

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "Password123",
  "phoneNumber": "+1234567890"
}
```

**Flow**:
1. Request arrives at `AuthController.registerPassenger()`
2. Spring validates the request (email format, password strength, required fields)
3. `AuthenticationService.registerPassenger()` is called
4. Service checks if email already exists in database
5. Password is hashed using BCrypt
6. New `PassengerEntity` is created with:
   - Hashed password
   - Role set to `PASSENGER`
   - Initial rating of 5.0
7. Entity is saved to database
8. JWT token is generated with userId and role
9. `AuthResponse` is returned with token

**Response** (HTTP 201):
```json
{
  "token": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": 1,
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "PASSENGER"
}
```

### Driver Registration

**Endpoint**: `POST /api/auth/register/driver`

**Request Body**:
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com",
  "password": "Password123",
  "phoneNumber": "+1234567890",
  "licenseNumber": "DL-12345"
}
```

**Flow**: Same as passenger registration, but:
- Creates `DriverEntity` instead
- Role set to `DRIVER`
- Includes license number
- Sets `isAvailable` to false initially
- Initial driver rating of 5.0

---

## Login Flow

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "Password123"
}
```

**Flow**:
1. Request arrives at `AuthController.login()`
2. `AuthenticationService.login()` is called
3. Spring Security's `AuthenticationManager` validates credentials:
   - Loads user from database via `UserDetailsServiceImpl`
   - Compares hashed password using BCrypt
   - If invalid, throws `BadCredentialsException`
4. If valid, user details are loaded
5. System checks if user is passenger or driver
6. JWT token is generated with userId and role
7. `AuthResponse` is returned with token

**Response** (HTTP 200):
```json
{
  "token": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": 1,
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "PASSENGER"
}
```

---

## JWT Token Structure

The JWT token contains:
```json
{
  "sub": "john@example.com",
  "userId": 1,
  "role": "PASSENGER",
  "iat": 1733599200,
  "exp": 1733685600
}
```

- **sub**: User's email
- **userId**: Database ID
- **role**: User role (PASSENGER/DRIVER/ADMIN)
- **iat**: Issued at timestamp
- **exp**: Expiration timestamp (24 hours later)

---

## Protected Endpoints

After login, clients must include the JWT token in requests:

```bash
Authorization: Bearer eyJhbGc...
```

**Example**:
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGc..."
```

**How it works**:
1. `JwtAuthenticationFilter` intercepts the request
2. Extracts JWT from Authorization header
3. Validates token signature and expiration
4. Extracts user email and role
5. Sets Spring Security context
6. Request proceeds to controller

---

## Key Components

### 1. AuthController
- `POST /api/auth/register/passenger` - Register passenger
- `POST /api/auth/register/driver` - Register driver
- `POST /api/auth/login` - Login
- `GET /api/auth/me` - Get current user info (protected)

**Location**: `auth/api/web/AuthController.java`

### 2. AuthenticationService
- `registerPassenger()` - Handles passenger registration
- `registerDriver()` - Handles driver registration
- `login()` - Handles login
- `validateEmailNotExists()` - Checks for duplicate emails
- `generateAuthResponse()` - Creates JWT response

**Location**: `auth/application/service/AuthenticationService.java`

### 3. JwtService
- `generateToken()` - Creates JWT with userId and role
- `validateToken()` - Validates JWT signature and expiration
- `extractUsername()` - Gets email from token
- `extractUserId()` - Gets user ID from token
- `extractRole()` - Gets role from token

**Location**: `auth/infrastructure/security/JwtService.java`

### 4. JwtAuthenticationFilter
- Intercepts all requests
- Validates JWT tokens
- Sets Spring Security context

**Location**: `auth/infrastructure/security/JwtAuthenticationFilter.java`

### 5. SecurityConfig
- Configures Spring Security
- Defines public endpoints (no token needed)
- Defines protected endpoints (token required)
- Sets up password encoder (BCrypt)

**Location**: `auth/infrastructure/config/SecurityConfig.java`

---

## Password Security

**Hashing**: BCrypt with automatic salt generation

**Validation Requirements**:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit

**Example**:
```java
// Registration
String hashedPassword = passwordEncoder.encode("Password123");
// Stored in DB: $2a$10$N9qo8uLOickgx2ZMRZoMye...

// Login
boolean matches = passwordEncoder.matches("Password123", hashedPassword);
// Returns: true
```

---

## Error Handling

### Validation Errors (HTTP 400)
```json
{
  "timestamp": "2025-12-07T20:00:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "Email must be valid",
    "password": "Password must be at least 8 characters"
  }
}
```

### Invalid Credentials (HTTP 401)
```json
{
  "timestamp": "2025-12-07T20:00:00",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid email or password"
}
```

### Email Already Exists (HTTP 400)
```json
{
  "timestamp": "2025-12-07T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already registered"
}
```

**Handler**: `AuthExceptionHandler.java`

---

## Testing the API

### 1. Register a Passenger
```bash
curl -X POST http://localhost:8080/api/auth/register/passenger \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "Password123",
    "phoneNumber": "+1234567890"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password123"
  }'
```

### 3. Access Protected Endpoint
```bash
# Save the token from login response
TOKEN="eyJhbGc..."

# Use it in requests
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## Database Schema

### Accounts Table
```sql
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PASSENGER', 'DRIVER', 'ADMIN'))
);
```

### Passengers Table
```sql
CREATE TABLE passengers (
    id BIGINT PRIMARY KEY REFERENCES accounts(id),
    passenger_rating DOUBLE PRECISION DEFAULT 5.0
);
```

### Drivers Table
```sql
CREATE TABLE drivers (
    id BIGINT PRIMARY KEY REFERENCES accounts(id),
    driver_rating DOUBLE PRECISION DEFAULT 5.0,
    is_available BOOLEAN DEFAULT FALSE,
    license_number VARCHAR(50) NOT NULL
);
```

---

## Environment Configuration

Required in `.env` file:
```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/uber_db
DB_USERNAME=uber
DB_PASSWORD=uber

# JWT Secret (generate with: openssl rand -base64 64)
JWT_SECRET=your-secret-key-here
```

---

## Summary

**Registration**:
1. Client sends user data
2. Server validates and hashes password
3. User saved to database with role
4. JWT token generated and returned

**Login**:
1. Client sends email and password
2. Server validates credentials
3. JWT token generated and returned

**Protected Access**:
1. Client sends JWT in Authorization header
2. Server validates token
3. Request processed with user context

That's it! The authentication system uses JWT for stateless authentication and BCrypt for secure password storage.