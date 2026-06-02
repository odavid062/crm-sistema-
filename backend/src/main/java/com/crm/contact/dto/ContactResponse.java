package com.crm.contact.dto;

import com.crm.contact.ContactStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record ContactResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String whatsapp,
        String cpf,
        LocalDate birthDate,
        String position,
        String department,
        ContactStatus status,
        String source,
        String address,
        String city,
        String state,
        String country,
        String notes,
        String avatarUrl,
        UUID companyId,
        String companyName,
        UUID ownerId,
        String ownerName,
        Set<TagDto> tags,
        LocalDateTime createdAt
) {
    public record TagDto(UUID id, String name, String color) {}
}
