package com.crm.valuereport.dto;

import com.crm.valuereport.AutomationLogType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Payload que o n8n (ou o próprio CRM) envia para registrar uma ação automatizada.
 * minutesSaved é opcional: se ausente, o serviço estima a partir do tipo + settings.
 */
public record AutomationLogRequest(
        @NotNull AutomationLogType type,
        String channel,
        UUID contactId,
        Boolean success,
        BigDecimal minutesSaved,
        String source,
        Map<String, Object> metadata
) {}
