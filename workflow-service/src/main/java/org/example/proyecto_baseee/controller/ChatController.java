package org.example.proyecto_baseee.controller;

import org.example.proyecto_baseee.model.ChatMessage;
import org.example.proyecto_baseee.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatMessage>> getRecentMessages(@RequestParam(defaultValue = "GLOBAL") String room) {
        return ResponseEntity.ok(chatService.getRecentMessages(room));
    }

    @PostMapping
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        return ResponseEntity.ok(chatService.sendMessage(message));
    }
}
