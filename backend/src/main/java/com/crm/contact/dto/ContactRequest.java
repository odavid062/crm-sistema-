package com.crm.contact.dto;

import com.crm.contact.ContactStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ContactRequest(
        @NotBlank String name,
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
        String zipCode,
        String country,
        String notes,
        String avatarUrl,
        UUID companyId,
        UUID ownerId,
        Set<UUID> tagIds
) {}
