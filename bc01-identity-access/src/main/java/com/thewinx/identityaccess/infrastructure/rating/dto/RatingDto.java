package com.thewinx.identityaccess.infrastructure.rating.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingDto {
    private Long ratingId;
    private Long userId;
    private Long bookingId;
    private Long vehicleId;
    private Long providerId;
    private int vehicleScore;
    private int providerScore;
    private String comment;
    private LocalDateTime createdAt;

    public Long getRatingId()     { return ratingId; }
    public void setRatingId(Long ratingId) { this.ratingId = ratingId; }
    public Long getUserId()       { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBookingId()    { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getVehicleId()    { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public Long getProviderId()   { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public int getVehicleScore()  { return vehicleScore; }
    public void setVehicleScore(int vehicleScore) { this.vehicleScore = vehicleScore; }
    public int getProviderScore() { return providerScore; }
    public void setProviderScore(int providerScore) { this.providerScore = providerScore; }
    public String getComment()    { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
