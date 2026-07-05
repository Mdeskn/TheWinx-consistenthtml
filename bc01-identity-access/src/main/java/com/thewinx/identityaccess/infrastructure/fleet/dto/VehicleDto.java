package com.thewinx.identityaccess.infrastructure.fleet.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleDto {

    /** BC-02 returns "vehicleId" in JSON; accept both names during deserialization. */
    @JsonAlias("vehicleId")
    private Long id;

    /** Provider who owns this vehicle. */
    private Long providerId;

    /** Vehicle type: E_SCOOTER, BICYCLE, E_BIKE, E_CAR */
    private String type;

    /** Human-readable description used as the vehicle display name. */
    private String description;

    /** AVAILABLE or BOOKED */
    private String status;

    /** Price charged per unit (hour or kilometer). */
    private BigDecimal pricePerUnit;

    /** PER_HOUR or PER_KILOMETER */
    private String billingModel;

    /** Maximum number of passengers. Shown as "seats" on the dashboard. */
    private Integer maxPersons;

    private Integer maxDurationMinutes;
    private Integer maxKilometers;
    private Integer minAge;

    private Double currentLatitude;
    private Double currentLongitude;

    // ── Getters & Setters ─────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public String getBillingModel() { return billingModel; }
    public void setBillingModel(String billingModel) { this.billingModel = billingModel; }

    public Integer getMaxPersons() { return maxPersons; }
    public void setMaxPersons(Integer maxPersons) { this.maxPersons = maxPersons; }

    public Integer getMaxDurationMinutes() { return maxDurationMinutes; }
    public void setMaxDurationMinutes(Integer maxDurationMinutes) { this.maxDurationMinutes = maxDurationMinutes; }

    public Integer getMaxKilometers() { return maxKilometers; }
    public void setMaxKilometers(Integer maxKilometers) { this.maxKilometers = maxKilometers; }

    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }

    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }

    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }

    // ── Convenience helpers for the dashboard ─────────────────────────────

    /** Returns true if the vehicle is available for booking. */
    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    /**
     * Returns a display-friendly name.
     * Falls back to "Vehicle #<id>" if description is blank.
     */
    public String getName() {
        return (description != null && !description.isBlank())
                ? description
                : "Vehicle #" + id;
    }

    /**
     * Returns a plate-like identifier using the type and id
     * (bc02 does not store a plate number field).
     */
    public String getPlate() {
        return (type != null ? type.substring(0, Math.min(type.length(), 3)) : "VEH")
                + "-" + String.format("%03d", id != null ? id : 0);
    }

    /** Returns the number of seats (falls back to 1 if not set). */
    public int getSeats() {
        return maxPersons != null ? maxPersons : 1;
    }

    /** Returns the price per unit as a plain double for JavaScript. */
    public double getPricePerDay() {
        return pricePerUnit != null ? pricePerUnit.doubleValue() : 0.0;
    }

    /**
     * Returns an emoji icon based on the vehicle type,
     * so the dashboard can display it without any image files.
     */
    public String getIcon() {
        if (type == null) return "🚗";
        return switch (type.toUpperCase()) {
            case "E_SCOOTER" -> "🛴";
            case "BICYCLE"   -> "🚲";
            case "E_BIKE"    -> "🏍️";
            case "E_CAR"     -> "🚗";
            default          -> "🚗";
        };
    }
}
