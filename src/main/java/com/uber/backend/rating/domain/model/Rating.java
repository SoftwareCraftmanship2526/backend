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
    private Long rideId;  // Reference by ID

    public boolean hasValidStars() {
        return stars != null && stars >= 1 && stars <= 5;
    }

    public boolean isPositive() {
        return stars != null && stars >= 4;
    }

    public boolean isNegative() {
        return stars != null && stars <= 2;
    }

    public boolean hasComment() {
        return comment != null && !comment.isBlank();
    }

    public boolean isValid() {
        return hasValidStars() && ratedBy != null && rideId != null;
    }

    public String getDescription() {
        StringBuilder desc = new StringBuilder(stars + " stars");
        if (hasComment()) {
            desc.append(": ").append(comment);
        }
        return desc.toString();
    }
}
