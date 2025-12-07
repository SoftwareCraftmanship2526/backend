package com.uber.backend.rating.domain.model;

import com.uber.backend.rating.domain.enums.RatingSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    private Long id;

    @NotNull(message = "Rating stars are required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating must be at most 5 stars")
    private Integer stars;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;

    @NotNull(message = "Rating source is required")
    private RatingSource ratedBy;

    @NotNull(message = "Ride ID is required")
    private Long rideId;
}
