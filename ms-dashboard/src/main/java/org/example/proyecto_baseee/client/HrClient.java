package org.example.proyecto_baseee.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "ms-hr")
public interface HrClient {
    @GetMapping("/api/hr/employees")
    List<Map<String, Object>> getEmployees();
}
