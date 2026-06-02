package com.crm.ticket.dto;

import java.util.UUID;

public record CloseTicketRequest(
        UUID closeReasonId,
        Integer rating,        // CSAT 1..5 (opcional)
        String ratingComment
) {}
