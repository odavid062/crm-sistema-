package com.crm.webhook;

import com.crm.event.CrmEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private static final String HMAC_ALG = "HmacSHA256";

    private final WebhookConfigRepository webhookConfigRepository;
    private final ObjectMapper objectMapper;

    // ---------- CRUD ----------

    public List<WebhookConfig> findAll() {
        return webhookConfigRepository.findAll();
    }

    public WebhookConfig findById(UUID id) {
        return webhookConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Webhook não encontrado"));
    }

    @Transactional
    public WebhookConfig create(WebhookConfig config) {
        return webhookConfigRepository.save(config);
    }

    @Transactional
    public WebhookConfig update(UUID id, WebhookConfig updated) {
        WebhookConfig config = findById(id);
        config.setName(updated.getName());
        config.setUrl(updated.getUrl());
        config.setEvents(updated.getEvents());
        config.setActive(updated.isActive());
        config.setHeaders(updated.getHeaders());
        config.setSecret(updated.getSecret());
        return webhookConfigRepository.save(config);
    }

    @Transactional
    public void delete(UUID id) {
        webhookConfigRepository.deleteById(id);
    }

    // ---------- Dispatch ----------

    /**
     * Dispara um CrmEvent para todos os webhooks ativos do tenant inscritos no evento.
     * Assíncrono: o publisher (services do CRM) não é bloqueado por chamadas HTTP externas.
     */
    @Async
    public void dispatch(CrmEvent event) {
        if (event.tenantId() == null) {
            log.warn("dispatch sem tenantId — ignorando evento {}", event.name());
            return;
        }
        List<WebhookConfig> configs = webhookConfigRepository
                .findActiveByTenantAndEvent(event.tenantId(), event.name());
        if (configs.isEmpty()) return;

        String body = serializePayload(event);
        if (body == null) return;

        for (WebhookConfig config : configs) {
            sendOne(config, event, body);
        }
    }

    /**
     * Atalho para testar manualmente um webhook específico (botão "Testar" na UI).
     */
    public void dispatchTest(WebhookConfig config) {
        CrmEvent demo = new CrmEvent("webhook.test", config.getTenantId(),
                Map.of("message", "Teste de webhook do CRM", "webhookId", config.getId().toString()));
        String body = serializePayload(demo);
        if (body != null) sendOne(config, demo, body);
    }

    private void sendOne(WebhookConfig config, CrmEvent event, String body) {
        try {
            WebClient.Builder builder = WebClient.builder()
                    .baseUrl(config.getUrl())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("X-CRM-Event", event.name())
                    .defaultHeader("X-CRM-Tenant", event.tenantId().toString())
                    .defaultHeader("X-CRM-Delivery", UUID.randomUUID().toString());

            if (config.getSecret() != null && !config.getSecret().isBlank()) {
                String signature = "sha256=" + hmacSha256(body, config.getSecret());
                builder.defaultHeader("X-CRM-Signature", signature);
            }
            if (config.getHeaders() != null) {
                config.getHeaders().forEach(builder::defaultHeader);
            }

            builder.build().post()
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnError(e -> log.error("Erro ao disparar webhook {} ({}): {}",
                            config.getName(), config.getUrl(), e.getMessage()))
                    .doOnSuccess(r -> log.info("Webhook entregue: {} -> {} ({})",
                            event.name(), config.getName(), r.getStatusCode()))
                    .subscribe();
        } catch (Exception e) {
            log.error("Falha ao montar/enviar webhook {}: {}", config.getId(), e.getMessage());
        }
    }

    private String serializePayload(CrmEvent event) {
        // LinkedHashMap mantém ordem para que a assinatura HMAC seja estável.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", event.name());
        body.put("occurredAt", event.occurredAt() != null ? event.occurredAt().toString() : Instant.now().toString());
        body.put("tenantId", event.tenantId().toString());
        body.put("data", event.data());
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar payload do evento {}: {}", event.name(), e.getMessage());
            return null;
        }
    }

    static String hmacSha256(String message, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] digest = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar HMAC", e);
        }
    }
}
