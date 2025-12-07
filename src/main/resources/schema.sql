-- ===================================================================
-- UBER RIDE-SHARING APPLICATION - DATABASE SCHEMA
-- Spring Boot 3 + PostgreSQL
-- Domain-Driven Design with JOINED inheritance strategy
-- ===================================================================

-- Drop tables if they exist (in correct order to respect foreign keys)
DROP TABLE IF EXISTS ratings CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS rides CASCADE;
DROP TABLE IF EXISTS passenger_saved_addresses CASCADE;
DROP TABLE IF EXISTS vehicles CASCADE;
DROP TABLE IF EXISTS drivers CASCADE;
DROP TABLE IF EXISTS passengers CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;

-- ===================================================================
-- ACCOUNT TABLES (Inheritance: JOINED strategy)
-- ===================================================================

-- Base account table (abstract)
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PASSENGER', 'DRIVER', 'ADMIN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_accounts_email ON accounts(email);
CREATE INDEX idx_accounts_role ON accounts(role);

-- Passenger table (extends Account)
CREATE TABLE passengers (
    id BIGINT PRIMARY KEY,
    passenger_rating DOUBLE PRECISION,
    CONSTRAINT fk_passengers_account FOREIGN KEY (id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Driver table (extends Account)
CREATE TABLE drivers (
    id BIGINT PRIMARY KEY,
    driver_rating DOUBLE PRECISION,
    is_available BOOLEAN DEFAULT TRUE,
    license_number VARCHAR(255) NOT NULL UNIQUE,
    current_latitude DOUBLE PRECISION,
    current_longitude DOUBLE PRECISION,
    current_address VARCHAR(500),
    CONSTRAINT fk_drivers_account FOREIGN KEY (id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE INDEX idx_drivers_license ON drivers(license_number);
CREATE INDEX idx_drivers_availability ON drivers(is_available);

-- Passenger saved addresses (ElementCollection)
CREATE TABLE passenger_saved_addresses (
    passenger_id BIGINT NOT NULL,
    address VARCHAR(500),
    CONSTRAINT fk_saved_addresses_passenger FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE CASCADE
);

-- ===================================================================
-- VEHICLE TABLE
-- ===================================================================

CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(255) NOT NULL UNIQUE,
    model VARCHAR(255) NOT NULL,
    color VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('UBER_X', 'UBER_BLACK', 'UBER_POOL')),
    driver_id BIGINT,
    CONSTRAINT fk_vehicles_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL
);

CREATE INDEX idx_vehicles_license_plate ON vehicles(license_plate);
CREATE INDEX idx_vehicles_driver ON vehicles(driver_id);
CREATE INDEX idx_vehicles_type ON vehicles(type);

-- ===================================================================
-- RIDE TABLE (Aggregate Root)
-- ===================================================================

CREATE TABLE rides (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL CHECK (status IN ('REQUESTED', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    fare_amount DECIMAL(10, 2),

    -- Pickup location (embedded)
    pickup_latitude DOUBLE PRECISION,
    pickup_longitude DOUBLE PRECISION,
    pickup_address VARCHAR(500),

    -- Dropoff location (embedded)
    dropoff_latitude DOUBLE PRECISION,
    dropoff_longitude DOUBLE PRECISION,
    dropoff_address VARCHAR(500),

    -- Foreign keys
    passenger_id BIGINT NOT NULL,
    driver_id BIGINT,
    vehicle_id BIGINT,

    CONSTRAINT fk_rides_passenger FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE CASCADE,
    CONSTRAINT fk_rides_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL,
    CONSTRAINT fk_rides_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL
);

CREATE INDEX idx_rides_passenger ON rides(passenger_id);
CREATE INDEX idx_rides_driver ON rides(driver_id);
CREATE INDEX idx_rides_vehicle ON rides(vehicle_id);
CREATE INDEX idx_rides_status ON rides(status);
CREATE INDEX idx_rides_requested_at ON rides(requested_at);

-- ===================================================================
-- PAYMENT TABLE
-- ===================================================================

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(10, 2) NOT NULL,
    method VARCHAR(50) NOT NULL CHECK (method IN ('CREDIT_CARD', 'CASH', 'WALLET', 'APPLE_PAY')),
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    transaction_id VARCHAR(255),
    ride_id BIGINT NOT NULL UNIQUE,

    CONSTRAINT fk_payments_ride FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_ride ON payments(ride_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_transaction ON payments(transaction_id);

-- ===================================================================
-- RATING TABLE
-- ===================================================================

CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    stars INTEGER NOT NULL CHECK (stars >= 1 AND stars <= 5),
    comment VARCHAR(1000),
    rated_by VARCHAR(50) NOT NULL CHECK (rated_by IN ('PASSENGER', 'DRIVER')),
    ride_id BIGINT NOT NULL,

    CONSTRAINT fk_ratings_ride FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE
);

CREATE INDEX idx_ratings_ride ON ratings(ride_id);
CREATE INDEX idx_ratings_source ON ratings(rated_by);

-- ===================================================================
-- END OF SCHEMA
-- ===================================================================
