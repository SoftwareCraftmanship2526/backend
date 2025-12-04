package com.uber.backend.infrastructure.repository;

import com.uber.backend.infrastructure.persistence.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<PassengerEntity, Long> {

    Optional<PassengerEntity> findByEmail(String email);
}
