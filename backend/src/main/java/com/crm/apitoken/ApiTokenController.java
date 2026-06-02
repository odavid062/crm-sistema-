package com.crm.apitoken;

import com.crm.apitoken.dto.ApiTokenResponse;
import com.crm.apitoken.dto.CreateApiTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/api-tokens")
@RequiredArgsConstructor
@Tag(name = "API Tokens", description = "Tokens Bearer para integração externa (n8n, sistemas próprios)")
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    @GetMapping
    @Operation(summary = "Listar tokens do tenant")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ApiTokenResponse>> findAll() {
        return ResponseEntity.ok(apiTokenService.findAll());
    }

    @PostMapping
    @Operation(summary = "Gerar novo token (mostrado uma única vez)")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiTokenResponse> create(@Valid @RequestBody CreateApiTokenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apiTokenService.create(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revogar token")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        apiTokenService.revoke(id);
        return ResponseEntity.noContent().build();
    }
}
