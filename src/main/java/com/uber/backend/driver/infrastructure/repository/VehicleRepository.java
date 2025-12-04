package com.uber.backend.driver.infrastructure.repository;

import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

}
