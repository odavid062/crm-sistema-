package com.crm.user.dto;

import com.crm.user.UserRole;
import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
        String name,
        @Email String email,
        String phone,
        String avatarUrl,
        UserRole role
) {}
