package com.crm.ticket;

import com.crm.ticket.dto.CloseTicketRequest;
import com.crm.ticket.dto.TicketNoteResponse;
import com.crm.ticket.dto.TicketRequest;
import com.crm.ticket.dto.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Atendimentos: filas, status, atribuição, notas, tags, encerramento e CSAT")
public class TicketController {

    private final TicketService service;

    @GetMapping
    @Operation(summary = "Listar tickets (filtros: status, fila, agente, canal)")
    public ResponseEntity<List<TicketResponse>> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) UUID queueId,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) String channel) {
        return ResponseEntity.ok(service.search(status, queueId, assignedTo, channel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes do ticket")
    public ResponseEntity<TicketResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/{id}/notes")
    @Operation(summary = "Notas internas do ticket")
    public ResponseEntity<List<TicketNoteResponse>> notes(@PathVariable UUID id) {
        return ResponseEntity.ok(service.notes(id));
    }

    @PostMapping
    @Operation(summary = "Abrir ticket")
    public ResponseEntity<TicketResponse> create(@RequestBody TicketRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Aceitar/assumir o ticket (atribui ao usuário atual)")
    public ResponseEntity<TicketResponse> accept(@PathVariable UUID id) {
        return ResponseEntity.ok(service.accept(id));
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Atribuir a um agente")
    public ResponseEntity<TicketResponse> assign(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String u = body.get("userId");
        return ResponseEntity.ok(service.assign(id, u != null ? UUID.fromString(u) : null));
    }

    @PatchMapping("/{id}/transfer")
    @Operation(summary = "Transferir de fila")
    public ResponseEntity<TicketResponse> transfer(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String q = body.get("queueId");
        return ResponseEntity.ok(service.transfer(id, q != null ? UUID.fromString(q) : null));
    }

    @PostMapping("/{id}/tags/{tagId}")
    @Operation(summary = "Adicionar tag")
    public ResponseEntity<TicketResponse> addTag(@PathVariable UUID id, @PathVariable UUID tagId) {
        return ResponseEntity.ok(service.addTag(id, tagId));
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    @Operation(summary = "Remover tag")
    public ResponseEntity<TicketResponse> removeTag(@PathVariable UUID id, @PathVariable UUID tagId) {
        return ResponseEntity.ok(service.removeTag(id, tagId));
    }

    @PostMapping("/{id}/notes")
    @Operation(summary = "Adicionar nota interna")
    public ResponseEntity<TicketNoteResponse> addNote(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addNote(id, body.get("content")));
    }

    @PatchMapping("/{id}/ai")
    @Operation(summary = "Ligar/desligar o agente de IA (n8n) neste ticket")
    public ResponseEntity<TicketResponse> toggleAi(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(service.toggleAi(id, Boolean.TRUE.equals(body.get("enabled"))));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Encerrar ticket (com motivo e CSAT opcional)")
    public ResponseEntity<TicketResponse> close(@PathVariable UUID id, @RequestBody CloseTicketRequest req) {
        return ResponseEntity.ok(service.close(id, req));
    }

    @PatchMapping("/{id}/reopen")
    @Operation(summary = "Reabrir ticket")
    public ResponseEntity<TicketResponse> reopen(@PathVariable UUID id) {
        return ResponseEntity.ok(service.reopen(id));
    }
}
