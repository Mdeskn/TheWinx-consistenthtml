package com.winx.fleet.dto;

import com.winx.fleet.model.BillingModel;
import com.winx.fleet.model.VehicleType;

import java.math.BigDecimal;

public class CreateVehicleRequest {

    public Long providerId;
    public String name;
    public VehicleType type;
    public String description;
    public BigDecimal pricePerUnit;
    public BillingModel billingModel;

    public Integer maxDurationMinutes;
    public Integer maxKilometers;
    public Integer minAge;
    public Integer maxPersons;

    public Double currentLatitude;
    public Double currentLongitude;
}