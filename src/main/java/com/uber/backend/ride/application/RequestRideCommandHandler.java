package com.uber.backend.ride.application;

import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import com.uber.backend.ride.application.command.RequestRideCommand;
import com.uber.backend.ride.application.command.RideRequestResult;
import com.uber.backend.ride.application.query.RideQueryService;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.domain.model.Ride;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.persistence.RideMapper;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestRideCommandHandler {

    private final RideRepository rideRepository;
    private final PassengerRepository passengerRepository;
    private final RideQueryService rideQueryService;
    private final RideMapper rideMapper;

    public RideRequestResult handle(RequestRideCommand command) {

        BigDecimal fare = rideQueryService.calculateFare(command.type(), command.start(), command.end(), command.durationMin(), command.demandMultiplier()) ;
        Ride ride = Ride.builder().status(RideStatus.REQUESTED).requestedAt(LocalDateTime.now()).fareAmount(fare).pickupLocation(command.start()).dropoffLocation(command.end()).passengerId(command.passengerId()).build();
        RideEntity rideEntity = rideMapper.toEntity(ride);
        PassengerEntity passengerEntity = passengerRepository.findById(command.passengerId()).orElse(null);
        rideEntity.setPassenger(passengerEntity);

        rideRepository.save(rideEntity);

        Long rideId = rideEntity.getId();
        RideStatus status = rideEntity.getStatus();
        LocalDateTime requestedAt = rideEntity.getRequestedAt();

        return RideRequestResult.builder().rideId(rideId).passengerId(command.passengerId()).status(status).requestedAt(requestedAt).price(fare).build();


    }
}
