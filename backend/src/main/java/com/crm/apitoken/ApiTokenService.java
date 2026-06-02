package com.crm.apitoken;

import com.crm.apitoken.dto.ApiTokenResponse;
import com.crm.apitoken.dto.CreateApiTokenRequest;
import com.crm.common.TenantContext;
import com.crm.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiTokenService {

    private static final String PREFIX = "crm_live_";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ApiTokenRepository repository;

    public List<ApiTokenResponse> findAll() {
        UUID tenantId = TenantContext.getTenantId();
        return repository.findByTenantId(tenantId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ApiTokenResponse create(CreateApiTokenRequest request) {
        String plainToken = PREFIX + randomString(40);
        String hash = sha256(plainToken);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ApiToken token = ApiToken.builder()
                .tenantId(TenantContext.getTenantId())
                .name(request.name())
                .tokenHash(hash)
                .tokenPrefix(plainToken.substring(0, 16) + "...")
                .scopes(request.scopes() != null ? request.scopes().toArray(new String[0]) : new String[0])
                .expiresAt(request.expiresAt())
                .createdBy(user.getId())
                .active(true)
                .build();
        token = repository.save(token);

        ApiTokenResponse response = toResponse(token);
        // Retorna o token em texto puro APENAS nesta resposta de criação.
        return new ApiTokenResponse(
                response.id(), response.name(), response.tokenPrefix(), response.scopes(),
                response.lastUsedAt(), response.expiresAt(), response.active(), response.createdAt(),
                plainToken
        );
    }

    @Transactional
    public void revoke(UUID id) {
        ApiToken token = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Token não encontrado"));
        if (!token.getTenantId().equals(TenantContext.getTenantId())) {
            throw new RuntimeException("Token não pertence ao seu tenant");
        }
        token.setActive(false);
        repository.save(token);
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash", e);
        }
    }

    private String randomString(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }

    private ApiTokenResponse toResponse(ApiToken t) {
        return new ApiTokenResponse(
                t.getId(), t.getName(), t.getTokenPrefix(), t.getScopes(),
                t.getLastUsedAt(), t.getExpiresAt(), t.isActive(), t.getCreatedAt(), null
        );
    }
}
