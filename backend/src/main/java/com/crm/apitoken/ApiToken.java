package com.crm.apitoken;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_tokens")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiToken {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    /** SHA-256 do token completo. O token em si nunca é armazenado. */
    @Column(nullable = false, unique = true)
    private String tokenHash;

    /** Prefixo visível para identificação (ex: crm_live_a1b2...). */
    @Column(nullable = false)
    private String tokenPrefix;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] scopes;

    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;

    @Builder.Default private boolean active = true;

    private UUID createdBy;

    @Column(nullable = false, updatable = false)
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
