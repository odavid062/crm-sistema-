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
@RequestMapping("/api/queues")
@RequiredArgsConstructor
@Tag(name = "Filas", description = "Filas / departamentos de atendimento")
public class QueueController {

    private final QueueService service;

    @GetMapping
    @Operation(summary = "Listar filas ativas")
    public ResponseEntity<List<Queue>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    @Operation(summary = "Criar fila")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Queue> create(@RequestBody Queue q) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(q));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fila")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Queue> update(@PathVariable UUID id, @RequestBody Queue q) {
        return ResponseEntity.ok(service.update(id, q));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar fila")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
