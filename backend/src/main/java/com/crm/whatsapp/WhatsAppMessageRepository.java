package com.crm.whatsapp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessage, UUID> {
    Page<WhatsAppMessage> findByConversationIdOrderByTimestampAsc(UUID conversationId, Pageable pageable);
    Optional<WhatsAppMessage> findByMessageId(String messageId);
}
