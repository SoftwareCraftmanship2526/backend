package com.uber.backend.rating.infrastructure.persistence;

import com.uber.backend.rating.domain.enums.RatingSource;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer stars;

    @Column(length = 1000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "rated_by", nullable = false)
    private RatingSource ratedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private RideEntity ride;
}
