package com.uber.backend.ride.infrastructure.repository;

import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<RideEntity, Long> {
    List<RideEntity> findByStatusEquals(RideStatus status);

    @Query("SELECT r FROM RideEntity r WHERE r.status = :status AND r.invitedAt < :cutoffTime")
    List<RideEntity> findTimedOutInvitations(@Param("status") RideStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT r FROM RideEntity r WHERE r.passenger.id = :passengerId AND r.status IN :statuses ORDER BY r.requestedAt DESC")
    List<RideEntity> findByPassengerIdAndStatusIn(@Param("passengerId") Long passengerId, @Param("statuses") List<RideStatus> statuses);
}
