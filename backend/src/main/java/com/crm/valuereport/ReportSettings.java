package com.crm.valuereport;

import com.crm.common.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_settings")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportSettings extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Builder.Default private BigDecimal monthlyInvestment = BigDecimal.ZERO;

    @Builder.Default private String currency = "BRL";

    @Column(nullable = false)
    @Builder.Default private BigDecimal minutesPerMessage = BigDecimal.valueOf(2);

    @Column(nullable = false)
    @Builder.Default private BigDecimal minutesPerFollowup = BigDecimal.valueOf(5);

    @Column(nullable = false)
    @Builder.Default private BigDecimal minutesPerTask = BigDecimal.valueOf(10);

    @Column(nullable = false)
    @Builder.Default private BigDecimal hourlyCost = BigDecimal.valueOf(50);

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
