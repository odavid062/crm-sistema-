package com.crm.common;

import jakarta.persistence.PrePersist;

/**
 * Define automaticamente o tenant_id ao persistir uma entidade,
 * lendo o tenant atual do TenantContext. Evita que o desenvolvedor
 * precise setar tenant manualmente em cada create.
 */
public class TenantEntityListener {

    @PrePersist
    public void setTenantOnPersist(Object entity) {
        if (entity instanceof BaseTenantEntity tenantEntity && tenantEntity.getTenantId() == null) {
            tenantEntity.setTenantId(TenantContext.getTenantId());
        }
    }
}
