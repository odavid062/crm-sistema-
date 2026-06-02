package com.crm.auth;

import com.crm.apitoken.ApiTokenAuthFilter;
import com.crm.common.TenantContext;
import com.crm.user.User;
import com.crm.user.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

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
        final String jwt = authHeader.substring(7);

        // API tokens (crm_...) são tratados pelo ApiTokenAuthFilter — ignorar aqui.
        if (jwt.startsWith(ApiTokenAuthFilter.API_TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Token expirado/malformado/assinatura inválida: não autentica e segue.
            // A requisição cai em 401/403 limpo (tratado pelo frontend), em vez de 500.
            String userEmail = null;
            try {
                userEmail = jwtService.extractUsername(jwt);
            } catch (JwtException | IllegalArgumentException ex) {
                logger.debug("JWT inválido/expirado: " + ex.getMessage());
            }

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Popula o contexto de tenant para isolamento automático de dados.
                    if (userDetails instanceof User user) {
                        if (user.getRole() == UserRole.SUPER_ADMIN) {
                            TenantContext.setSuperAdmin(true);
                        } else {
                            TenantContext.setTenantId(user.getTenantId());
                        }
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
