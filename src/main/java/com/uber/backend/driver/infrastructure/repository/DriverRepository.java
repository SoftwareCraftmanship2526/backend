package com.uber.backend.driver.infrastructure.repository;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, Long> {

    Optional<DriverEntity> findByEmail(String email);
    List<DriverEntity> findByIsAvailableTrue();

    Optional<DriverEntity> findByLicenseNumber(String licenseNumber);
}
