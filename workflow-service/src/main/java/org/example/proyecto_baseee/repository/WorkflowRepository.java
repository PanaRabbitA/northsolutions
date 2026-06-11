package org.example.proyecto_baseee.repository;

import org.example.proyecto_baseee.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {
    java.util.List<Workflow> findByEstado(String estado);
    java.util.Optional<Workflow> findByDocumentId(Long documentId);
    java.util.List<Workflow> findByModulo(String modulo);
}