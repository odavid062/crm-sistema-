package com.crm.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@Slf4j
public class AsaasClient {

    @Value("${asaas.base-url}")
    private String baseUrl;

    @Value("${asaas.api-key}")
    private String apiKey;

    private WebClient buildClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("access_token", apiKey)
                .build();
    }

    public Map<String, Object> createCustomer(Map<String, Object> customerData) {
        return buildClient().post()
                .uri("/customers")
                .bodyValue(customerData)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Erro ao criar cliente Asaas: {}", e.getMessage()))
                .block();
    }

    public Map<String, Object> createCharge(Map<String, Object> chargeData) {
        return buildClient().post()
                .uri("/payments")
                .bodyValue(chargeData)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Erro ao criar cobrança Asaas: {}", e.getMessage()))
                .block();
    }

    public Map<String, Object> getPayment(String asaasId) {
        return buildClient().get()
                .uri("/payments/{id}", asaasId)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Erro ao buscar pagamento Asaas: {}", e.getMessage()))
                .block();
    }

    public Map<String, Object> cancelPayment(String asaasId) {
        return buildClient().delete()
                .uri("/payments/{id}", asaasId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> createSubscription(Map<String, Object> subscriptionData) {
        return buildClient().post()
                .uri("/subscriptions")
                .bodyValue(subscriptionData)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Erro ao criar assinatura Asaas: {}", e.getMessage()))
                .block();
    }

    public Map<String, Object> getPixQrCode(String asaasId) {
        return buildClient().get()
                .uri("/payments/{id}/pixQrCode", asaasId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> getBankSlipBarCode(String asaasId) {
        return buildClient().get()
                .uri("/payments/{id}/bankSlipBarCode", asaasId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
