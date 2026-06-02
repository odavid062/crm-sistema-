package com.crm.appointment;

import com.crm.appointment.dto.AppointmentRequest;
import com.crm.appointment.dto.AppointmentResponse;
import com.crm.contact.ContactRepository;
import com.crm.deal.DealRepository;
import com.crm.event.CrmEvent;
import com.crm.user.User;
import com.crm.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repository;
    private final ContactRepository contactRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher events;

    public List<AppointmentResponse> calendar(LocalDateTime from, LocalDateTime to) {
        return repository.findByStartAtBetweenOrderByStartAtAsc(from, to).stream().map(this::toResponse).toList();
    }

    public List<AppointmentResponse> board(AppointmentStatus status, UUID assignedTo) {
        return repository.search(status, assignedTo).stream().map(this::toResponse).toList();
    }

    public AppointmentResponse findById(UUID id) {
        return toResponse(get(id));
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest req) {
        User current = currentUser();
        Appointment a = Appointment.builder()
                .title(req.title())
                .description(req.description())
                .type(req.type() != null ? req.type() : AppointmentType.MEETING)
                .status(req.status() != null ? req.status() : AppointmentStatus.SCHEDULED)
                .startAt(req.startAt())
                .endAt(req.endAt())
                .allDay(Boolean.TRUE.equals(req.allDay()))
                .location(req.location())
                .meetingUrl(req.meetingUrl())
                .color(req.color() != null ? req.color() : "#6366f1")
                .reminderMinutes(req.reminderMinutes())
                .createdBy(current)
                .build();
        applyRelations(a, req);
        Appointment saved = repository.save(a);
        AppointmentResponse resp = toResponse(saved);
        events.publishEvent(new CrmEvent("appointment.created", saved.getTenantId(), resp));
        return resp;
    }

    @Transactional
    public AppointmentResponse update(UUID id, AppointmentRequest req) {
        Appointment a = get(id);
        if (req.title() != null) a.setTitle(req.title());
        if (req.description() != null) a.setDescription(req.description());
        if (req.type() != null) a.setType(req.type());
        if (req.status() != null) a.setStatus(req.status());
        if (req.startAt() != null) a.setStartAt(req.startAt());
        if (req.endAt() != null) a.setEndAt(req.endAt());
        if (req.allDay() != null) a.setAllDay(req.allDay());
        if (req.location() != null) a.setLocation(req.location());
        if (req.meetingUrl() != null) a.setMeetingUrl(req.meetingUrl());
        if (req.color() != null) a.setColor(req.color());
        if (req.reminderMinutes() != null) a.setReminderMinutes(req.reminderMinutes());
        applyRelations(a, req);
        Appointment saved = repository.save(a);
        AppointmentResponse resp = toResponse(saved);
        events.publishEvent(new CrmEvent("appointment.updated", saved.getTenantId(), resp));
        return resp;
    }

    @Transactional
    public AppointmentResponse changeStatus(UUID id, AppointmentStatus status) {
        Appointment a = get(id);
        a.setStatus(status);
        Appointment saved = repository.save(a);
        AppointmentResponse resp = toResponse(saved);
        String event = status == AppointmentStatus.CANCELLED ? "appointment.cancelled" : "appointment.status_changed";
        events.publishEvent(new CrmEvent(event, saved.getTenantId(), resp));
        return resp;
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private void applyRelations(Appointment a, AppointmentRequest req) {
        if (req.contactId() != null) a.setContact(contactRepository.findById(req.contactId()).orElse(null));
        if (req.dealId() != null) a.setDeal(dealRepository.findById(req.dealId()).orElse(null));
        if (req.assignedTo() != null) a.setAssignedTo(userRepository.findById(req.assignedTo()).orElse(null));
    }

    private Appointment get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
    }

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User u) return u;
        return null;
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(), a.getTitle(), a.getDescription(), a.getType(), a.getStatus(),
                a.getStartAt(), a.getEndAt(), a.isAllDay(), a.getLocation(), a.getMeetingUrl(), a.getColor(),
                a.getContact() != null ? a.getContact().getId() : null,
                a.getContact() != null ? a.getContact().getName() : null,
                a.getDeal() != null ? a.getDeal().getId() : null,
                a.getDeal() != null ? a.getDeal().getTitle() : null,
                a.getAssignedTo() != null ? a.getAssignedTo().getId() : null,
                a.getAssignedTo() != null ? a.getAssignedTo().getName() : null,
                a.getReminderMinutes(), a.getCreatedAt()
        );
    }
}
