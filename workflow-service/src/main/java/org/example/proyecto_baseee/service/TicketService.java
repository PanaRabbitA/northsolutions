package org.example.proyecto_baseee.service;

import org.example.proyecto_baseee.model.Ticket;
import org.example.proyecto_baseee.model.TicketMessage;
import org.example.proyecto_baseee.repository.TicketRepository;
import org.example.proyecto_baseee.repository.TicketMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private TicketMessageRepository ticketMessageRepository;
    
    public Ticket createTicket(Ticket ticket) {
        if (ticketRepository.existsByCreatedByAndStatusNot(ticket.getCreatedBy(), "FINALIZADO")) {
            throw new RuntimeException("No puedes crear un nuevo ticket porque tienes un ticket activo.");
        }
        ticket.setStatus("RECIBIDO");
        return ticketRepository.save(ticket);
    }
    
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Ticket> getUserTickets(String username) {
        return ticketRepository.findByCreatedByOrderByCreatedAtDesc(username);
    }
    
    public Ticket getTicket(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }
    
    public Ticket updateTicketStatus(Long id, String status) {
        Ticket ticket = getTicket(id);
        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }
    
    public TicketMessage addMessage(Long ticketId, TicketMessage message) {
        message.setTicketId(ticketId);
        return ticketMessageRepository.save(message);
    }
    
    public List<TicketMessage> getMessages(Long ticketId) {
        return ticketMessageRepository.findByTicketIdOrderByTimestampAsc(ticketId);
    }
}
