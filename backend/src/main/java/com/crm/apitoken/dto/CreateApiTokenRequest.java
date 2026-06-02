package com.crm.apitoken.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public record CreateApiTokenRequest(
        @NotBlank String name,
        List<String> scopes,
        LocalDateTime expiresAt
) {}
