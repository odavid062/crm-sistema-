package com.crm.ticket.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketNoteResponse(
        UUID id,
        String content,
        UUID createdById,
        String createdByName,
        LocalDateTime createdAt
) {}
