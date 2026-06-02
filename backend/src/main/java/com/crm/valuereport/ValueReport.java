package com.crm.valuereport;

import com.crm.common.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "value_reports")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ValueReport extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportPeriodType periodType;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false)
    @Builder.Default private String status = "READY";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metrics;

    @Column(columnDefinition = "TEXT")
    private String executiveSummary;

    @Column(nullable = false) @Builder.Default private BigDecimal revenue = BigDecimal.ZERO;
    @Column(nullable = false) @Builder.Default private BigDecimal investment = BigDecimal.ZERO;
    @Column(nullable = false) @Builder.Default private BigDecimal roiPercent = BigDecimal.ZERO;
    @Column(nullable = false) @Builder.Default private BigDecimal hoursSaved = BigDecimal.ZERO;

    @Column(nullable = false) @Builder.Default private LocalDateTime generatedAt = LocalDateTime.now();
    @Column(nullable = false, updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
