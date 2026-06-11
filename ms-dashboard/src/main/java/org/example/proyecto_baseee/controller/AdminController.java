package org.example.proyecto_baseee.controller;

import org.example.proyecto_baseee.service.ServiceManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final ServiceManager serviceManager;

    public AdminController(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @PostMapping("/{serviceName}/stop")
    public ResponseEntity<Map<String, String>> stopService(@PathVariable String serviceName) {
        String result = serviceManager.stopService(serviceName);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        response.put("status", serviceManager.isServiceRunning(serviceName) ? "RUNNING" : "STOPPED");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{serviceName}/start")
    public ResponseEntity<Map<String, String>> startService(@PathVariable String serviceName) {
        String result = serviceManager.startService(serviceName);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        // It takes a bit to start, so we just return STARTING
        response.put("status", "STARTING");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{serviceName}/status")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable String serviceName) {
        Map<String, String> response = new HashMap<>();
        response.put("status", serviceManager.isServiceRunning(serviceName) ? "RUNNING" : "STOPPED");
        return ResponseEntity.ok(response);
    }
}
