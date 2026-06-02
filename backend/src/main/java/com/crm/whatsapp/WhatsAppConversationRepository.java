package com.crm.whatsapp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WhatsAppConversationRepository extends JpaRepository<WhatsAppConversation, UUID> {
    Optional<WhatsAppConversation> findByRemoteJidAndInstanceName(String remoteJid, String instanceName);
    Page<WhatsAppConversation> findByStatusOrderByLastMessageAtDesc(ConversationStatus status, Pageable pageable);
    Page<WhatsAppConversation> findByAssignedToIdOrderByLastMessageAtDesc(UUID userId, Pageable pageable);
}
