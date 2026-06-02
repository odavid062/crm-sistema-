package com.crm.plan;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "plans")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Plan {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    @Builder.Default private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default private int maxUsers = 5;
    @Builder.Default private int maxChannels = 1;
    @Builder.Default private int maxQueues = 3;
    @Builder.Default private int maxContacts = 1000;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> features;

    @Builder.Default private boolean active = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
