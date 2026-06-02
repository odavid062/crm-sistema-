package com.crm.valuereport;

import com.crm.valuereport.dto.AutomationLogRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/automation-logs")
@RequiredArgsConstructor
@Tag(name = "Logs de Automação", description = "n8n registra aqui cada ação automatizada (alimenta o Relatório de Valor)")
public class AutomationLogController {

    private final ValueReportService valueReportService;

    @PostMapping
    @Operation(summary = "Registrar ação automatizada (chamado pelo n8n via API token)")
    public ResponseEntity<Map<String, Object>> log(@Valid @RequestBody AutomationLogRequest request) {
        AutomationLog saved = valueReportService.logAutomation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "type", saved.getType(),
                "minutesSaved", saved.getMinutesSaved()
        ));
    }
}
