package com.crm.activity;

import com.crm.activity.dto.ActivityRequest;
import com.crm.activity.dto.ActivityUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Atividades", description = "Tarefas, ligações, reuniões e acompanhamentos")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @Operation(summary = "Agenda: listar atividades por período/status/tipo (Kanban e Calendário)")
    public ResponseEntity<List<Activity>> agenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) ActivityStatus status,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) UUID assignedTo
    ) {
        return ResponseEntity.ok(activityService.agenda(from, to, status, type, assignedTo));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar/reagendar atividade (mover no Kanban ou no Calendário)")
    public ResponseEntity<Activity> update(@PathVariable UUID id, @RequestBody ActivityUpdateRequest request) {
        return ResponseEntity.ok(activityService.update(id, request));
    }

    @GetMapping("/my")
    @Operation(summary = "Minhas atividades")
    public ResponseEntity<Page<Activity>> myActivities(
            @RequestParam(required = false) ActivityStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(activityService.findMyActivities(status, pageable));
    }

    @GetMapping("/contact/{contactId}")
    @Operation(summary = "Atividades de um contato")
    public ResponseEntity<Page<Activity>> byContact(@PathVariable UUID contactId, Pageable pageable) {
        return ResponseEntity.ok(activityService.findByContact(contactId, pageable));
    }

    @GetMapping("/deal/{dealId}")
    @Operation(summary = "Atividades de um deal")
    public ResponseEntity<Page<Activity>> byDeal(@PathVariable UUID dealId, Pageable pageable) {
        return ResponseEntity.ok(activityService.findByDeal(dealId, pageable));
    }

    @PostMapping
    @Operation(summary = "Criar atividade")
    public ResponseEntity<Activity> create(@Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.create(request));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Marcar atividade como concluída")
    public ResponseEntity<Activity> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(activityService.complete(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir atividade")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
