package org.example.proyecto_baseee.repository;

import org.example.proyecto_baseee.model.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
    List<EmployeeDocument> findByEmployeeId(Long employeeId);
    Optional<EmployeeDocument> findByEmployeeIdAndDocumentType(Long employeeId, String documentType);
}
