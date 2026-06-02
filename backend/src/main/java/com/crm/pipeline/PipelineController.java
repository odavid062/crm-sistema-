package com.crm.pipeline;

import com.crm.pipeline.dto.PipelineRequest;
import com.crm.pipeline.dto.PipelineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pipelines")
@RequiredArgsConstructor
@Tag(name = "Pipelines", description = "Gerenciamento de pipelines de vendas")
public class PipelineController {

    private final PipelineService pipelineService;

    @GetMapping
    @Operation(summary = "Listar pipelines ativos")
    public ResponseEntity<List<PipelineResponse>> findAll() {
        return ResponseEntity.ok(pipelineService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pipeline por ID")
    public ResponseEntity<PipelineResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(pipelineService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar pipeline")
    public ResponseEntity<PipelineResponse> create(@Valid @RequestBody PipelineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pipelineService.create(request));
    }

    @PostMapping("/{id}/stages")
    @Operation(summary = "Adicionar etapa ao pipeline")
    public ResponseEntity<PipelineResponse> addStage(@PathVariable UUID id, @RequestBody PipelineRequest.StageRequest request) {
        return ResponseEntity.ok(pipelineService.addStage(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar pipeline")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        pipelineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
