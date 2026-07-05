package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.PrincipalDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "bc01-identity-access", url = "${identity.service.url:http://localhost:8081}", path = "/auth")
public interface IdentityClient {

    @GetMapping("/validate")
    PrincipalDto validate(@RequestHeader("X-Auth-Token") String token);
}
