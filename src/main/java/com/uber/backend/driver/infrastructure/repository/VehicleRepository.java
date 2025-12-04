package com.uber.backend.driver.infrastructure.repository;

import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

    Optional<VehicleEntity> findByLicensePlate(String licensePlate);
}
