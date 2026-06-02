package com.crm.valuereport;

import com.crm.common.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "automation_logs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AutomationLog extends BaseTenantEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AutomationLogType type;

    private String channel;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(nullable = false)
    @Builder.Default private boolean success = true;

    @Column(nullable = false)
    @Builder.Default private BigDecimal minutesSaved = BigDecimal.ZERO;

    @Builder.Default private String source = "n8n";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
