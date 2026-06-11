package org.example.proyecto_baseee.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "ms-warehouse")
public interface WarehouseClient {
    @GetMapping("/api/warehouse/products")
    List<Map<String, Object>> getProducts();
}
