package com.crm.pipeline;

import com.crm.common.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pipeline_stages")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PipelineStage extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @Column(nullable = false)
    private String name;

    @Builder.Default private String color = "#6366f1";
    @Builder.Default private int position = 0;
    @Builder.Default private double winProbability = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
