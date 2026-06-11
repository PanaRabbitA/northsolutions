package com.northsolutions.msauth.controller;

import com.northsolutions.msauth.client.UserClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserClient userClient;
    
    // Almacena los usuarios conectados en memoria: K=Token, V=Datos de sesión (Map)
    private final Map<String, Map<String, Object>> activeSessions = new java.util.concurrent.ConcurrentHashMap<>();

    public AuthController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        try {
            // Forward login request to ms-user via Feign
            Map<String, Object> user = userClient.login(credentials);
            if (user != null && user.containsKey("username")) {
                String username = user.get("username").toString();
                
                // Validate unique session
                boolean alreadyActive = activeSessions.values().stream()
                        .anyMatch(s -> s.get("username").equals(username));
                        
                if (alreadyActive) {
                    return ResponseEntity.status(403).body(Map.of("error", "Ya existe una sesión activa para este usuario en otro navegador/dispositivo."));
                }
                
                String role = user.get("role").toString();
                String token = "mock-jwt-token." + username + "." + role + "." + java.util.UUID.randomUUID().toString();
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("role", role);
                response.put("username", username);
                
                // Registrar sesión activa
                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("username", username);
                sessionData.put("role", role);
                sessionData.put("connectedAt", java.time.LocalDateTime.now().toString());
                activeSessions.put(token, sessionData);
                
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Map<String, Object>> verifyPassword(@RequestBody Map<String, String> credentials) {
        try {
            Map<String, Object> user = userClient.login(credentials);
            if (user != null && user.containsKey("username")) {
                return ResponseEntity.ok(Map.of("valid", true));
            }
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token != null && activeSessions.containsKey(token)) {
            String[] parts = token.split("\\.");
            if (parts.length >= 3) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", parts[1]);
                response.put("role", parts[2]);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Token inválido o sesión cerrada"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token != null) {
            activeSessions.remove(token);
        }
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Sesión cerrada correctamente"));
    }

    @GetMapping("/active-sessions")
    public ResponseEntity<java.util.Collection<Map<String, Object>>> getActiveSessions() {
        return ResponseEntity.ok(activeSessions.values());
    }

    @DeleteMapping("/active-sessions/{username}")
    public ResponseEntity<Map<String, String>> forceLogoutUser(@PathVariable String username) {
        String tokenToRemove = null;
        for (Map.Entry<String, Map<String, Object>> entry : activeSessions.entrySet()) {
            if (username.equals(entry.getValue().get("username"))) {
                tokenToRemove = entry.getKey();
                break;
            }
        }
        if (tokenToRemove != null) {
            activeSessions.remove(tokenToRemove);
            return ResponseEntity.ok(Map.of("status", "ok", "message", "Sesión cerrada."));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Sesión no encontrada."));
    }
}