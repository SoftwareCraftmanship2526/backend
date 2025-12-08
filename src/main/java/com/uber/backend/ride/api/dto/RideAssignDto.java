package com.uber.backend.ride.api.dto;

import com.uber.backend.driver.domain.model.Driver;
import com.uber.backend.driver.domain.model.Vehicle;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class RideAssignDto extends RideResponseDto{
    private Driver driver;
    private Vehicle vehicle;
    private LocalDateTime startTime;
}
