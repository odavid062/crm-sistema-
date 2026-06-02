package com.crm.activity.dto;

import com.crm.activity.ActivityStatus;
import com.crm.activity.ActivityType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Atualização parcial de atividade (todos os campos opcionais).
 * Usado pelo Kanban (mover = mudar status) e Calendário (reagendar = mudar dueDate).
 */
public record ActivityUpdateRequest(
        String title,
        ActivityType type,
        String description,
        ActivityStatus status,
        String priority,
        LocalDateTime dueDate,
        Integer durationMinutes,
        UUID contactId,
        UUID dealId,
        UUID assignedTo
) {}
