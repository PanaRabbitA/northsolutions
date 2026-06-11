package org.example.proyecto_baseee.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "MS-SALES")
public interface SalesClient {
    @GetMapping("/api/sales/{id}")
    Map<String, Object> consultarDocumento(@PathVariable("id") Long id);
}
