package com.crm.company;

import com.crm.company.dto.CompanyRequest;
import com.crm.company.dto.CompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Gerenciamento de empresas")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "Listar empresas")
    public ResponseEntity<Page<CompanyResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String industry,
            Pageable pageable
    ) {
        return ResponseEntity.ok(companyService.search(search, industry, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar empresa por ID")
    public ResponseEntity<CompanyResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar empresa")
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar empresa")
    public ResponseEntity<CompanyResponse> update(@PathVariable UUID id, @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir empresa")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
