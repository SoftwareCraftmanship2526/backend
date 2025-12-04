package com.uber.backend.infrastructure.repository;

import com.uber.backend.infrastructure.persistence.entity.DriverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, Long> {

    Optional<DriverEntity> findByEmail(String email);

    Optional<DriverEntity> findByLicenseNumber(String licenseNumber);

    List<DriverEntity> findByIsAvailable(Boolean isAvailable);
}
