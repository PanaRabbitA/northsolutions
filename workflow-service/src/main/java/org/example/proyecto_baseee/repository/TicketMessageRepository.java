package org.example.proyecto_baseee.repository;

import org.example.proyecto_baseee.model.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
    List<TicketMessage> findByTicketIdOrderByTimestampAsc(Long ticketId);
}
