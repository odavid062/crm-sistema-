package com.crm.ticket;

import com.crm.common.TenantContext;
import com.crm.contact.ContactRepository;
import com.crm.event.CrmEvent;
import com.crm.tag.Tag;
import com.crm.tag.TagRepository;
import com.crm.ticket.dto.CloseTicketRequest;
import com.crm.ticket.dto.TicketNoteResponse;
import com.crm.ticket.dto.TicketRequest;
import com.crm.ticket.dto.TicketResponse;
import com.crm.user.User;
import com.crm.user.UserRepository;
import com.crm.whatsapp.WhatsAppConversation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketNoteRepository noteRepository;
    private final QueueRepository queueRepository;
    private final CloseReasonRepository closeReasonRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher events;

    // -------------------- Consultas --------------------

    public List<TicketResponse> search(TicketStatus status, UUID queueId, UUID assignedTo, String channel) {
        return ticketRepository.search(status, queueId, assignedTo, channel).stream().map(this::toResponse).toList();
    }

    public TicketResponse get(UUID id) {
        return toResponse(find(id));
    }

    public List<TicketNoteResponse> notes(UUID id) {
        return noteRepository.findByTicketIdOrderByCreatedAtAsc(id).stream().map(this::toNoteResponse).toList();
    }

    // -------------------- Ciclo de vida --------------------

    @Transactional
    public TicketResponse create(TicketRequest req) {
        Ticket t = Ticket.builder()
                .protocol(ticketRepository.nextProtocol())
                .subject(req.subject())
                .channel(req.channel() != null ? req.channel() : "WHATSAPP")
                .priority(req.priority() != null ? req.priority() : "MEDIUM")
                .status(TicketStatus.PENDING)
                .lastMessageAt(LocalDateTime.now())
                .build();
        if (req.contactId() != null) t.setContact(contactRepository.findById(req.contactId()).orElse(null));
        if (req.queueId() != null) t.setQueue(queueRepository.findById(req.queueId()).orElse(null));
        else queueRepository.findByActiveTrueOrderByOrderIndexAsc().stream().findFirst().ifPresent(t::setQueue);
        Ticket saved = ticketRepository.save(t);
        publish("ticket.created", saved);
        return toResponse(saved);
    }

    @Transactional
    public TicketResponse accept(UUID id) {
        Ticket t = find(id);
        User me = currentUser();
        if (me != null) t.setAssignedTo(me);
        t.setStatus(TicketStatus.OPEN);
        if (t.getOpenedAt() == null) t.setOpenedAt(LocalDateTime.now());
        // Humano assumiu: desliga o agente de IA automaticamente.
        t.setAiEnabled(false);
        Ticket saved = ticketRepository.save(t);
        publish("ticket.assigned", saved);
        publish("ticket.ai_toggled", saved);
        return toResponse(saved);
    }

    /** Liga/desliga o agente de IA do n8n para este ticket. */
    @Transactional
    public TicketResponse toggleAi(UUID id, boolean enabled) {
        Ticket t = find(id);
        t.setAiEnabled(enabled);
        Ticket saved = ticketRepository.save(t);
        publish("ticket.ai_toggled", saved);
        return toResponse(saved);
    }

    @Transactional
    public TicketResponse assign(UUID id, UUID userId) {
        Ticket t = find(id);
        t.setAssignedTo(userId != null ? userRepository.findById(userId).orElse(null) : null);
        if (t.getStatus() == TicketStatus.PENDING) t.setStatus(TicketStatus.OPEN);
        if (t.getOpenedAt() == null) t.setOpenedAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(t);
        publish("ticket.assigned", saved);
        return toResponse(saved);
    }

    @Transactional
    public TicketResponse transfer(UUID id, UUID queueId) {
        Ticket t = find(id);
        t.setQueue(queueId != null ? queueRepository.findById(queueId).orElse(null) : null);
        return toResponse(ticketRepository.save(t));
    }

    @Transactional
    public TicketResponse addTag(UUID id, UUID tagId) {
        Ticket t = find(id);
        tagRepository.findById(tagId).ifPresent(t.getTags()::add);
        return toResponse(ticketRepository.save(t));
    }

    @Transactional
    public TicketResponse removeTag(UUID id, UUID tagId) {
        Ticket t = find(id);
        t.getTags().removeIf(tag -> tag.getId().equals(tagId));
        return toResponse(ticketRepository.save(t));
    }

    @Transactional
    public TicketNoteResponse addNote(UUID id, String content) {
        Ticket t = find(id);
        TicketNote note = TicketNote.builder().ticket(t).content(content).createdBy(currentUser()).build();
        return toNoteResponse(noteRepository.save(note));
    }

    @Transactional
    public TicketResponse close(UUID id, CloseTicketRequest req) {
        Ticket t = find(id);
        t.setStatus(TicketStatus.CLOSED);
        t.setClosedAt(LocalDateTime.now());
        if (req.closeReasonId() != null) t.setCloseReason(closeReasonRepository.findById(req.closeReasonId()).orElse(null));
        if (req.rating() != null) t.setRating(req.rating());
        if (req.ratingComment() != null) t.setRatingComment(req.ratingComment());
        Ticket saved = ticketRepository.save(t);
        publish("ticket.closed", saved);
        return toResponse(saved);
    }

    @Transactional
    public TicketResponse reopen(UUID id) {
        Ticket t = find(id);
        t.setStatus(TicketStatus.OPEN);
        t.setClosedAt(null);
        return toResponse(ticketRepository.save(t));
    }

    private TicketNoteResponse toNoteResponse(TicketNote n) {
        return new TicketNoteResponse(
                n.getId(), n.getContent(),
                n.getCreatedBy() != null ? n.getCreatedBy().getId() : null,
                n.getCreatedBy() != null ? n.getCreatedBy().getName() : null,
                n.getCreatedAt());
    }

    // -------------------- Integração WhatsApp --------------------

    /** Garante um ticket aberto para a conversa (chamado ao receber mensagem no webhook). */
    @Transactional
    public void ensureOpenTicket(WhatsAppConversation conversation) {
        ticketRepository.findByConversationIdAndStatusNot(conversation.getId(), TicketStatus.CLOSED)
                .ifPresentOrElse(
                        t -> { t.setLastMessageAt(LocalDateTime.now()); ticketRepository.save(t); },
                        () -> {
                            Ticket t = Ticket.builder()
                                    .protocol(ticketRepository.nextProtocol())
                                    .channel("WHATSAPP")
                                    .status(TicketStatus.PENDING)
                                    .conversation(conversation)
                                    .contact(conversation.getContact())
                                    .subject(conversation.getContactName())
                                    .lastMessageAt(LocalDateTime.now())
                                    .build();
                            queueRepository.findByActiveTrueOrderByOrderIndexAsc().stream().findFirst().ifPresent(t::setQueue);
                            t.setTenantId(conversation.getTenantId());
                            Ticket saved = ticketRepository.save(t);
                            publish("ticket.created", saved);
                        });
    }

    /** Indica se o agente de IA deve responder nesta conversa (ticket aberto). Default true. */
    public boolean isAiEnabledForConversation(UUID conversationId) {
        return ticketRepository.findByConversationIdAndStatusNot(conversationId, TicketStatus.CLOSED)
                .map(Ticket::isAiEnabled).orElse(true);
    }

    // -------------------- Helpers --------------------

    private Ticket find(UUID id) {
        return ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket não encontrado"));
    }

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof User u) ? u : null;
    }

    private void publish(String event, Ticket t) {
        if (t.getTenantId() != null) {
            events.publishEvent(new CrmEvent(event, t.getTenantId(), toResponse(t)));
        }
    }

    public TicketResponse toResponse(Ticket t) {
        List<TicketResponse.TagDto> tags = t.getTags().stream()
                .map(tag -> new TicketResponse.TagDto(tag.getId(), tag.getName(), tag.getColor()))
                .toList();
        return new TicketResponse(
                t.getId(), t.getProtocol(), t.getSubject(), t.getChannel(), t.getStatus(), t.getPriority(),
                t.isAiEnabled(),
                t.getContact() != null ? t.getContact().getId() : null,
                t.getContact() != null ? t.getContact().getName() : null,
                t.getConversation() != null ? t.getConversation().getId() : null,
                t.getQueue() != null ? t.getQueue().getId() : null,
                t.getQueue() != null ? t.getQueue().getName() : null,
                t.getQueue() != null ? t.getQueue().getColor() : null,
                t.getAssignedTo() != null ? t.getAssignedTo().getId() : null,
                t.getAssignedTo() != null ? t.getAssignedTo().getName() : null,
                t.getCloseReason() != null ? t.getCloseReason().getId() : null,
                t.getCloseReason() != null ? t.getCloseReason().getName() : null,
                t.getRating(), t.getRatingComment(), tags,
                t.getLastMessageAt(), t.getOpenedAt(), t.getClosedAt(), t.getCreatedAt()
        );
    }
}
