package com.winx.booking.application.pricing;

import com.winx.booking.domain.vo.TimeInterval;
import com.winx.booking.domain.vo.VehicleSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PerKilometerPricing implements PricingStrategy {

    @Override
    public String billingModel() {
        return "PER_KILOMETER";
    }

    @Override
    public BigDecimal cost(VehicleSnapshot snapshot, TimeInterval interval, double distanceKm) {
        return snapshot.getPricePerUnit()
                .multiply(BigDecimal.valueOf(distanceKm))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
