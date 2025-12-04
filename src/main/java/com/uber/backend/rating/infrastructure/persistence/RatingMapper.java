package com.uber.backend.rating.infrastructure.persistence;

import com.uber.backend.rating.domain.model.Rating;
import com.uber.backend.rating.infrastructure.persistence.RatingEntity;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public Rating toDomain(RatingEntity entity) {
        if (entity == null) {
            return null;
        }

        return Rating.builder()
                .id(entity.getId())
                .stars(entity.getStars())
                .comment(entity.getComment())
                .ratedBy(entity.getRatedBy())
                .rideId(entity.getRide() != null ? entity.getRide().getId() : null)
                .build();
    }

    public RatingEntity toEntity(Rating domain) {
        if (domain == null) {
            return null;
        }

        RatingEntity entity = new RatingEntity();
        entity.setId(domain.getId());
        entity.setStars(domain.getStars());
        entity.setComment(domain.getComment());
        entity.setRatedBy(domain.getRatedBy());
        return entity;
    }

    public void updateEntity(Rating domain, RatingEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setStars(domain.getStars());
        entity.setComment(domain.getComment());
        entity.setRatedBy(domain.getRatedBy());
    }
}
