package com.crm.company.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        String cnpj,
        String email,
        String phone,
        String website,
        String industry,
        String size,
        String address,
        String city,
        String state,
        String country,
        String notes,
        UUID ownerId,
        String ownerName,
        Set<TagDto> tags,
        LocalDateTime createdAt
) {
    public record TagDto(UUID id, String name, String color) {}
}
