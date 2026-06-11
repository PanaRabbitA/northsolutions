package org.example.proyecto_baseee.controller;

import org.example.proyecto_baseee.model.Ticket;
import org.example.proyecto_baseee.model.TicketMessage;
import org.example.proyecto_baseee.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
        try {
            return ResponseEntity.ok(ticketService.createTicket(ticket));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Ticket>> getUserTickets(@PathVariable String username) {
        return ResponseEntity.ok(ticketService.getUserTickets(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Ticket> updateTicketStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, status));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<TicketMessage> addMessage(@PathVariable Long id, @RequestBody TicketMessage message) {
        return ResponseEntity.ok(ticketService.addMessage(id, message));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<TicketMessage>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getMessages(id));
    }
}
