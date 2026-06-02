package com.crm.plan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Tag(name = "Planos", description = "Planos do SaaS (gerenciados pelo super admin)")
public class PlanController {

    private final PlanRepository planRepository;

    @GetMapping
    @Operation(summary = "Listar planos ativos")
    public ResponseEntity<List<Plan>> findAll() {
        return ResponseEntity.ok(planRepository.findByActiveTrue());
    }

    @PostMapping
    @Operation(summary = "Criar plano")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Plan> create(@RequestBody Plan plan) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planRepository.save(plan));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar plano")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Plan> update(@PathVariable UUID id, @RequestBody Plan updated) {
        return planRepository.findById(id).map(plan -> {
            plan.setName(updated.getName());
            plan.setDescription(updated.getDescription());
            plan.setPrice(updated.getPrice());
            plan.setMaxUsers(updated.getMaxUsers());
            plan.setMaxChannels(updated.getMaxChannels());
            plan.setMaxQueues(updated.getMaxQueues());
            plan.setMaxContacts(updated.getMaxContacts());
            plan.setActive(updated.isActive());
            return ResponseEntity.ok(planRepository.save(plan));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar plano")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        planRepository.findById(id).ifPresent(plan -> {
            plan.setActive(false);
            planRepository.save(plan);
        });
        return ResponseEntity.noContent().build();
    }
}
