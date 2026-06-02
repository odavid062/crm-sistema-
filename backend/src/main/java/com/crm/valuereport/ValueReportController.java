package com.crm.valuereport;

import com.crm.common.TenantContext;
import com.crm.valuereport.dto.GenerateReportRequest;
import com.crm.valuereport.dto.ReportSettingsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/value-reports")
@RequiredArgsConstructor
@Tag(name = "Relatório de Valor", description = "Relatórios de valor gerado (ROI, automação, comercial)")
public class ValueReportController {

    private final ValueReportService service;

    @GetMapping
    @Operation(summary = "Histórico de relatórios do tenant")
    public ResponseEntity<List<ValueReport>> list() {
        return ResponseEntity.ok(service.list(TenantContext.getTenantId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar relatório por ID")
    public ResponseEntity<ValueReport> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/preview")
    @Operation(summary = "Prévia ao vivo do período atual (não persiste)")
    public ResponseEntity<ValueReport> preview(
            @RequestParam(defaultValue = "MONTHLY") ReportPeriodType periodType) {
        return ResponseEntity.ok(service.preview(TenantContext.getTenantId(), periodType));
    }

    @PostMapping("/generate")
    @Operation(summary = "Gerar e salvar relatório de um período")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ValueReport> generate(@RequestBody GenerateReportRequest req) {
        UUID tenantId = TenantContext.getTenantId();
        ReportPeriodType type = req.periodType() != null ? req.periodType() : ReportPeriodType.MONTHLY;
        LocalDate start = req.periodStart();
        LocalDate end = req.periodEnd();
        if (start == null || end == null) {
            LocalDate[] range = service.computeRange(type, LocalDate.now());
            start = range[0];
            end = range[1];
        }
        return ResponseEntity.ok(service.generateAndSave(tenantId, type, start, end));
    }

    @GetMapping("/settings")
    @Operation(summary = "Configurações de ROI do tenant")
    public ResponseEntity<ReportSettings> getSettings() {
        return ResponseEntity.ok(service.getOrCreateSettings(TenantContext.getTenantId()));
    }

    @PutMapping("/settings")
    @Operation(summary = "Atualizar configurações de ROI")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ReportSettings> updateSettings(@RequestBody ReportSettingsDto dto) {
        return ResponseEntity.ok(service.updateSettings(TenantContext.getTenantId(), dto));
    }
}
