package com.crm.ticket;

import com.crm.common.BaseTenantEntity;
import com.crm.contact.Contact;
import com.crm.tag.Tag;
import com.crm.user.User;
import com.crm.whatsapp.WhatsAppConversation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Ticket extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Número de protocolo (sequência global). Definido pelo service na criação. */
    @Column(nullable = false, updatable = false)
    private Long protocol;

    private String subject;

    @Column(nullable = false)
    @Builder.Default private String channel = "WHATSAPP";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default private TicketStatus status = TicketStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default private String priority = "MEDIUM";

    /** Quando true, o agente de IA do n8n responde automaticamente neste ticket. */
    @Column(name = "ai_enabled", nullable = false)
    @Builder.Default private boolean aiEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "contact_id")
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "conversation_id")
    private WhatsAppConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "queue_id")
    private Queue queue;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "close_reason_id")
    private CloseReason closeReason;

    private Integer rating;
    @Column(columnDefinition = "TEXT") private String ratingComment;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ticket_tags",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    private LocalDateTime lastMessageAt;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
