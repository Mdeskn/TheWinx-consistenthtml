package com.winx.rating.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class RatingTarget {

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private Long bookingId;

    protected RatingTarget() {}

    public RatingTarget(Long vehicleId, Long providerId, Long bookingId) {
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId required");
        this.providerId = Objects.requireNonNull(providerId, "providerId required");
        this.bookingId = Objects.requireNonNull(bookingId, "bookingId required");
    }

    public Long getVehicleId()  { return vehicleId; }
    public Long getProviderId() { return providerId; }
    public Long getBookingId()  { return bookingId; }
}
