package com.crm.whatsapp;

import com.crm.common.TenantContext;
import com.crm.whatsapp.dto.SendMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
@Tag(name = "WhatsApp (UazAPI)", description = "Integração com WhatsApp via UazAPI")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @GetMapping("/conversations")
    @Operation(summary = "Listar conversas")
    public ResponseEntity<Page<WhatsAppConversation>> getConversations(
            @RequestParam(required = false) ConversationStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(whatsAppService.findConversations(status, pageable));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "Buscar mensagens de uma conversa")
    public ResponseEntity<Page<WhatsAppMessage>> getMessages(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(whatsAppService.findMessages(id, pageable));
    }

    @PostMapping("/conversations/{id}/send")
    @Operation(summary = "Enviar mensagem")
    public ResponseEntity<WhatsAppMessage> sendMessage(@PathVariable UUID id, @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(whatsAppService.sendMessage(id, request));
    }

    @PatchMapping("/conversations/{id}/status")
    @Operation(summary = "Atualizar status da conversa")
    public ResponseEntity<WhatsAppConversation> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(whatsAppService.updateStatus(id, ConversationStatus.valueOf(body.get("status"))));
    }

    @PostMapping("/webhook/receive/{tenantId}")
    @Operation(summary = "Webhook para receber mensagens do UazAPI (URL única por tenant)")
    public ResponseEntity<Void> receiveWebhook(@PathVariable UUID tenantId, @RequestBody Map<String, Object> payload) {
        try {
            // Webhook é público: o tenant é identificado pela URL exclusiva configurada no UazAPI.
            TenantContext.setTenantId(tenantId);
            whatsAppService.processIncomingWebhook(payload);
            return ResponseEntity.ok().build();
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Status da instância WhatsApp")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(whatsAppService.getInstanceStatus());
    }

    @GetMapping("/qrcode")
    @Operation(summary = "QR Code para conectar WhatsApp")
    public ResponseEntity<Map<String, Object>> getQrCode() {
        return ResponseEntity.ok(whatsAppService.getQrCode());
    }
}
