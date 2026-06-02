package com.crm.deal.dto;

import com.crm.deal.DealStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DealResponse(
        UUID id,
        String title,
        BigDecimal value,
        String currency,
        UUID stageId,
        String stageName,
        String stageColor,
        UUID pipelineId,
        String pipelineName,
        UUID contactId,
        String contactName,
        UUID companyId,
        String companyName,
        UUID ownerId,
        String ownerName,
        DealStatus status,
        String priority,
        LocalDate expectedCloseDate,
        LocalDateTime closedAt,
        String lostReason,
        String description,
        LocalDateTime createdAt
) {}
