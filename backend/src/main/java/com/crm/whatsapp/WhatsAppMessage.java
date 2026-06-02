package com.crm.whatsapp;

import com.crm.common.BaseTenantEntity;
import com.crm.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "whatsapp_messages")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WhatsAppMessage extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private WhatsAppConversation conversation;

    private String messageId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String mediaUrl;
    private String mediaType;
    private String mediaCaption;

    @Column(nullable = false)
    private String direction;

    @Builder.Default private String status = "SENT";

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sender_id")
    private User sender;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
