package com.thewinx.identityaccess.contracts;

import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * Stub service that models downstream contract calls to adjacent bounded contexts.
 * Each method is guarded by a Resilience4j circuit breaker so that if the real
 * downstream service were to become unavailable the fallback is invoked instead
 * of propagating failures to callers.
 */
@Service
public class AdjacentContextMockService {

    // BC-03 Booking

    @CircuitBreaker(name = "bc03-booking", fallbackMethod = "verifyIdentityForBookingFallback")
    public BookingIdentityCheckResponse verifyIdentityForBooking(String username) {
        boolean knownUser = username != null && (username.startsWith("demo") || username.contains("user"));
        return new BookingIdentityCheckResponse(knownUser, knownUser ? "Identity verified" : "Identity not found");
    }

    private BookingIdentityCheckResponse verifyIdentityForBookingFallback(String username, Throwable t) {
        return new BookingIdentityCheckResponse(false, "Booking service unavailable – circuit open");
    }

    //  BC-04 Payment

    @CircuitBreaker(name = "bc04-payment", fallbackMethod = "checkPaymentEligibilityFallback")
    public PaymentEligibilityResponse checkPaymentEligibility(Long userId) {
        boolean eligible = userId != null && userId > 0 && userId % 2 != 0;
        return new PaymentEligibilityResponse(userId, eligible, eligible ? "ELIGIBLE" : "REVIEW_REQUIRED");
    }

    private PaymentEligibilityResponse checkPaymentEligibilityFallback(Long userId, Throwable t) {
        return new PaymentEligibilityResponse(userId, false, "Payment service unavailable – circuit open");
    }

    // BC-02 Fleet / Provider

    @CircuitBreaker(name = "bc02-fleet", fallbackMethod = "checkProviderPortalAccessFallback")
    public ProviderAccessResponse checkProviderPortalAccess(Long userId) {
        boolean canAccess = userId != null && userId % 2 == 0;
        return new ProviderAccessResponse(userId, canAccess, canAccess ? "PROVIDER_ACCESS_GRANTED" : "PROVIDER_ACCESS_DENIED");
    }

    private ProviderAccessResponse checkProviderPortalAccessFallback(Long userId, Throwable t) {
        return new ProviderAccessResponse(userId, false, "Fleet service unavailable – circuit open");
    }
}
