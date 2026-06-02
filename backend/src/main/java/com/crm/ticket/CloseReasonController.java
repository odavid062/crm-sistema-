package com.crm.ticket;

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
@RequestMapping("/api/close-reasons")
@RequiredArgsConstructor
@Tag(name = "Motivos de Encerramento", description = "Motivos de fechamento de tickets")
public class CloseReasonController {

    private final CloseReasonService service;

    @GetMapping
    @Operation(summary = "Listar motivos ativos")
    public ResponseEntity<List<CloseReason>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    @Operation(summary = "Criar motivo")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<CloseReason> create(@RequestBody CloseReason r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar motivo")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
