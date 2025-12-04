package com.uber.backend.rating.domain.model;

import com.uber.backend.rating.domain.enums.RatingSource;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    private Long id;
    private Integer stars;
    private String comment;
    private RatingSource ratedBy;
    private Long rideId;
}
