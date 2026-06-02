package com.crm.deal.dto;

import com.crm.deal.DealStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DealRequest(
        @NotBlank String title,
        BigDecimal value,
        @NotNull UUID stageId,
        @NotNull UUID pipelineId,
        UUID contactId,
        UUID companyId,
        UUID ownerId,
        DealStatus status,
        String priority,
        LocalDate expectedCloseDate,
        String lostReason,
        String description
) {}
