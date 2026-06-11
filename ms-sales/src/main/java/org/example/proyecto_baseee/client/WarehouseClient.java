package org.example.proyecto_baseee.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ms-warehouse", path = "/api/warehouse")
public interface WarehouseClient {

    @GetMapping("/products/{id}")
    ResponseEntity<Map> getProductById(@PathVariable("id") Long id);

    @PostMapping("/inventory/out")
    ResponseEntity<Map> deductInventory(@RequestBody Map<String, Object> payload);
}
