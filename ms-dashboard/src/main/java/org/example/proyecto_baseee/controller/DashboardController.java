package org.example.proyecto_baseee.controller;

import org.example.proyecto_baseee.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/kpi")
    public ResponseEntity<Map<String, Object>> getKpis() {
        return ResponseEntity.ok(dashboardService.getKpis());
    }
}
