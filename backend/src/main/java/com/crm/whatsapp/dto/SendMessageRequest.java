package com.crm.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank String to,
        String text,
        String mediaUrl,
        String mediaType,
        String caption
) {}
