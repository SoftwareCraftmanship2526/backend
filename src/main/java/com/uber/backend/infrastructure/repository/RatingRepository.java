package com.uber.backend.infrastructure.repository;

import com.uber.backend.infrastructure.persistence.entity.RatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<RatingEntity, Long> {

    List<RatingEntity> findByRideId(Long rideId);
}
