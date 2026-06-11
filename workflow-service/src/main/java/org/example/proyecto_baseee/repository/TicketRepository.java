package org.example.proyecto_baseee.repository;

import org.example.proyecto_baseee.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    List<Ticket> findAllByOrderByCreatedAtDesc();
    
    // Check if user has active tickets (not finished)
    boolean existsByCreatedByAndStatusNot(String createdBy, String status);
}
