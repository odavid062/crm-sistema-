package com.crm.appointment.dto;

import com.crm.appointment.AppointmentStatus;
import com.crm.appointment.AppointmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentRequest(
        @NotBlank String title,
        String description,
        AppointmentType type,
        AppointmentStatus status,
        @NotNull LocalDateTime startAt,
        LocalDateTime endAt,
        Boolean allDay,
        String location,
        String meetingUrl,
        String color,
        UUID contactId,
        UUID dealId,
        UUID assignedTo,
        Integer reminderMinutes
) {}
