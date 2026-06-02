package com.crm.appointment;

import com.crm.appointment.dto.AppointmentRequest;
import com.crm.appointment.dto.AppointmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Agenda com visão Kanban e Calendário")
public class AppointmentController {

    private final AppointmentService service;

    @GetMapping("/calendar")
    @Operation(summary = "Agendamentos por intervalo (visão calendário)")
    public ResponseEntity<List<AppointmentResponse>> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(service.calendar(from, to));
    }

    @GetMapping
    @Operation(summary = "Agendamentos (visão Kanban / lista)")
    public ResponseEntity<List<AppointmentResponse>> board(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) UUID assignedTo) {
        return ResponseEntity.ok(service.board(status, assignedTo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar agendamento")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar agendamento")
    public ResponseEntity<AppointmentResponse> update(@PathVariable UUID id, @Valid @RequestBody AppointmentRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mover no Kanban / mudar status")
    public ResponseEntity<AppointmentResponse> changeStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.changeStatus(id, AppointmentStatus.valueOf(body.get("status"))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir agendamento")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
