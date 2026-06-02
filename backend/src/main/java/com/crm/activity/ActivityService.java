package com.crm.activity;

import com.crm.activity.dto.ActivityRequest;
import com.crm.activity.dto.ActivityUpdateRequest;
import com.crm.company.CompanyRepository;
import com.crm.contact.ContactRepository;
import com.crm.deal.DealRepository;
import com.crm.user.User;
import com.crm.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;

    public Page<Activity> findByContact(UUID contactId, Pageable pageable) {
        return activityRepository.findByContactIdOrderByDueDateAsc(contactId, pageable);
    }

    public Page<Activity> findByDeal(UUID dealId, Pageable pageable) {
        return activityRepository.findByDealIdOrderByDueDateAsc(dealId, pageable);
    }

    public Page<Activity> findMyActivities(ActivityStatus status, Pageable pageable) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return activityRepository.findByAssignedToIdAndStatusOrderByDueDateAsc(user.getId(), status, pageable);
    }

    /** Agenda: lista atividades por período/status/tipo/responsável (para Kanban e Calendário). */
    public List<Activity> agenda(LocalDateTime from, LocalDateTime to,
                                 ActivityStatus status, ActivityType type, UUID assignedTo) {
        return activityRepository.searchAgenda(from, to, status, type, assignedTo);
    }

    /** Atualização parcial — usada ao mover no Kanban (status) ou reagendar no Calendário (dueDate). */
    @Transactional
    public Activity update(UUID id, ActivityUpdateRequest req) {
        Activity a = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));
        if (req.title() != null) a.setTitle(req.title());
        if (req.type() != null) a.setType(req.type());
        if (req.description() != null) a.setDescription(req.description());
        if (req.priority() != null) a.setPriority(req.priority());
        if (req.dueDate() != null) a.setDueDate(req.dueDate());
        if (req.durationMinutes() != null) a.setDurationMinutes(req.durationMinutes());
        if (req.status() != null) {
            a.setStatus(req.status());
            a.setCompletedAt(req.status() == ActivityStatus.COMPLETED ? LocalDateTime.now() : null);
        }
        if (req.contactId() != null) a.setContact(contactRepository.findById(req.contactId()).orElse(null));
        if (req.dealId() != null) a.setDeal(dealRepository.findById(req.dealId()).orElse(null));
        if (req.assignedTo() != null) a.setAssignedTo(userRepository.findById(req.assignedTo()).orElse(null));
        return activityRepository.save(a);
    }

    @Transactional
    public Activity create(ActivityRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Activity activity = Activity.builder()
                .title(request.title())
                .type(request.type())
                .description(request.description())
                .status(request.status() != null ? request.status() : ActivityStatus.PENDING)
                .priority(request.priority() != null ? request.priority() : "MEDIUM")
                .dueDate(request.dueDate())
                .durationMinutes(request.durationMinutes())
                .createdBy(currentUser)
                .build();
        if (request.contactId() != null) activity.setContact(contactRepository.findById(request.contactId()).orElse(null));
        if (request.companyId() != null) activity.setCompany(companyRepository.findById(request.companyId()).orElse(null));
        if (request.dealId() != null) activity.setDeal(dealRepository.findById(request.dealId()).orElse(null));
        if (request.assignedTo() != null) activity.setAssignedTo(userRepository.findById(request.assignedTo()).orElse(null));
        return activityRepository.save(activity);
    }

    @Transactional
    public Activity complete(UUID id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));
        activity.setStatus(ActivityStatus.COMPLETED);
        activity.setCompletedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    @Transactional
    public void delete(UUID id) {
        activityRepository.deleteById(id);
    }
}
