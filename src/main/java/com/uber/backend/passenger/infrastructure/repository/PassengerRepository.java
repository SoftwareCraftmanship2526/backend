package com.uber.backend.passenger.infrastructure.repository;

import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<PassengerEntity, Long> {

    Optional<PassengerEntity> findByEmail(String email);
}
