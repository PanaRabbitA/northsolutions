package org.example.proyecto_baseee.controller;

import org.example.proyecto_baseee.dto.AuthResponse;
import org.example.proyecto_baseee.dto.DocumentResponse;
import org.example.proyecto_baseee.dto.RoleResponse;
import org.example.proyecto_baseee.dto.UserResponse;
import org.example.proyecto_baseee.service.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/procesos")
@CrossOrigin(origins = "*")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }


    @PostMapping("/aprobar-documento/{idDocumento}")
    public ResponseEntity<?> aprobarFlujoCompleto(
            @PathVariable Long idDocumento,
            @RequestParam String token) {

        Map<String, Object> respuesta = new HashMap<>();

        try {
            AuthResponse auth = workflowService.validarToken(token);
            if (auth == null || auth.getUsername() == null) {
                throw new RuntimeException("Token inválido");
            }

            UserResponse user = workflowService.obtenerResponsable(auth.getUsername());

            RoleResponse role = workflowService.validarPermiso(user.getUsername());

            DocumentResponse doc = workflowService.consultarDocumento(idDocumento);
            
            // Aprobar en la BD de Workflow
            workflowService.aprobarWorkflow(idDocumento);

            respuesta.put("mensaje", "Flujo completado con éxito. Documento aprobado.");
            respuesta.put("responsable", user.getUsername());
            respuesta.put("documento", doc);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            respuesta.put("error", "Error de integración con microservicios externos");
            respuesta.put("detalle", e.getMessage());
            respuesta.put("accion_sugerida", "Verifique que el proveedor esté encendido en Eureka o que los datos sean correctos.");

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(respuesta);
        }
    }

    @PostMapping("/rechazar-documento/{idDocumento}")
    public ResponseEntity<?> rechazarFlujoCompleto(
            @PathVariable Long idDocumento,
            @RequestParam String token) {

        Map<String, Object> respuesta = new HashMap<>();

        try {
            AuthResponse auth = workflowService.validarToken(token);
            if (auth == null || auth.getUsername() == null) {
                throw new RuntimeException("Token inválido");
            }

            UserResponse user = workflowService.obtenerResponsable(auth.getUsername());
            RoleResponse role = workflowService.validarPermiso(user.getUsername());
            DocumentResponse doc = workflowService.consultarDocumento(idDocumento);
            
            // Rechazar en la BD de Workflow
            workflowService.rechazarWorkflow(idDocumento);

            respuesta.put("mensaje", "Flujo completado con éxito. Documento rechazado.");
            respuesta.put("responsable", user.getUsername());
            respuesta.put("documento", doc);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            respuesta.put("error", "Error al rechazar el documento");
            respuesta.put("detalle", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarAprobacion(
            @RequestParam Long documentId,
            @RequestParam String modulo,
            @RequestParam String token) {
        try {
            AuthResponse auth = workflowService.validarToken(token);
            if (auth == null || !auth.isValid()) {
                throw new RuntimeException("Token inválido");
            }
            return ResponseEntity.ok(workflowService.solicitarAprobacion(documentId, modulo, auth.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> getPendientes(@RequestParam String token) {
        try {
            AuthResponse auth = workflowService.validarToken(token);
            if (auth == null || !auth.isValid()) {
                throw new RuntimeException("Token inválido");
            }
            RoleResponse role = workflowService.validarPermiso(auth.getUsername());
            if (!role.isPermitido()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tiene permisos para ver esta lista."));
            }
            return ResponseEntity.ok(workflowService.getPendientes());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/modulo/{modulo}")
    public ResponseEntity<?> getWorkflowsPorModulo(@PathVariable String modulo, @RequestParam String token) {
        try {
            AuthResponse auth = workflowService.validarToken(token);
            if (auth == null || !auth.isValid()) {
                throw new RuntimeException("Token inválido");
            }
            return ResponseEntity.ok(workflowService.getWorkflowsPorModulo(modulo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}