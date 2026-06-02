package com.crm.config;

import com.crm.common.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Habilita o filtro de tenant na sessão do Hibernate (ligada à requisição via OSIV)
 * antes do controller executar. Cobre inclusive controllers que usam repositórios
 * diretamente (Report, Tag, Note, Notification), garantindo isolamento total.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null && !TenantContext.isSuperAdmin()) {
            try {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            } catch (Exception ignored) {
                // Sem sessão ligada (ex.: endpoints sem JPA) — ignora.
            }
        }
        return true;
    }
}
