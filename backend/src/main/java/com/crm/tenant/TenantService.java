package com.crm.tenant;

import com.crm.plan.Plan;
import com.crm.plan.PlanRepository;
import com.crm.tenant.dto.CreateTenantRequest;
import com.crm.tenant.dto.TenantResponse;
import com.crm.tenant.dto.UpdateTenantRequest;
import com.crm.user.User;
import com.crm.user.UserRepository;
import com.crm.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<TenantResponse> search(String search, TenantStatus status, Pageable pageable) {
        return tenantRepository.search(search, status, pageable).map(this::toResponse);
    }

    public TenantResponse findById(UUID id) {
        return toResponse(tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado")));
    }

    /** Provisiona um novo tenant junto com seu primeiro usuário ADMIN. */
    @Transactional
    public TenantResponse provision(CreateTenantRequest request) {
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new RuntimeException("Slug já está em uso");
        }
        if (userRepository.existsByEmail(request.adminEmail())) {
            throw new RuntimeException("Email do admin já está cadastrado");
        }

        Plan plan = request.planId() != null
                ? planRepository.findById(request.planId()).orElse(null)
                : null;

        Tenant tenant = Tenant.builder()
                .name(request.name())
                .slug(request.slug().toLowerCase().replaceAll("[^a-z0-9-]", "-"))
                .document(request.document())
                .email(request.email())
                .phone(request.phone())
                .plan(plan)
                .status(TenantStatus.TRIAL)
                .trialEndsAt(LocalDate.now().plusDays(14))
                .build();
        tenant = tenantRepository.save(tenant);

        User admin = User.builder()
                .name(request.adminName())
                .email(request.adminEmail())
                .password(passwordEncoder.encode(request.adminPassword()))
                .role(UserRole.ADMIN)
                .tenantId(tenant.getId())
                .active(true)
                .build();
        userRepository.save(admin);

        return toResponse(tenant);
    }

    @Transactional
    public TenantResponse update(UUID id, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
        if (request.name() != null) tenant.setName(request.name());
        if (request.document() != null) tenant.setDocument(request.document());
        if (request.email() != null) tenant.setEmail(request.email());
        if (request.phone() != null) tenant.setPhone(request.phone());
        if (request.logoUrl() != null) tenant.setLogoUrl(request.logoUrl());
        if (request.status() != null) tenant.setStatus(request.status());
        if (request.dueDate() != null) tenant.setDueDate(request.dueDate());
        if (request.planId() != null) {
            tenant.setPlan(planRepository.findById(request.planId()).orElse(null));
        }
        return toResponse(tenantRepository.save(tenant));
    }

    @Transactional
    public TenantResponse changeStatus(UUID id, TenantStatus status) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
        tenant.setStatus(status);
        return toResponse(tenantRepository.save(tenant));
    }

    @Transactional
    public void delete(UUID id) {
        tenantRepository.deleteById(id);
    }

    private TenantResponse toResponse(Tenant t) {
        return new TenantResponse(
                t.getId(), t.getName(), t.getSlug(), t.getDocument(), t.getEmail(),
                t.getPhone(), t.getLogoUrl(),
                t.getPlan() != null ? t.getPlan().getId() : null,
                t.getPlan() != null ? t.getPlan().getName() : null,
                t.getStatus(), t.getTrialEndsAt(), t.getDueDate(),
                userRepository.countByTenantId(t.getId()), t.getCreatedAt()
        );
    }
}
