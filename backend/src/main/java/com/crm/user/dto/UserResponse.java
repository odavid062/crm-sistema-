package com.crm.user.dto;

import com.crm.user.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        String avatarUrl,
        String phone,
        boolean active,
        LocalDateTime createdAt
) {}
