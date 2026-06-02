package com.crm.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Cria um tenant junto com seu primeiro usuário ADMIN (provisionamento).
 */
public record CreateTenantRequest(
        @NotBlank String name,
        @NotBlank String slug,
        String document,
        String email,
        String phone,
        UUID planId,

        // Primeiro admin do tenant
        @NotBlank String adminName,
        @NotBlank @Email String adminEmail,
        @NotBlank @Size(min = 6) String adminPassword
) {}
