package com.crm.appointment.dto;

import com.crm.appointment.AppointmentStatus;
import com.crm.appointment.AppointmentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        String title,
        String description,
        AppointmentType type,
        AppointmentStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean allDay,
        String location,
        String meetingUrl,
        String color,
        UUID contactId,
        String contactName,
        UUID dealId,
        String dealTitle,
        UUID assignedTo,
        String assignedToName,
        Integer reminderMinutes,
        LocalDateTime createdAt
) {}
