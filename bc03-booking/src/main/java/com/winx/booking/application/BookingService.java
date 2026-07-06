package com.winx.booking.application;

import com.winx.booking.api.dto.BookingCreateRequest;
import com.winx.booking.api.dto.EndRideRequest;
import com.winx.booking.api.dto.PrincipalDto;
import com.winx.booking.api.dto.VehicleDto;
import com.winx.booking.application.pricing.CostCalculationService;
import com.winx.booking.domain.Booking;
import com.winx.booking.domain.BookingStatus;
import com.winx.booking.domain.event.BookingCompleted;
import com.winx.booking.domain.vo.RideLocation;
import com.winx.booking.domain.vo.RideSummary;
import com.winx.booking.domain.vo.TimeInterval;
import com.winx.booking.domain.vo.VehicleSnapshot;
import com.winx.booking.exception.ActiveBookingExistsException;
import com.winx.booking.exception.BookingNotFoundException;
import com.winx.booking.exception.VehicleNotAvailableException;
import com.winx.booking.infrastructure.client.FleetGateway;
import com.winx.booking.infrastructure.client.IdentityGateway;
import com.winx.booking.infrastructure.persistence.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository repository;
    private final IdentityGateway identityGateway;
    private final FleetGateway fleetGateway;
    private final CostCalculationService costCalculationService;
    private final RestrictionValidator restrictionValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Booking createBooking(String authToken, BookingCreateRequest request) {
        PrincipalDto principal = identityGateway.validate(authToken);

        VehicleDto vehicle = fleetGateway.findVehicle(request.vehicleId());
        if (vehicle == null || !"AVAILABLE".equalsIgnoreCase(vehicle.status())) {
            throw new VehicleNotAvailableException(request.vehicleId());
        }

        restrictionValidator.validateAge(principal, vehicle);

        repository.findFirstByUserIdAndStatus(principal.id(), BookingStatus.ACTIVE)
                .ifPresent(existing -> { throw new ActiveBookingExistsException(principal.id()); });

        VehicleSnapshot snapshot = new VehicleSnapshot(
                vehicle.vehicleId(), vehicle.providerId(), vehicle.type(), vehicle.pricePerUnit(), vehicle.billingModel());
        RideLocation start = new RideLocation(request.startLatitude(), request.startLongitude());

        Booking booking = repository.save(Booking.start(principal.id(), snapshot, start, request.paymentMethod()));
        fleetGateway.markBooked(vehicle.vehicleId());
        return booking;
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        booking.cancel();
        fleetGateway.markAvailable(booking.getVehicleSnapshot().getVehicleId());
        try {
            paymentGateway.cancelPayment(bookingId);
        } catch (Exception ignored) {
            // No payment may exist yet (booking cancelled before completion); ignore silently
        }
        return booking;
    }

    @Transactional
    public Booking endBooking(Long bookingId, EndRideRequest request) {
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        LocalDateTime endTime = LocalDateTime.now();
        TimeInterval endedInterval = booking.getInterval().endingAt(endTime);
        BigDecimal totalCost = costCalculationService.computeCost(
                booking.getVehicleSnapshot(), endedInterval, request.distanceKm());

        RideLocation endLocation = new RideLocation(request.endLatitude(), request.endLongitude());
        booking.complete(endLocation, new RideSummary(request.distanceKm(), totalCost), endTime);

        fleetGateway.markAvailable(booking.getVehicleSnapshot().getVehicleId());
        eventPublisher.publishEvent(new BookingCompleted(
                booking.getBookingId(), booking.getUserId(), totalCost, "EUR", booking.getPaymentMethod()));
        return booking;
    }

    @Transactional(readOnly = true)
    public Booking findById(Long bookingId) {
        return repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    @Transactional(readOnly = true)
    public List<Booking> findByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> searchVehicles(double lat, double lon, double radiusKm,
                                           String type, BigDecimal maxPrice, Integer minPersons) {
        return fleetGateway.search(lat, lon, radiusKm, type, maxPrice, minPersons);
    }
}
