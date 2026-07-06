package com.winx.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long bookingId;

    private Long userId;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    private String failureReason;

    private LocalDateTime createdAt;

    public Payment() {
    }

    public Payment(Long bookingId, Long userId, BigDecimal amount, String currency, PaymentMethod paymentMethod) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsPaid() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be marked as paid.");
        }

        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.failureReason = null;
    }

    public void markAsFailed(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be marked as failed.");
        }

        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void markAsRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}