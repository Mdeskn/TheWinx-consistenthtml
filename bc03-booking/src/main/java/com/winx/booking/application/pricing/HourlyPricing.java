package com.winx.booking.application.pricing;

import com.winx.booking.domain.vo.TimeInterval;
import com.winx.booking.domain.vo.VehicleSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Component
public class HourlyPricing implements PricingStrategy {

    @Override
    public String billingModel() {
        return "PER_HOUR";
    }

    @Override
    public BigDecimal cost(VehicleSnapshot snapshot, TimeInterval interval, double distanceKm) {
        long minutes = Duration.between(interval.getStartTime(), interval.getEndTime()).toMinutes();
        long hours = Math.max(1, (long) Math.ceil(minutes / 60.0));
        return snapshot.getPricePerUnit()
                .multiply(BigDecimal.valueOf(hours))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
