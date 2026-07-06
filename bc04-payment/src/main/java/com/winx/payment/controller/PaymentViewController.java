package com.winx.payment.controller;

import com.winx.payment.model.PaymentMethod;
import com.winx.payment.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/payments")
public class PaymentViewController {

    private final PaymentService paymentService;

    public PaymentViewController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String showPayments(@RequestParam(required = false) Long userId, Model model) {
        model.addAttribute("payments",
                userId != null ? paymentService.getPaymentsByUserId(userId) : paymentService.getAllPayments());
        model.addAttribute("userId", userId);
        return "payments";
    }

    @GetMapping("/new")
    public String showCreatePaymentForm(Model model) {
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "create-payment";
    }

    @PostMapping
    public String createPayment(
            @RequestParam Long bookingId,
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam PaymentMethod paymentMethod
    ) {
        paymentService.createAndProcessPayment(
                bookingId,
                userId,
                amount,
                currency,
                paymentMethod
        );

        return "redirect:/payments";
    }
}