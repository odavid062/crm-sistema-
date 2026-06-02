package com.crm.event;

import com.crm.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Único ponto que ouve os CrmEvent e repassa para o WebhookService
 * (que decide para quais URLs cadastradas do tenant fazer POST).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookEventListener {

    private final WebhookService webhookService;

    @EventListener
    public void onCrmEvent(CrmEvent event) {
        if (event.tenantId() == null) {
            log.warn("CrmEvent sem tenantId ignorado: {}", event.name());
            return;
        }
        log.debug("CrmEvent recebido: {} (tenant {})", event.name(), event.tenantId());
        webhookService.dispatch(event);
    }
}
