package com.winx.booking.domain;

import com.winx.booking.domain.vo.RideLocation;
import com.winx.booking.domain.vo.RideSummary;
import com.winx.booking.domain.vo.TimeInterval;
import com.winx.booking.domain.vo.VehicleSnapshot;
import com.winx.booking.exception.InvalidBookingStateException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false)
    private Long userId;

    @Embedded
    private VehicleSnapshot vehicleSnapshot;

    @Embedded
    private TimeInterval interval;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_longitude"))
    })
    private RideLocation startLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "end_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "end_longitude"))
    })
    private RideLocation endLocation;

    @Embedded
    private RideSummary summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "payment_method")
    private String paymentMethod;

    private Booking(Long userId, VehicleSnapshot vehicleSnapshot, RideLocation startLocation, String paymentMethod) {
        this.userId = userId;
        this.vehicleSnapshot = vehicleSnapshot;
        this.startLocation = startLocation;
        this.interval = TimeInterval.startingNow();
        this.status = BookingStatus.ACTIVE;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CARD";
    }

    public static Booking start(Long userId, VehicleSnapshot vehicleSnapshot, RideLocation startLocation, String paymentMethod) {
        return new Booking(userId, vehicleSnapshot, startLocation, paymentMethod);
    }

    public void cancel() {
        if (status != BookingStatus.ACTIVE) {
            throw new InvalidBookingStateException(
                    "Only an ACTIVE booking can be cancelled (current status: " + status + ").");
        }
        this.status = BookingStatus.CANCELLED;
    }

    public void complete(RideLocation endLocation, RideSummary summary, LocalDateTime endTime) {
        if (status != BookingStatus.ACTIVE) {
            throw new InvalidBookingStateException(
                    "Only an ACTIVE booking can be completed (current status: " + status + ").");
        }
        this.endLocation = endLocation;
        this.summary = summary;
        this.interval = this.interval.endingAt(endTime);
        this.status = BookingStatus.COMPLETED;
    }
}
