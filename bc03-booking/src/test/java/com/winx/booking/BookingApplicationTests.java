package com.winx.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: the application context loads. Runs under the {@code mock} profile with Eureka
 * disabled so it needs no other running services.
 */
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@ActiveProfiles("mock")
class BookingApplicationTests {

    @Test
    void contextLoads() {
    }
}
