package com.crm.company.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;
import java.util.UUID;

public record CompanyRequest(
        @NotBlank String name,
        String cnpj,
        String email,
        String phone,
        String website,
        String industry,
        String size,
        String address,
        String city,
        String state,
        String zipCode,
        String country,
        String notes,
        UUID ownerId,
        Set<UUID> tagIds
) {}
