package com.crm.apitoken.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiTokenResponse(
        UUID id,
        String name,
        String tokenPrefix,
        String[] scopes,
        LocalDateTime lastUsedAt,
        LocalDateTime expiresAt,
        boolean active,
        LocalDateTime createdAt,
        // Preenchido apenas na criação — token completo mostrado uma única vez
        String plainToken
) {}
