package com.crm.tenant.dto;

import com.crm.tenant.TenantStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        String document,
        String email,
        String phone,
        String logoUrl,
        UUID planId,
        String planName,
        TenantStatus status,
        LocalDate trialEndsAt,
        LocalDate dueDate,
        long userCount,
        LocalDateTime createdAt
) {}
