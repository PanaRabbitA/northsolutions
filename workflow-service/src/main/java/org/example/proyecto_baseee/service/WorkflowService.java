package org.example.proyecto_baseee.service;

import org.example.proyecto_baseee.dto.AuthResponse;
import org.example.proyecto_baseee.dto.DocumentResponse;
import org.example.proyecto_baseee.dto.RoleResponse;
import org.example.proyecto_baseee.dto.UserResponse;
import org.example.proyecto_baseee.model.Workflow;
import org.example.proyecto_baseee.repository.WorkflowRepository;
import org.example.proyecto_baseee.client.AuthClient;
import org.example.proyecto_baseee.client.UserClient;
import org.example.proyecto_baseee.client.SalesClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class WorkflowService {

    private final AuthClient authClient;
    private final UserClient userClient;
    private final SalesClient salesClient;
    private final WorkflowRepository workflowRepository;

    public WorkflowService(AuthClient authClient, UserClient userClient, SalesClient salesClient, WorkflowRepository workflowRepository) {
        this.authClient = authClient;
        this.userClient = userClient;
        this.salesClient = salesClient;
        this.workflowRepository = workflowRepository;
    }

    public AuthResponse validarToken(String token) {
        Map<String, String> request = new HashMap<>();
        request.put("token", token);
        return authClient.validarToken(request);
    }

    public UserResponse obtenerResponsable(String username) {
        return userClient.obtenerResponsable(username);
    }

    public RoleResponse validarPermiso(String username) {
        UserResponse user = obtenerResponsable(username);
        RoleResponse roleResponse = new RoleResponse();
        if (user != null && "ADMIN".equals(user.getRole())) {
            roleResponse.setPermitido(true);
            roleResponse.setMotivo("El usuario tiene rol ADMIN");
        } else {
            roleResponse.setPermitido(false);
            roleResponse.setMotivo("El usuario NO es administrador");
        }
        return roleResponse;
    }

    public DocumentResponse consultarDocumento(Long idDocumento) {
        Map<String, Object> sale = salesClient.consultarDocumento(idDocumento);
        DocumentResponse doc = new DocumentResponse();
        if (sale != null) {
            doc.setId(Long.valueOf(sale.get("id").toString()));
            doc.setTipo("Venta Automática");
            doc.setEstado("En Revisión - Total: $" + sale.get("total"));
        }
        return doc;
    }

    public Workflow solicitarAprobacion(Long documentId, String modulo, String username) {
        // Verificar si ya existe
        if (workflowRepository.findByDocumentId(documentId).isPresent()) {
            throw new RuntimeException("El documento ya tiene una solicitud pendiente o aprobada.");
        }
        Workflow workflow = new Workflow();
        workflow.setDocumentId(documentId);
        workflow.setModulo(modulo);
        workflow.setResponsable(username);
        workflow.setEstado("PENDIENTE");
        workflow.setNombre_proceso("Aprobación de " + modulo);
        workflow.setDescripcion("Solicitud de aprobación para documento #" + documentId);
        return workflowRepository.save(workflow);
    }

    public List<Workflow> getPendientes() {
        return workflowRepository.findByEstado("PENDIENTE");
    }

    public List<Workflow> getWorkflowsPorModulo(String modulo) {
        return workflowRepository.findByModulo(modulo);
    }

    public void aprobarWorkflow(Long documentId) {
        Workflow workflow = workflowRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("No se encontró solicitud pendiente para este documento."));
        workflow.setEstado("APROBADO");
        workflowRepository.save(workflow);
    }

    public void rechazarWorkflow(Long documentId) {
        Workflow workflow = workflowRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("No se encontró solicitud pendiente para este documento."));
        workflow.setEstado("RECHAZADO");
        workflowRepository.save(workflow);
    }
}