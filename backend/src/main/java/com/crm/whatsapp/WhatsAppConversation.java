package com.crm.whatsapp;

import com.crm.common.BaseTenantEntity;
import com.crm.contact.Contact;
import com.crm.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "whatsapp_conversations")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WhatsAppConversation extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String remoteJid;

    private String contactName;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "contact_id")
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.OPEN;

    private LocalDateTime lastMessageAt;
    @Builder.Default private int unreadCount = 0;
    private String instanceName;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    @Builder.Default
    private List<WhatsAppMessage> messages = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
