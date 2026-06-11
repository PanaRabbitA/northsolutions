package org.example.proyecto_baseee.client;

import org.example.proyecto_baseee.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MS-USER")
public interface UserClient {
    @GetMapping("/api/usuarios/username/{username}")
    UserResponse obtenerResponsable(@PathVariable("username") String username);
}
