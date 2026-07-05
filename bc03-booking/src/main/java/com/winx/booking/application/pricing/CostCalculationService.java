package com.winx.booking.application.pricing;

import com.winx.booking.domain.vo.TimeInterval;
import com.winx.booking.domain.vo.VehicleSnapshot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CostCalculationService {

    private final Map<String, PricingStrategy> strategiesByModel;

    public CostCalculationService(List<PricingStrategy> strategies) {
        this.strategiesByModel = strategies.stream()
                .collect(Collectors.toMap(PricingStrategy::billingModel, Function.identity()));
    }

    public BigDecimal computeCost(VehicleSnapshot snapshot, TimeInterval interval, double distanceKm) {
        PricingStrategy strategy = strategiesByModel.get(snapshot.getBillingModel());
        if (strategy == null) {
            throw new IllegalStateException("No pricing strategy for billing model: " + snapshot.getBillingModel());
        }
        return strategy.cost(snapshot, interval, distanceKm);
    }
}
