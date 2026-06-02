package com.crm.payment;

import com.crm.common.BaseTenantEntity;
import com.crm.contact.Contact;
import com.crm.deal.Deal;
import com.crm.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Payment extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String asaasId;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "contact_id")
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "deal_id")
    private Deal deal;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(nullable = false)
    private String billingType;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate paymentDate;
    @Column(columnDefinition = "TEXT") private String invoiceUrl;
    @Column(columnDefinition = "TEXT") private String bankSlipUrl;
    @Column(columnDefinition = "TEXT") private String pixCode;
    @Column(columnDefinition = "TEXT") private String pixQrCodeImage;
    private String nossoNumero;
    private String invoiceNumber;
    private String externalReference;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
