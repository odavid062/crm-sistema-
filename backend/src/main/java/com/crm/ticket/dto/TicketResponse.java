package com.crm.ticket.dto;

import com.crm.ticket.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        Long protocol,
        String subject,
        String channel,
        TicketStatus status,
        String priority,
        boolean aiEnabled,
        UUID contactId,
        String contactName,
        UUID conversationId,
        UUID queueId,
        String queueName,
        String queueColor,
        UUID assignedTo,
        String assignedToName,
        UUID closeReasonId,
        String closeReasonName,
        Integer rating,
        String ratingComment,
        List<TagDto> tags,
        LocalDateTime lastMessageAt,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        LocalDateTime createdAt
) {
    public record TagDto(UUID id, String name, String color) {}
}
