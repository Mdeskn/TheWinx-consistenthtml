package com.winx.rating.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "ratings",
    uniqueConstraints = @UniqueConstraint(name = "uq_rating_booking", columnNames = "booking_id")
)
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ratingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Embedded
    private Review review;

    @Embedded
    private RatingTarget target;

    protected Rating() {}

    public static Rating create(Long userId, RatingTarget target, Review review) {
        Rating r = new Rating();
        r.userId     = Objects.requireNonNull(userId,  "userId required");
        r.target     = Objects.requireNonNull(target,  "target required");
        r.review     = Objects.requireNonNull(review,  "review required");
        r.createdAt  = LocalDateTime.now();
        return r;
    }

    public Long          getRatingId()  { return ratingId; }
    public Long          getUserId()    { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Review        getReview()    { return review; }
    public RatingTarget  getTarget()    { return target; }
}
