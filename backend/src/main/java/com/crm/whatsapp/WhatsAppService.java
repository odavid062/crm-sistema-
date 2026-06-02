package com.crm.whatsapp;

import com.crm.contact.ContactRepository;
import com.crm.event.CrmEvent;
import com.crm.ticket.TicketService;
import com.crm.whatsapp.dto.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final WhatsAppConversationRepository conversationRepository;
    private final WhatsAppMessageRepository messageRepository;
    private final ContactRepository contactRepository;
    private final UazApiClient uazApiClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher events;
    private final TicketService ticketService;

    @Value("${uazapi.instance}")
    private String instanceName;

    public Page<WhatsAppConversation> findConversations(ConversationStatus status, Pageable pageable) {
        if (status != null) return conversationRepository.findByStatusOrderByLastMessageAtDesc(status, pageable);
        return conversationRepository.findAll(pageable);
    }

    public Page<WhatsAppMessage> findMessages(UUID conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId, pageable);
    }

    @Transactional
    public WhatsAppMessage sendMessage(UUID conversationId, SendMessageRequest request) {
        WhatsAppConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada"));

        Map<String, Object> result;
        if (request.text() != null) {
            result = uazApiClient.sendText(conversation.getRemoteJid(), request.text());
        } else {
            result = uazApiClient.sendMedia(conversation.getRemoteJid(), request.mediaUrl(), request.caption(), request.mediaType());
        }

        WhatsAppMessage message = WhatsAppMessage.builder()
                .conversation(conversation)
                .content(request.text())
                .mediaUrl(request.mediaUrl())
                .mediaType(request.mediaType())
                .mediaCaption(request.caption())
                .direction("OUT")
                .status("SENT")
                .timestamp(LocalDateTime.now())
                .build();

        message = messageRepository.save(message);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, message);
        return message;
    }

    @Transactional
    public WhatsAppConversation processIncomingWebhook(Map<String, Object> payload) {
        try {
            String remoteJid = (String) payload.get("from");
            String text = (String) payload.get("text");
            String messageId = (String) payload.get("id");

            WhatsAppConversation conversation = conversationRepository
                    .findByRemoteJidAndInstanceName(remoteJid, instanceName)
                    .orElseGet(() -> {
                        WhatsAppConversation c = WhatsAppConversation.builder()
                                .remoteJid(remoteJid)
                                .instanceName(instanceName)
                                .contactName((String) payload.get("pushName"))
                                .status(ConversationStatus.OPEN)
                                .build();
                        contactRepository.findAll().stream()
                                .filter(ct -> remoteJid.replace("@s.whatsapp.net", "")
                                        .equals(ct.getWhatsapp() != null ? ct.getWhatsapp().replaceAll("[^0-9]", "") : ""))
                                .findFirst()
                                .ifPresent(c::setContact);
                        return conversationRepository.save(c);
                    });

            if (messageRepository.findByMessageId(messageId).isEmpty()) {
                WhatsAppMessage message = WhatsAppMessage.builder()
                        .conversation(conversation)
                        .messageId(messageId)
                        .content(text)
                        .direction("IN")
                        .status("RECEIVED")
                        .timestamp(LocalDateTime.now())
                        .build();
                WhatsAppMessage savedMessage = messageRepository.save(message);
                conversation.setLastMessageAt(LocalDateTime.now());
                conversation.setUnreadCount(conversation.getUnreadCount() + 1);
                conversationRepository.save(conversation);
                messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getId(), message);
                messagingTemplate.convertAndSend("/topic/conversations", conversation);
                // Garante um ticket aberto para esta conversa (entra na fila de atendimento)
                ticketService.ensureOpenTicket(conversation);
                // Evento para o n8n: o agente de IA só responde se aiEnabled = true neste ticket
                java.util.Map<String, Object> evt = new java.util.HashMap<>();
                evt.put("conversationId", conversation.getId());
                evt.put("messageId", savedMessage.getId());
                evt.put("remoteJid", remoteJid);
                evt.put("text", text != null ? text : "");
                evt.put("pushName", payload.getOrDefault("pushName", ""));
                evt.put("contactId", conversation.getContact() != null ? conversation.getContact().getId() : null);
                evt.put("aiEnabled", ticketService.isAiEnabledForConversation(conversation.getId()));
                events.publishEvent(new CrmEvent("whatsapp.message.received", conversation.getTenantId(), evt));
            }
            return conversation;
        } catch (Exception e) {
            log.error("Erro ao processar webhook WhatsApp: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar mensagem recebida");
        }
    }

    @Transactional
    public WhatsAppConversation updateStatus(UUID id, ConversationStatus status) {
        WhatsAppConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada"));
        conversation.setStatus(status);
        return conversationRepository.save(conversation);
    }

    public Map<String, Object> getInstanceStatus() {
        return uazApiClient.getInstanceStatus();
    }

    public Map<String, Object> getQrCode() {
        return uazApiClient.getQrCode();
    }
}
