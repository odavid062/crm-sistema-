package com.crm.superadmin;

import com.crm.tenant.TenantService;
import com.crm.tenant.TenantStatus;
import com.crm.tenant.dto.CreateTenantRequest;
import com.crm.tenant.dto.TenantResponse;
import com.crm.tenant.dto.UpdateTenantRequest;
import com.crm.tenant.TenantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super Admin", description = "Gestão de tenants (apenas super admin)")
public class SuperAdminController {

    private final TenantService tenantService;
    private final TenantRepository tenantRepository;

    @GetMapping
    @Operation(summary = "Listar todos os tenants")
    public ResponseEntity<Page<TenantResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TenantStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(tenantService.search(search, status, pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas gerais do SaaS")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
                "total", tenantRepository.count(),
                "active", tenantRepository.countByStatus(TenantStatus.ACTIVE),
                "trial", tenantRepository.countByStatus(TenantStatus.TRIAL),
                "suspended", tenantRepository.countByStatus(TenantStatus.SUSPENDED),
                "cancelled", tenantRepository.countByStatus(TenantStatus.CANCELLED)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tenant por ID")
    public ResponseEntity<TenantResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar tenant + admin inicial (provisionamento)")
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.provision(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar tenant")
    public ResponseEntity<TenantResponse> update(@PathVariable UUID id, @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(tenantService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Suspender/ativar/cancelar tenant")
    public ResponseEntity<TenantResponse> changeStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(tenantService.changeStatus(id, TenantStatus.valueOf(body.get("status"))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tenant (e todos os dados)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
