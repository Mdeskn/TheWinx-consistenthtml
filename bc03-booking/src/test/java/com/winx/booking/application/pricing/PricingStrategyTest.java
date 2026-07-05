package com.winx.booking.application.pricing;

import com.winx.booking.domain.vo.TimeInterval;
import com.winx.booking.domain.vo.VehicleSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingStrategyTest {

    private final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);

    private VehicleSnapshot snapshot(String price, String billingModel) {
        return new VehicleSnapshot(1L, null, "E_SCOOTER", new BigDecimal(price), billingModel);
    }

    @Test
    void hourlyRoundsUpToNextStartedHour() {
        var interval = new TimeInterval(start, start.plusMinutes(61)); // 61 min -> 2 hours
        var cost = new HourlyPricing().cost(snapshot("1.50", "PER_HOUR"), interval, 0);
        assertEquals(new BigDecimal("3.00"), cost);
    }

    @Test
    void hourlyChargesAtLeastOneHour() {
        var interval = new TimeInterval(start, start.plusMinutes(5)); // 5 min -> 1 hour
        var cost = new HourlyPricing().cost(snapshot("1.50", "PER_HOUR"), interval, 0);
        assertEquals(new BigDecimal("1.50"), cost);
    }

    @Test
    void perKilometerMultipliesDistance() {
        var interval = new TimeInterval(start, start.plusMinutes(30));
        var cost = new PerKilometerPricing().cost(snapshot("0.50", "PER_KILOMETER"), interval, 4.0); // 2.00
        assertEquals(new BigDecimal("2.00"), cost);
    }
}
