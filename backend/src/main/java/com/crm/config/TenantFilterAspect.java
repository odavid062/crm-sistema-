package com.crm.config;

import com.crm.common.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Habilita o filtro de tenant do Hibernate antes de cada método de service.
 * Quando há um tenant no contexto, todas as queries são automaticamente
 * restritas a esse tenant. Super admins (tenant nulo) enxergam todos os dados.
 */
@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.crm..*Service.*(..))")
    public void enableTenantFilter() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null && !TenantContext.isSuperAdmin()) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }
    }
}
