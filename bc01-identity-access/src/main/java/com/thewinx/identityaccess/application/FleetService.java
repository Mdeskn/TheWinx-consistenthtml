package com.thewinx.identityaccess.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class FleetService {

    private final Map<Long, VehicleView> vehicles = new LinkedHashMap<>();
    private final Map<Long, BookingView> bookings = new LinkedHashMap<>();
    private final AtomicLong bookingIdSequence = new AtomicLong(1);

    public FleetService() {
        seedVehicles();
    }

    public synchronized List<VehicleView> listVehicles() {
        return vehicles.values().stream()
            .map(VehicleView::copy)
            .toList();
    }

    public synchronized List<BookingView> listBookings() {
        return bookings.values().stream()
            .sorted(Comparator.comparingLong(BookingView::getId).reversed())
            .map(BookingView::copy)
            .toList();
    }

    public synchronized List<BookingView> listBookingsByUsername(String username) {
        return bookings.values().stream()
            .filter(booking -> booking.getUsername().equalsIgnoreCase(username))
            .sorted(Comparator.comparingLong(BookingView::getId).reversed())
            .map(BookingView::copy)
            .toList();
    }

    public synchronized BookingView createBooking(Long vehicleId, String username, LocalDate pickupDate, LocalDate returnDate) {
        if (!returnDate.isAfter(pickupDate)) {
            throw new IllegalArgumentException("Return date must be after pick-up date");
        }
        if (hasActiveBookingForVehicle(vehicleId)) {
            throw new DuplicateResourceException("Vehicle is already booked");
        }

        long bookingId = bookingIdSequence.getAndIncrement();
        BookingView booking = new BookingView(
            bookingId,
            vehicleId,
            "Vehicle #" + vehicleId,  // name resolved by JS from the vehicles list
            "VEH-" + String.format("%03d", vehicleId),
            username,
            pickupDate,
            returnDate,
            "CONFIRMED"
        );
        bookings.put(bookingId, booking);
        return booking.copy();
    }

    public synchronized BookingView cancelBooking(Long bookingId) {
        BookingView booking = bookings.get(bookingId);
        if (booking == null) {
            throw new NotFoundException("Booking not found");
        }

        booking.setStatus("CANCELLED");

        VehicleView vehicle = vehicles.get(booking.getVehicleId());
        if (vehicle != null && !hasActiveBookingForVehicle(vehicle.getId())) {
            vehicle.setAvailable(true);
        }

        return booking.copy();
    }

    private boolean hasActiveBookingForVehicle(Long vehicleId) {
        return bookings.values().stream()
            .anyMatch(booking -> booking.getVehicleId().equals(vehicleId)
                && ("CONFIRMED".equals(booking.getStatus()) || "PENDING".equals(booking.getStatus())));
    }

    private void seedVehicles() {
        addVehicle(1L, "Toyota Camry", "TW-001", "Sedan", 5, 45, true, "CAR");
        addVehicle(2L, "Honda CR-V", "TW-002", "SUV", 5, 65, true, "SUV");
        addVehicle(3L, "Ford Transit", "TW-003", "Van", 9, 90, false, "VAN");
        addVehicle(4L, "Chevrolet Tahoe", "TW-004", "SUV", 7, 80, true, "SUV");
        addVehicle(5L, "Tesla Model 3", "TW-005", "Sedan", 5, 75, true, "CAR");
        addVehicle(6L, "Ford F-150", "TW-006", "Truck", 5, 100, false, "TRUCK");
        addVehicle(7L, "Kia Carnival", "TW-007", "Van", 8, 85, true, "VAN");
        addVehicle(8L, "BMW X5", "TW-008", "SUV", 5, 110, true, "SUV");
    }

    private void addVehicle(Long id, String name, String plate, String type, int seats, int pricePerDay, boolean available, String icon) {
        vehicles.put(id, new VehicleView(id, name, plate, type, seats, pricePerDay, available, icon));
    }

    public static class VehicleView {
        private final Long id;
        private final String name;
        private final String plate;
        private final String type;
        private final int seats;
        private final int pricePerDay;
        private boolean available;
        private final String icon;

        public VehicleView(Long id, String name, String plate, String type, int seats, int pricePerDay, boolean available, String icon) {
            this.id = id;
            this.name = name;
            this.plate = plate;
            this.type = type;
            this.seats = seats;
            this.pricePerDay = pricePerDay;
            this.available = available;
            this.icon = icon;
        }

        public VehicleView copy() {
            return new VehicleView(id, name, plate, type, seats, pricePerDay, available, icon);
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPlate() {
            return plate;
        }

        public String getType() {
            return type;
        }

        public int getSeats() {
            return seats;
        }

        public int getPricePerDay() {
            return pricePerDay;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public String getIcon() {
            return icon;
        }
    }

    public static class BookingView {
        private final Long id;
        private final Long vehicleId;
        private final String vehicleName;
        private final String plate;
        private final String username;
        private final LocalDate pickupDate;
        private final LocalDate returnDate;
        private String status;

        public BookingView(Long id,
                           Long vehicleId,
                           String vehicleName,
                           String plate,
                           String username,
                           LocalDate pickupDate,
                           LocalDate returnDate,
                           String status) {
            this.id = id;
            this.vehicleId = vehicleId;
            this.vehicleName = vehicleName;
            this.plate = plate;
            this.username = username;
            this.pickupDate = pickupDate;
            this.returnDate = returnDate;
            this.status = status;
        }

        public BookingView copy() {
            return new BookingView(id, vehicleId, vehicleName, plate, username, pickupDate, returnDate, status);
        }

        public Long getId() {
            return id;
        }

        public Long getVehicleId() {
            return vehicleId;
        }

        public String getVehicleName() {
            return vehicleName;
        }

        public String getPlate() {
            return plate;
        }

        public String getUsername() {
            return username;
        }

        public LocalDate getPickupDate() {
            return pickupDate;
        }

        public LocalDate getReturnDate() {
            return returnDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
