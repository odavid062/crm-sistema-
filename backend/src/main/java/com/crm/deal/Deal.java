package com.crm.deal;

import com.crm.common.BaseTenantEntity;
import com.crm.company.Company;
import com.crm.contact.Contact;
import com.crm.pipeline.Pipeline;
import com.crm.pipeline.PipelineStage;
import com.crm.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deals")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Deal extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Builder.Default private BigDecimal value = BigDecimal.ZERO;
    @Builder.Default private String currency = "BRL";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private PipelineStage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default private DealStatus status = DealStatus.OPEN;

    @Builder.Default private String priority = "MEDIUM";
    private LocalDate expectedCloseDate;
    private LocalDateTime closedAt;
    private String lostReason;
    @Column(columnDefinition = "TEXT") private String description;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
