package com.crm.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de domínio publicado via Spring ApplicationEventPublisher
 * sempre que algo relevante acontece no CRM (contato criado, deal mudou de
 * etapa, pagamento recebido, etc.). Um único listener traduz isso em
 * webhook HTTP de saída — desacoplando os services dos canais externos.
 *
 * <p>Convenção de nomes de evento: minúsculas, separadas por ponto:
 * {@code contact.created}, {@code deal.stage_changed}, {@code payment.received},
 * {@code whatsapp.message.received}, etc.
 */
public record CrmEvent(String name, UUID tenantId, Object data, Instant occurredAt) {

    public CrmEvent(String name, UUID tenantId, Object data) {
        this(name, tenantId, data, Instant.now());
    }
}
