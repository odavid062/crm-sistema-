package com.crm.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull UUID contactId,
        UUID dealId,
        @NotBlank String description,
        @NotNull @Positive BigDecimal value,
        @NotBlank String billingType,
        @NotNull LocalDate dueDate,
        String externalReference,
        String asaasCustomerId
) {}
