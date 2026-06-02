package com.crm.tenant.dto;

import com.crm.tenant.TenantStatus;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTenantRequest(
        String name,
        String document,
        String email,
        String phone,
        String logoUrl,
        UUID planId,
        TenantStatus status,
        LocalDate dueDate
) {}
