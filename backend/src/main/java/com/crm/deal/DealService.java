package com.crm.deal;

import com.crm.company.CompanyRepository;
import com.crm.contact.ContactRepository;
import com.crm.deal.dto.DealRequest;
import com.crm.deal.dto.DealResponse;
import com.crm.event.CrmEvent;
import com.crm.pipeline.PipelineRepository;
import com.crm.pipeline.PipelineStageRepository;
import com.crm.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository stageRepository;
    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher events;

    public Page<DealResponse> search(UUID pipelineId, UUID stageId, DealStatus status, UUID ownerId, String search, Pageable pageable) {
        return dealRepository.search(pipelineId, stageId, status, ownerId, search, pageable).map(this::toResponse);
    }

    public DealResponse findById(UUID id) {
        return toResponse(dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal não encontrado")));
    }

    @Transactional
    public DealResponse create(DealRequest request) {
        Deal deal = Deal.builder()
                .title(request.title())
                .value(request.value())
                .stage(stageRepository.findById(request.stageId())
                        .orElseThrow(() -> new RuntimeException("Etapa não encontrada")))
                .pipeline(pipelineRepository.findById(request.pipelineId())
                        .orElseThrow(() -> new RuntimeException("Pipeline não encontrado")))
                .priority(request.priority() != null ? request.priority() : "MEDIUM")
                .expectedCloseDate(request.expectedCloseDate())
                .description(request.description())
                .status(DealStatus.OPEN)
                .build();
        if (request.contactId() != null) deal.setContact(contactRepository.findById(request.contactId()).orElse(null));
        if (request.companyId() != null) deal.setCompany(companyRepository.findById(request.companyId()).orElse(null));
        if (request.ownerId() != null) deal.setOwner(userRepository.findById(request.ownerId()).orElse(null));
        Deal saved = dealRepository.save(deal);
        DealResponse response = toResponse(saved);
        events.publishEvent(new CrmEvent("deal.created", saved.getTenantId(), response));
        return response;
    }

    @Transactional
    public DealResponse update(UUID id, DealRequest request) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal não encontrado"));
        if (request.title() != null) deal.setTitle(request.title());
        if (request.value() != null) deal.setValue(request.value());
        if (request.stageId() != null) deal.setStage(stageRepository.findById(request.stageId()).orElseThrow());
        if (request.priority() != null) deal.setPriority(request.priority());
        if (request.expectedCloseDate() != null) deal.setExpectedCloseDate(request.expectedCloseDate());
        if (request.description() != null) deal.setDescription(request.description());
        Deal saved = dealRepository.save(deal);
        DealResponse response = toResponse(saved);
        events.publishEvent(new CrmEvent("deal.updated", saved.getTenantId(), response));
        return response;
    }

    @Transactional
    public DealResponse updateStatus(UUID id, DealStatus status, String lostReason) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal não encontrado"));
        DealStatus oldStatus = deal.getStatus();
        deal.setStatus(status);
        if (status == DealStatus.WON || status == DealStatus.LOST) {
            deal.setClosedAt(LocalDateTime.now());
        }
        if (status == DealStatus.LOST && lostReason != null) {
            deal.setLostReason(lostReason);
        }
        Deal saved = dealRepository.save(deal);
        DealResponse response = toResponse(saved);
        if (oldStatus != status) {
            if (status == DealStatus.WON) {
                events.publishEvent(new CrmEvent("deal.won", saved.getTenantId(), response));
            } else if (status == DealStatus.LOST) {
                events.publishEvent(new CrmEvent("deal.lost", saved.getTenantId(), response));
            }
        }
        return response;
    }

    @Transactional
    public DealResponse moveStage(UUID id, UUID stageId) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal não encontrado"));
        UUID fromStageId = deal.getStage().getId();
        String fromStageName = deal.getStage().getName();
        deal.setStage(stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Etapa não encontrada")));
        Deal saved = dealRepository.save(deal);
        DealResponse response = toResponse(saved);
        events.publishEvent(new CrmEvent("deal.stage_changed", saved.getTenantId(),
                Map.of("dealId", saved.getId(), "fromStageId", fromStageId, "fromStage", fromStageName,
                       "toStageId", stageId, "toStage", saved.getStage().getName(), "deal", response)));
        return response;
    }

    @Transactional
    public void delete(UUID id) {
        dealRepository.deleteById(id);
    }

    public DealResponse toResponse(Deal d) {
        return new DealResponse(
                d.getId(), d.getTitle(), d.getValue(), d.getCurrency(),
                d.getStage().getId(), d.getStage().getName(), d.getStage().getColor(),
                d.getPipeline().getId(), d.getPipeline().getName(),
                d.getContact() != null ? d.getContact().getId() : null,
                d.getContact() != null ? d.getContact().getName() : null,
                d.getCompany() != null ? d.getCompany().getId() : null,
                d.getCompany() != null ? d.getCompany().getName() : null,
                d.getOwner() != null ? d.getOwner().getId() : null,
                d.getOwner() != null ? d.getOwner().getName() : null,
                d.getStatus(), d.getPriority(), d.getExpectedCloseDate(),
                d.getClosedAt(), d.getLostReason(), d.getDescription(), d.getCreatedAt()
        );
    }
}
