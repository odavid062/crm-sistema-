package com.crm.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * Superclasse para todas as entidades pertencentes a um tenant.
 * O filtro do Hibernate (tenantFilter) é habilitado por requisição no TenantFilterAspect,
 * garantindo que cada tenant só enxergue seus próprios dados.
 */
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@EntityListeners(TenantEntityListener.class)
@Getter
@Setter
public abstract class BaseTenantEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
}
