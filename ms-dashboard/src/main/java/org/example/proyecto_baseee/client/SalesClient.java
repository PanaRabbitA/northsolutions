package org.example.proyecto_baseee.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "ms-sales")
public interface SalesClient {
    @GetMapping("/api/sales")
    List<Map<String, Object>> getSales();
}
