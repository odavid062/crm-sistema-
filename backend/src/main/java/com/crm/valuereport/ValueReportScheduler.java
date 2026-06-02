package com.crm.valuereport;

import com.crm.tenant.Tenant;
import com.crm.tenant.TenantRepository;
import com.crm.tenant.TenantStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Geração automática de relatórios de valor.
 * Roda fora de contexto de requisição — por isso usa tenantId explícito
 * (o ValueReportService faz todas as agregações com tenant explícito).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValueReportScheduler {

    private final ValueReportService valueReportService;
    private final TenantRepository tenantRepository;

    /** Todo dia 1, às 06:00 — fecha o MÊS anterior. */
    @Scheduled(cron = "0 0 6 1 * *", zone = "America/Sao_Paulo")
    public void monthlyClose() {
        runForAllTenants(ReportPeriodType.MONTHLY, LocalDate.now().minusMonths(1));
    }

    /** Dia 1 de Jan/Abr/Jul/Out, às 07:00 — fecha o TRIMESTRE anterior. */
    @Scheduled(cron = "0 0 7 1 1,4,7,10 *", zone = "America/Sao_Paulo")
    public void quarterlyClose() {
        runForAllTenants(ReportPeriodType.QUARTERLY, LocalDate.now().minusMonths(1));
    }

    /** Dia 1 de Janeiro, às 08:00 — fecha o ANO anterior. */
    @Scheduled(cron = "0 0 8 1 1 *", zone = "America/Sao_Paulo")
    public void annualClose() {
        runForAllTenants(ReportPeriodType.ANNUAL, LocalDate.now().minusMonths(1));
    }

    private void runForAllTenants(ReportPeriodType type, LocalDate ref) {
        List<Tenant> tenants = tenantRepository.findAll().stream()
                .filter(t -> t.getStatus() == TenantStatus.ACTIVE || t.getStatus() == TenantStatus.TRIAL)
                .toList();
        LocalDate[] range = valueReportService.computeRange(type, ref);
        log.info("Gerando relatórios {} para {} tenants ({} a {})", type, tenants.size(), range[0], range[1]);
        for (Tenant t : tenants) {
            try {
                valueReportService.generateAndSave(t.getId(), type, range[0], range[1]);
            } catch (Exception e) {
                log.error("Falha ao gerar relatório {} do tenant {}: {}", type, t.getId(), e.getMessage());
            }
        }
    }
}
