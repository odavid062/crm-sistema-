package com.crm.apitoken;

import com.crm.common.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Autentica requisições externas que usam um API Token (Bearer crm_live_...).
 * Diferencia de JWT pelo prefixo: tokens de API começam com "crm_". Tokens JWT não.
 * Ao validar, define o tenant no contexto para isolamento de dados.
 */
@Component
@RequiredArgsConstructor
public class ApiTokenAuthFilter extends OncePerRequestFilter {

    public static final String API_TOKEN_PREFIX = "crm_";
    private final ApiTokenRepository apiTokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String token = authHeader.substring(7);
        // Só trata como API token se tiver o prefixo. Caso contrário deixa o JwtAuthFilter cuidar.
        if (!token.startsWith(API_TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String hash = ApiTokenService.sha256(token);
        apiTokenRepository.findByTokenHashAndActiveTrue(hash).ifPresent(apiToken -> {
            boolean expired = apiToken.getExpiresAt() != null && apiToken.getExpiresAt().isBefore(LocalDateTime.now());
            if (!expired) {
                TenantContext.setTenantId(apiToken.getTenantId());
                var auth = new UsernamePasswordAuthenticationToken(
                        "api-token:" + apiToken.getId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_API"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                apiToken.setLastUsedAt(LocalDateTime.now());
                apiTokenRepository.save(apiToken);
            }
        });

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
