package com.crm.webhook;

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
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks & n8n", description = "Configuração de webhooks e integração com n8n")
public class WebhookController {

    private final WebhookService webhookService;

    @GetMapping
    @Operation(summary = "Listar webhooks configurados")
    public ResponseEntity<List<WebhookConfig>> findAll() {
        return ResponseEntity.ok(webhookService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar webhook por ID")
    public ResponseEntity<WebhookConfig> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(webhookService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar configuração de webhook")
    public ResponseEntity<WebhookConfig> create(@RequestBody WebhookConfig config) {
        return ResponseEntity.status(HttpStatus.CREATED).body(webhookService.create(config));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar webhook")
    public ResponseEntity<WebhookConfig> update(@PathVariable UUID id, @RequestBody WebhookConfig config) {
        return ResponseEntity.ok(webhookService.update(id, config));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir webhook")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        webhookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test/{id}")
    @Operation(summary = "Testar webhook (disparo manual)")
    public ResponseEntity<Void> test(@PathVariable UUID id) {
        WebhookConfig config = webhookService.findById(id);
        webhookService.dispatchTest(config);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/receive/{source}")
    @Operation(summary = "Receber dados do n8n ou outras plataformas")
    public ResponseEntity<Map<String, String>> receive(
            @PathVariable String source,
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.ok(Map.of("status", "received", "source", source));
    }
}
