package com.northsolutions.msauth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ms-user")
public interface UserClient {
    
    @PostMapping("/api/usuarios/login")
    Map<String, Object> login(@RequestBody Map<String, String> credentials);
}
