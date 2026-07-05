package com.winx.booking.application.pricing;

import com.winx.booking.domain.vo.TimeInterval;
import com.winx.booking.domain.vo.VehicleSnapshot;

import java.math.BigDecimal;

public interface PricingStrategy {

    String billingModel();

    BigDecimal cost(VehicleSnapshot snapshot, TimeInterval interval, double distanceKm);
}
