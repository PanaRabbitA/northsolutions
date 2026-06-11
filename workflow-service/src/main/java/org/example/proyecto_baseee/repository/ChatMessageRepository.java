package org.example.proyecto_baseee.repository;

import org.example.proyecto_baseee.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByOrderByTimestampAsc();
    // To avoid loading too many messages, maybe get top N
    List<ChatMessage> findTop50ByRoomOrderByTimestampDesc(String room);
}
