package com.winx.payment.controller;

import com.winx.payment.dto.PaymentRequest;
import com.winx.payment.model.Payment;
import com.winx.payment.model.PaymentMethod;
import com.winx.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

//    @PostMapping
//    public Payment createPayment(
//            @RequestParam Long bookingId,
//            @RequestParam Long userId,
//            @RequestParam BigDecimal amount,
//            @RequestParam String currency,
//            @RequestParam PaymentMethod paymentMethod
//    ) {
//        return paymentService.createAndProcessPayment(
//                bookingId,
//                userId,
//                amount,
//                currency,
//                paymentMethod
//        );
//    }
    @PostMapping
    public Payment createPayment(@RequestBody PaymentRequest request) {
        return paymentService.createAndProcessPayment(
                request.getBookingId(),
                request.getUserId(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethod()
        );
    }
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{paymentId}")
    public Payment getPaymentById(@PathVariable Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    @GetMapping("/booking/{bookingId}")
    public Payment getPaymentByBookingId(@PathVariable Long bookingId) {
        return paymentService.getPaymentByBookingId(bookingId);
    }
}