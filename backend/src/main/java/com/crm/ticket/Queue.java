package com.crm.ticket;

import com.crm.common.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/** Fila / Departamento de atendimento. (Não confundir com java.util.Queue.) */
@Entity
@Table(name = "queues")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Queue extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Builder.Default private String color = "#6366f1";
    @Builder.Default private int orderIndex = 0;

    @Column(columnDefinition = "TEXT")
    private String greetingMessage;

    @Builder.Default private boolean active = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
