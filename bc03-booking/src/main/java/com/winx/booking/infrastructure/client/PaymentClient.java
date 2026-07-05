package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.AuthorizeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bc04-payment", url = "${payment.service.url:http://localhost:8084}")
public interface PaymentClient {

    @PostMapping("/api/payments")
    void authorize(@RequestBody AuthorizeRequest request);
}
