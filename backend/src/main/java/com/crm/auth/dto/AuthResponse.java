package com.crm.auth.dto;

import com.crm.user.UserRole;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID userId,
        String name,
        String email,
        UserRole role,
        UUID tenantId
) {
    public AuthResponse(String accessToken, String refreshToken, UUID userId, String name,
                        String email, UserRole role, UUID tenantId) {
        this(accessToken, refreshToken, "Bearer", userId, name, email, role, tenantId);
    }
}
