# Vehicle Management

## Overview

This document explains how drivers can add and manage their vehicles in the Uber backend system.

---

## Add Vehicle

**Endpoint**: `POST /api/vehicles`

**Authentication**: Required (Driver role)

**Request Body**:
```json
{
  "licensePlate": "ABC-123",
  "model": "Toyota Camry",
  "color": "Black",
  "type": "UBER_X"
}
```

**Validation Rules**:
- License plate: 3-15 characters (uppercase letters, numbers, hyphens only)
- Model: Required
- Color: Required
- Type: Required (UBER_X, UBER_BLACK, or UBER_POOL)

**Flow**:
1. Request arrives at `VehicleController.addVehicle()`
2. JWT token is validated and driver ID extracted
3. `VehicleService.addVehicle()` is called
4. Service checks if license plate already exists
5. New `VehicleEntity` is created and saved
6. If driver has no current vehicle, this vehicle is set as current
7. License plate is automatically converted to uppercase
8. `VehicleDTO` is returned

**Response** (HTTP 201):
```json
{
  "id": 1,
  "licensePlate": "ABC-123",
  "model": "Toyota Camry",
  "color": "Black",
  "type": "UBER_X",
  "driverId": 1
}
```

**Errors**:
- `400 Bad Request` - Validation failed or duplicate license plate
- `401 Unauthorized` - No valid JWT token
- `403 Forbidden` - User is not a driver

---

## Get All My Vehicles

**Endpoint**: `GET /api/vehicles`

**Authentication**: Required (Driver role)

**Flow**:
1. JWT token validated, driver ID extracted
2. `VehicleService.getDriverVehicles()` retrieves all vehicles for driver
3. List of `VehicleDTO` returned

**Response** (HTTP 200):
```json
[
  {
    "id": 1,
    "licensePlate": "ABC-123",
    "model": "Toyota Camry",
    "color": "Black",
    "type": "UBER_X",
    "driverId": 1
  },
  {
    "id": 2,
    "licensePlate": "XYZ-789",
    "model": "Honda Accord",
    "color": "White",
    "type": "UBER_BLACK",
    "driverId": 1
  }
]
```

---

## Get Vehicle by ID

**Endpoint**: `GET /api/vehicles/{vehicleId}`

**Authentication**: Required (Driver role)

**Response** (HTTP 200):
```json
{
  "id": 1,
  "licensePlate": "ABC-123",
  "model": "Toyota Camry",
  "color": "Black",
  "type": "UBER_X",
  "driverId": 1
}
```

**Errors**:
- `404 Not Found` - Vehicle doesn't exist

---

## Update Vehicle

**Endpoint**: `PUT /api/vehicles/{vehicleId}`

**Authentication**: Required (Driver role)

**Request Body** (all fields optional):
```json
{
  "model": "Toyota Camry 2024",
  "color": "Silver",
  "type": "UBER_BLACK"
}
```

**Flow**:
1. JWT token validated, driver ID extracted
2. Service verifies vehicle belongs to driver
3. Only provided fields are updated
4. Updated `VehicleDTO` returned

**Response** (HTTP 200):
```json
{
  "id": 1,
  "licensePlate": "ABC-123",
  "model": "Toyota Camry 2024",
  "color": "Silver",
  "type": "UBER_BLACK",
  "driverId": 1
}
```

**Errors**:
- `400 Bad Request` - Vehicle doesn't belong to driver
- `404 Not Found` - Vehicle doesn't exist

---

## Delete Vehicle

**Endpoint**: `DELETE /api/vehicles/{vehicleId}`

**Authentication**: Required (Driver role)

**Flow**:
1. JWT token validated, driver ID extracted
2. Service verifies vehicle belongs to driver
3. If vehicle is current vehicle, it's unset
4. Vehicle is deleted

**Response** (HTTP 204 No Content)

**Errors**:
- `400 Bad Request` - Vehicle doesn't belong to driver
- `404 Not Found` - Vehicle doesn't exist

---

## Set Current Vehicle

**Endpoint**: `PUT /api/vehicles/{vehicleId}/set-current`

**Authentication**: Required (Driver role)

**Flow**:
1. JWT token validated, driver ID extracted
2. Service verifies vehicle belongs to driver
3. Vehicle is set as driver's current vehicle
4. Updated `VehicleDTO` returned

**Response** (HTTP 200):
```json
{
  "id": 1,
  "licensePlate": "ABC-123",
  "model": "Toyota Camry",
  "color": "Black",
  "type": "UBER_X",
  "driverId": 1
}
```

---

## Key Components

### VehicleController
Handles HTTP requests and responses for vehicle management.

**Location**: `driver/api/web/VehicleController.java`

**Security**: All endpoints require `@PreAuthorize("hasRole('DRIVER')")`

**JWT Integration**: Uses `JwtUtil` to extract driver ID from JWT token

### VehicleService
Contains business logic for vehicle operations.

**Location**: `driver/application/service/VehicleService.java`

**Methods**:
- `addVehicle()` - Add new vehicle with validation
- `getDriverVehicles()` - Get all vehicles for a driver
- `getVehicleById()` - Get specific vehicle
- `updateVehicle()` - Update vehicle information
- `deleteVehicle()` - Delete vehicle
- `setCurrentVehicle()` - Set as current vehicle

**Validation**:
- Checks driver exists
- Validates vehicle ownership
- Prevents duplicate license plates
- Manages current vehicle state

### VehicleRepository
Database access for vehicles.

**Location**: `driver/infrastructure/repository/VehicleRepository.java`

**Methods**:
- `findByLicensePlate()` - Find vehicle by license plate
- `findByDriverId()` - Find all vehicles for a driver

### JwtUtil
Helper for extracting user information from JWT tokens.

**Location**: `auth/infrastructure/security/JwtUtil.java`

**Methods**:
- `extractUserIdFromRequest()` - Extract user ID from JWT
- `extractRoleFromRequest()` - Extract role from JWT

---

## Testing Examples

### 1. Register as Driver
```bash
curl -X POST http://localhost:8080/api/auth/register/driver \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Driver",
    "email": "john.driver@example.com",
    "password": "Password123",
    "phoneNumber": "+1234567890",
    "licenseNumber": "DL-12345"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.driver@example.com",
    "password": "Password123"
  }'
```

Save the token from the response.

### 3. Add Vehicle
```bash
TOKEN="your-jwt-token-here"

curl -X POST http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "ABC-123",
    "model": "Toyota Camry",
    "color": "Black",
    "type": "UBER_X"
  }'
```

### 4. Get All Vehicles
```bash
curl -X GET http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Update Vehicle
```bash
curl -X PUT http://localhost:8080/api/vehicles/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "color": "Silver",
    "type": "UBER_BLACK"
  }'
```

### 6. Set Current Vehicle
```bash
curl -X PUT http://localhost:8080/api/vehicles/1/set-current \
  -H "Authorization: Bearer $TOKEN"
```

### 7. Delete Vehicle
```bash
curl -X DELETE http://localhost:8080/api/vehicles/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Vehicle Types

The system supports three vehicle types:

- **UBER_X** - Standard rides
- **UBER_BLACK** - Premium rides
- **UBER_POOL** - Shared rides

---

## Business Rules

1. **License Plate Uniqueness**: Each license plate must be unique across all vehicles
2. **Automatic Uppercase**: License plates are automatically converted to uppercase
3. **Current Vehicle**: When a driver adds their first vehicle, it's automatically set as current
4. **Ownership Validation**: Drivers can only manage their own vehicles
5. **Current Vehicle Management**: Deleting the current vehicle automatically unsets it

---

## Database Schema

### Vehicles Table
```sql
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(15) UNIQUE NOT NULL,
    model VARCHAR(100) NOT NULL,
    color VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('UBER_X', 'UBER_BLACK', 'UBER_POOL')),
    driver_id BIGINT REFERENCES drivers(id)
);
```

---

## Summary

**Vehicle Management** allows drivers to:
1. Add multiple vehicles with validation
2. View all their vehicles
3. Update vehicle details
4. Delete vehicles
5. Set a current/active vehicle

All operations are secured with JWT authentication and role-based access control (DRIVER role required).
