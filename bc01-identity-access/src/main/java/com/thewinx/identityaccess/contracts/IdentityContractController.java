package com.thewinx.identityaccess.contracts;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contracts/v1")
public class IdentityContractController {

    private final AdjacentContextMockService adjacentContextMockService;

    public IdentityContractController(AdjacentContextMockService adjacentContextMockService) {
        this.adjacentContextMockService = adjacentContextMockService;
    }

    @GetMapping("/booking/identity-check")
    public BookingIdentityCheckResponse bookingIdentityCheck(@RequestParam String username) {
        return adjacentContextMockService.verifyIdentityForBooking(username);
    }

    @GetMapping("/payment/eligibility")
    public PaymentEligibilityResponse paymentEligibility(@RequestParam Long userId) {
        return adjacentContextMockService.checkPaymentEligibility(userId);
    }

    @GetMapping("/provider/access")
    public ProviderAccessResponse providerAccess(@RequestParam Long userId) {
        return adjacentContextMockService.checkProviderPortalAccess(userId);
    }
}
