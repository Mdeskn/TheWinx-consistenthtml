package com.winx.booking.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VehicleSnapshot {

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "vehicle_type")
    private String type;

    @Column(name = "price_per_unit")
    private BigDecimal pricePerUnit;

    @Column(name = "billing_model")
    private String billingModel;
}
