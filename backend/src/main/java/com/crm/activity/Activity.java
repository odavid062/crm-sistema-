package com.crm.activity;

import com.crm.common.BaseTenantEntity;
import com.crm.company.Company;
import com.crm.contact.Contact;
import com.crm.deal.Deal;
import com.crm.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Activity extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default private ActivityStatus status = ActivityStatus.PENDING;

    @Builder.Default private String priority = "MEDIUM";

    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "contact_id")
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "deal_id")
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
