package org.example.proyecto_baseee.client;

import org.example.proyecto_baseee.dto.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthClient {
    @PostMapping("/api/auth/validate")
    AuthResponse validarToken(@RequestBody Map<String, String> request);
}
