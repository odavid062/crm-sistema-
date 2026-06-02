package com.crm.auth;

import com.crm.auth.dto.AuthResponse;
import com.crm.auth.dto.LoginRequest;
import com.crm.auth.dto.RegisterRequest;
import com.crm.plan.Plan;
import com.crm.plan.PlanRepository;
import com.crm.tenant.Tenant;
import com.crm.tenant.TenantRepository;
import com.crm.tenant.TenantStatus;
import com.crm.user.User;
import com.crm.user.UserRepository;
import com.crm.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /** Self-signup: cria uma nova empresa (tenant) e o primeiro usuário ADMIN. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        String companyName = request.companyName() != null && !request.companyName().isBlank()
                ? request.companyName()
                : request.name() + " - Empresa";

        Plan freePlan = planRepository.findByActiveTrue().stream()
                .filter(p -> p.getName().equalsIgnoreCase("Free"))
                .findFirst().orElse(null);

        Tenant tenant = Tenant.builder()
                .name(companyName)
                .slug(generateUniqueSlug(companyName))
                .email(request.email())
                .plan(freePlan)
                .status(TenantStatus.TRIAL)
                .trialEndsAt(LocalDate.now().plusDays(14))
                .build();
        tenant = tenantRepository.save(tenant);

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ADMIN)
                .tenantId(tenant.getId())
                .active(true)
                .build();
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Refresh token inválido");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        if (user.getTenantId() != null) claims.put("tenantId", user.getTenantId().toString());

        String token = jwtService.generateToken(claims, user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(token, refreshToken, user.getId(), user.getName(),
                user.getEmail(), user.getRole(), user.getTenantId());
    }

    private String generateUniqueSlug(String base) {
        String slug = base.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        if (slug.isBlank()) slug = "empresa";
        String candidate = slug;
        int counter = 1;
        while (tenantRepository.existsBySlug(candidate)) {
            candidate = slug + "-" + counter++;
        }
        return candidate;
    }
}
