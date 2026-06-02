package com.crm.payment;

import com.crm.payment.dto.CreatePaymentRequest;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Pagamentos (Asaas)", description = "Integração com Asaas para cobranças e pagamentos")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Listar pagamentos")
    public ResponseEntity<Page<Payment>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID contactId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(paymentService.findAll(status, contactId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID")
    public ResponseEntity<Payment> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar cobrança no Asaas")
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    @GetMapping("/{id}/pix")
    @Operation(summary = "Obter QR Code Pix")
    public ResponseEntity<Map<String, Object>> getPixQrCode(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPixQrCode(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar cobrança")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        paymentService.cancelPayment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook para notificações do Asaas")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        paymentService.processWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
