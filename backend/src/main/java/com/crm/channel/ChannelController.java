package com.crm.channel;

import com.crm.channel.dto.ChannelRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@Tag(name = "Canais", description = "Canais de atendimento (WhatsApp, Instagram, Facebook, etc.)")
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    @Operation(summary = "Listar canais do tenant")
    public ResponseEntity<List<Channel>> findAll() {
        return ResponseEntity.ok(channelService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar canal por ID")
    public ResponseEntity<Channel> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(channelService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar canal")
    public ResponseEntity<Channel> create(@Valid @RequestBody ChannelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar canal")
    public ResponseEntity<Channel> update(@PathVariable UUID id, @Valid @RequestBody ChannelRequest request) {
        return ResponseEntity.ok(channelService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status de conexão do canal")
    public ResponseEntity<Channel> changeStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(channelService.changeStatus(id, ChannelStatus.valueOf(body.get("status"))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir canal")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
