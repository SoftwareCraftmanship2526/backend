package com.uber.backend.driver.infrastructure.repository;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, Long> {

}
