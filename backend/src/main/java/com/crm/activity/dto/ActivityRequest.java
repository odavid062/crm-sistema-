package com.crm.activity.dto;

import com.crm.activity.ActivityStatus;
import com.crm.activity.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityRequest(
        @NotBlank String title,
        @NotNull ActivityType type,
        String description,
        ActivityStatus status,
        String priority,
        LocalDateTime dueDate,
        Integer durationMinutes,
        UUID contactId,
        UUID companyId,
        UUID dealId,
        UUID assignedTo
) {}
