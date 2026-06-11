package org.example.proyecto_baseee.service;

import org.example.proyecto_baseee.model.ChatMessage;
import org.example.proyecto_baseee.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ChatService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    public ChatMessage sendMessage(ChatMessage message) {
        if (message.getRoom() == null) {
            message.setRoom("GLOBAL");
        }
        return chatMessageRepository.save(message);
    }
    
    public List<ChatMessage> getRecentMessages(String room) {
        List<ChatMessage> msgs = chatMessageRepository.findTop50ByRoomOrderByTimestampDesc(room);
        Collections.reverse(msgs); // Desc gives the most recent 50, then we reverse for chronological order
        return msgs;
    }
}
