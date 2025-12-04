package com.uber.backend.ride.infrastructure.repository;

import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<RideEntity, Long> {

}
