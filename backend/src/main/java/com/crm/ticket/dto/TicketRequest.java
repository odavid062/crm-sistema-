package com.crm.ticket.dto;

import java.util.UUID;

public record TicketRequest(
        UUID contactId,
        String channel,
        UUID queueId,
        String subject,
        String priority,
        UUID conversationId
) {}
