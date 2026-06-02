package com.crm.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookConfigRepository extends JpaRepository<WebhookConfig, UUID> {
    List<WebhookConfig> findByActiveTrue();

    /**
     * Busca webhooks ativos de UM tenant específico, inscritos no evento dado.
     * Filtragem explícita por tenant_id é essencial porque o dispatch é @Async e
     * roda em thread sem TenantContext (logo, sem o filtro automático do Hibernate).
     */
    @Query(value = """
        SELECT * FROM webhook_configs
        WHERE active = true
          AND tenant_id = :tenantId
          AND :event = ANY(events)
        """, nativeQuery = true)
    List<WebhookConfig> findActiveByTenantAndEvent(@Param("tenantId") UUID tenantId,
                                                   @Param("event") String event);
}
