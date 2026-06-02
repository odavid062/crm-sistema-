package com.crm.deal;

import com.crm.deal.dto.DealRequest;
import com.crm.deal.dto.DealResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Tag(name = "Deals", description = "Gerenciamento de negociações e oportunidades")
public class DealController {

    private final DealService dealService;

    @GetMapping
    @Operation(summary = "Listar deals com filtros e paginação")
    public ResponseEntity<Page<DealResponse>> search(
            @RequestParam(required = false) UUID pipelineId,
            @RequestParam(required = false) UUID stageId,
            @RequestParam(required = false) DealStatus status,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return ResponseEntity.ok(dealService.search(pipelineId, stageId, status, ownerId, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar deal por ID")
    public ResponseEntity<DealResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(dealService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar deal")
    public ResponseEntity<DealResponse> create(@Valid @RequestBody DealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar deal")
    public ResponseEntity<DealResponse> update(@PathVariable UUID id, @Valid @RequestBody DealRequest request) {
        return ResponseEntity.ok(dealService.update(id, request));
    }

    @PatchMapping("/{id}/stage")
    @Operation(summary = "Mover deal para outra etapa do pipeline (Kanban)")
    public ResponseEntity<DealResponse> moveStage(@PathVariable UUID id, @RequestBody Map<String, UUID> body) {
        return ResponseEntity.ok(dealService.moveStage(id, body.get("stageId")));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do deal (won/lost)")
    public ResponseEntity<DealResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        DealStatus status = DealStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(dealService.updateStatus(id, status, body.get("lostReason")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir deal")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        dealService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
