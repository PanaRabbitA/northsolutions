package org.example.proyecto_baseee.controller;

import org.example.proyecto_baseee.model.Attendance;
import org.example.proyecto_baseee.model.Employee;
import org.example.proyecto_baseee.service.HrService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hr")
@CrossOrigin(origins = "*")
public class HrController {

    private final HrService hrService;

    public HrController(HrService hrService) {
        this.hrService = hrService;
    }

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(hrService.getAllEmployees());
    }

    @PostMapping("/employees")
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee employee) {
        return ResponseEntity.ok(hrService.addEmployee(employee));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return ResponseEntity.ok(hrService.updateEmployee(id, employee));
    }

    @PutMapping("/employees/{id}/status")
    public ResponseEntity<Employee> changeEmployeeStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(hrService.changeEmployeeStatus(id, status));
    }

    @PostMapping("/attendance")
    public ResponseEntity<Attendance> registerAttendance(@RequestBody Map<String, Object> payload) {
        Long employeeId = Long.valueOf(payload.get("employeeId").toString());
        String status = payload.get("status").toString();
        String registeredBy = payload.getOrDefault("registeredBy", "System").toString();
        return ResponseEntity.ok(hrService.registerAttendance(employeeId, status, registeredBy));
    }

    @GetMapping("/employees/{id}/attendance")
    public ResponseEntity<List<Attendance>> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(hrService.getEmployeeAttendance(id));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        return ResponseEntity.ok(hrService.getAllAttendance());
    }

    @PostMapping("/employees/{id}/documents")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        try {
            hrService.saveDocument(id, type, file.getOriginalFilename(), file.getContentType(), file.getBytes());
            return ResponseEntity.ok(Map.of("status", "success", "message", "Document saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/employees/{id}/documents")
    public ResponseEntity<List<Map<String, String>>> getEmployeeDocuments(@PathVariable Long id) {
        List<Map<String, String>> docs = hrService.getEmployeeDocuments(id).stream()
            .map(d -> Map.of(
                "type", d.getDocumentType(),
                "fileName", d.getFileName(),
                "contentType", d.getContentType()
            )).collect(Collectors.toList());
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/employees/{id}/documents/{type}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long id, @PathVariable String type) {
        try {
            org.example.proyecto_baseee.model.EmployeeDocument doc = hrService.getDocument(id, type);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(doc.getContentType()))
                    .body(doc.getData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
