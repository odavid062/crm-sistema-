package com.crm.tenant;

import com.crm.plan.Plan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Tenant {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String document;
    private String email;
    private String phone;
    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default private TenantStatus status = TenantStatus.TRIAL;

    private LocalDate trialEndsAt;
    private LocalDate dueDate;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
