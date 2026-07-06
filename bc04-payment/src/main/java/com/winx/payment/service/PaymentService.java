package com.winx.payment.service;

import com.winx.payment.model.Payment;
import com.winx.payment.model.PaymentMethod;
import com.winx.payment.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Random random = new Random();

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "createPaymentFallback")
    public Payment createAndProcessPayment(
            Long bookingId,
            Long userId,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod
    ) {
        Payment payment = new Payment(bookingId, userId, amount, currency, paymentMethod);

        paymentRepository.save(payment);

        boolean paymentSuccessful = random.nextBoolean();

        if (paymentSuccessful) {
            payment.markAsPaid();
        } else {
            payment.markAsFailed("Payment was declined by the provider.");
        }

        return paymentRepository.save(payment);
    }

    public Payment createPaymentFallback(
            Long bookingId,
            Long userId,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            Throwable t
    ) {
        Payment payment = new Payment(bookingId, userId, amount, currency, paymentMethod);
        payment.markAsFailed("Payment gateway unavailable. Please try again later.");
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found for booking"));
    }

    public Payment cancelPayment(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("No payment found for booking " + bookingId));
        payment.markAsRefunded();
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findAll().stream()
                .filter(p -> userId.equals(p.getUserId()))
                .toList();
    }
}