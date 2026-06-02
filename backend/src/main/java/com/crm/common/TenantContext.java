package com.crm.common;

import java.util.UUID;

/**
 * Mantém o tenant atual da requisição em um ThreadLocal.
 * Populado pelo JwtAuthFilter (login de usuário) ou ApiTokenAuthFilter (API externa).
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SUPER_ADMIN = ThreadLocal.withInitial(() -> false);

    private TenantContext() {}

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setSuperAdmin(boolean superAdmin) {
        SUPER_ADMIN.set(superAdmin);
    }

    public static boolean isSuperAdmin() {
        return Boolean.TRUE.equals(SUPER_ADMIN.get());
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        SUPER_ADMIN.remove();
    }
}
