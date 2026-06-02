package com.crm.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente da UazAPI v2.
 * Autenticação: header "token" identifica a instância (não vai no path).
 * Base URL configurável (ex.: https://free.uazapi.com ou servidor próprio).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UazApiClient {

    @Value("${uazapi.base-url}")
    private String baseUrl;

    @Value("${uazapi.token}")
    private String token;

    private WebClient buildClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("token", token)
                .build();
    }

    /** Envia mensagem de texto. UazAPI v2: POST /send/text { number, text }. */
    public Map<String, Object> sendText(String to, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("number", normalize(to));
        body.put("text", message);
        return post("/send/text", body, "enviar texto");
    }

    /** Envia mídia. UazAPI v2: POST /send/media { number, type, file, text }. */
    public Map<String, Object> sendMedia(String to, String mediaUrl, String caption, String mediaType) {
        Map<String, Object> body = new HashMap<>();
        body.put("number", normalize(to));
        body.put("type", mediaType != null ? mediaType : "image");
        body.put("file", mediaUrl);
        body.put("text", caption != null ? caption : "");
        return post("/send/media", body, "enviar mídia");
    }

    /** Status da instância (conectado/desconectado). GET /instance/status. */
    public Map<String, Object> getInstanceStatus() {
        return buildClient().get()
                .uri("/instance/status")
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Erro ao verificar status UazAPI: {}", e.getMessage()))
                .onErrorReturn(Map.of("connected", false, "error", "indisponível"))
                .block();
    }

    /** Inicia conexão e retorna QR Code (campo qrcode/base64). POST /instance/connect. */
    public Map<String, Object> getQrCode() {
        return post("/instance/connect", new HashMap<>(), "obter QR Code");
    }

    private Map<String, Object> post(String path, Map<String, Object> body, String acao) {
        return buildClient().post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("Erro ao {} UazAPI: {}", acao, e.getMessage()))
                .block();
    }

    /** Remove caracteres não numéricos do telefone (UazAPI espera só dígitos com DDI). */
    private String normalize(String phone) {
        return phone == null ? null : phone.replaceAll("[^0-9]", "");
    }
}
