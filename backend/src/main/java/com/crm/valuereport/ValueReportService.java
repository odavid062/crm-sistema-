package com.crm.valuereport;

import com.crm.common.TenantContext;
import com.crm.valuereport.dto.AutomationLogRequest;
import com.crm.valuereport.dto.ReportSettingsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValueReportService {

    private final JdbcTemplate jdbc;
    private final AutomationLogRepository automationLogRepository;
    private final ReportSettingsRepository settingsRepository;
    private final ValueReportRepository reportRepository;

    private static final NumberFormat BRL = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // ===================== Automation logging (n8n -> CRM) =====================

    @Transactional
    public AutomationLog logAutomation(AutomationLogRequest req) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new RuntimeException("Tenant não identificado");

        ReportSettings settings = getOrCreateSettings(tenantId);
        BigDecimal minutes = req.minutesSaved() != null
                ? req.minutesSaved()
                : estimateMinutes(req.type(), settings);

        AutomationLog logEntry = AutomationLog.builder()
                .type(req.type())
                .channel(req.channel())
                .contactId(req.contactId())
                .success(req.success() == null || req.success())
                .minutesSaved(minutes)
                .source(req.source() != null ? req.source() : "n8n")
                .metadata(req.metadata())
                .build();
        logEntry.setTenantId(tenantId);
        return automationLogRepository.save(logEntry);
    }

    private BigDecimal estimateMinutes(AutomationLogType type, ReportSettings s) {
        return switch (type) {
            case MESSAGE_SENT -> s.getMinutesPerMessage();
            case FOLLOWUP, LEAD_REENGAGED, LEAD_RECOVERED -> s.getMinutesPerFollowup();
            case TASK_AUTOMATED -> s.getMinutesPerTask();
            default -> BigDecimal.ZERO;
        };
    }

    // ===================== Settings =====================

    public ReportSettings getOrCreateSettings(UUID tenantId) {
        return settingsRepository.findByTenant(tenantId).orElseGet(() -> {
            ReportSettings s = ReportSettings.builder().build();
            s.setTenantId(tenantId);
            return settingsRepository.save(s);
        });
    }

    @Transactional
    public ReportSettings updateSettings(UUID tenantId, ReportSettingsDto dto) {
        ReportSettings s = getOrCreateSettings(tenantId);
        if (dto.monthlyInvestment() != null) s.setMonthlyInvestment(dto.monthlyInvestment());
        if (dto.currency() != null) s.setCurrency(dto.currency());
        if (dto.minutesPerMessage() != null) s.setMinutesPerMessage(dto.minutesPerMessage());
        if (dto.minutesPerFollowup() != null) s.setMinutesPerFollowup(dto.minutesPerFollowup());
        if (dto.minutesPerTask() != null) s.setMinutesPerTask(dto.minutesPerTask());
        if (dto.hourlyCost() != null) s.setHourlyCost(dto.hourlyCost());
        return settingsRepository.save(s);
    }

    // ===================== Report generation =====================

    /** Calcula período padrão a partir do tipo, usando uma data de referência. */
    public LocalDate[] computeRange(ReportPeriodType type, LocalDate ref) {
        return switch (type) {
            case MONTHLY -> {
                YearMonth ym = YearMonth.from(ref);
                yield new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
            }
            case QUARTERLY -> {
                int q = (ref.getMonthValue() - 1) / 3;
                LocalDate start = LocalDate.of(ref.getYear(), q * 3 + 1, 1);
                yield new LocalDate[]{start, start.plusMonths(3).minusDays(1)};
            }
            case ANNUAL -> new LocalDate[]{
                    LocalDate.of(ref.getYear(), 1, 1), LocalDate.of(ref.getYear(), 12, 31)};
        };
    }

    private int monthsInPeriod(ReportPeriodType type) {
        return switch (type) { case MONTHLY -> 1; case QUARTERLY -> 3; case ANNUAL -> 12; };
    }

    /** Gera o relatório (sem persistir). */
    public ValueReport generate(UUID tenantId, ReportPeriodType type, LocalDate start, LocalDate end) {
        Date s = Date.valueOf(start), e = Date.valueOf(end);
        ReportSettings settings = getOrCreateSettings(tenantId);

        // ---------- Automação ----------
        long execucoes      = count("automation_logs", tenantId, s, e, null);
        long mensagens      = count("automation_logs", tenantId, s, e, "type = 'MESSAGE_SENT'");
        long followups      = count("automation_logs", tenantId, s, e, "type = 'FOLLOWUP'");
        long reengajados    = count("automation_logs", tenantId, s, e, "type = 'LEAD_REENGAGED'");
        long recuperados    = count("automation_logs", tenantId, s, e, "type = 'LEAD_RECOVERED'");
        long tarefas        = count("automation_logs", tenantId, s, e, "type = 'TASK_AUTOMATED'");
        long followupOk     = count("automation_logs", tenantId, s, e, "type = 'FOLLOWUP' AND success = true");
        BigDecimal minutos  = sum("automation_logs", "minutes_saved", tenantId, s, e, null);
        BigDecimal horas    = minutos.divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
        double followupRate = followups > 0 ? round1(followupOk * 100.0 / followups) : 0;

        Map<String, Object> automacao = ordered(
                "execucoes", execucoes, "mensagensEnviadas", mensagens, "followups", followups,
                "leadsReengajados", reengajados, "leadsRecuperados", recuperados,
                "tarefasAutomatizadas", tarefas, "taxaSucessoFollowup", followupRate,
                "horasEconomizadas", horas);

        // ---------- Atendimento ----------
        long conversas   = count("whatsapp_conversations", tenantId, s, e, null);
        long concluidas  = count("whatsapp_conversations", tenantId, s, e, "status = 'RESOLVED'");
        long porHumano   = count("whatsapp_conversations", tenantId, s, e, "assigned_to IS NOT NULL");
        long porIA       = mensagens; // mensagens automáticas como proxy de atendimento por IA

        List<Map<String, Object>> porCanal = new ArrayList<>();
        porCanal.add(ordered("canal", "WhatsApp", "conversas", conversas, "concluidas", concluidas));
        // Demais canais a partir de automation_logs agrupado por channel
        jdbc.query("""
            SELECT channel, COUNT(*) AS qtd FROM automation_logs
            WHERE tenant_id = ? AND created_at::date BETWEEN ? AND ?
              AND channel IS NOT NULL AND channel <> 'WHATSAPP'
            GROUP BY channel ORDER BY qtd DESC
            """, rs -> {
            porCanal.add(ordered("canal", rs.getString("channel"),
                    "conversas", rs.getLong("qtd"), "concluidas", 0L));
        }, tenantId, s, e);

        Map<String, Object> atendimento = ordered(
                "totalConversas", conversas, "concluidas", concluidas,
                "porIA", porIA, "porHumano", porHumano, "porCanal", porCanal);

        // ---------- Comercial ----------
        long leadsRecebidos   = count("contacts", tenantId, s, e, null);
        long leadsQualificados= count("contacts", tenantId, s, e, "status IN ('PROSPECT','CUSTOMER')");
        long leadsDescartados = count("contacts", tenantId, s, e, "status IN ('CHURNED','INACTIVE')");
        long visitas          = count("activities", tenantId, s, e, "type = 'MEETING'");
        long oportunidades    = count("deals", tenantId, s, e, null);
        long vendasFechadas   = countClosed("deals", tenantId, s, e, "status = 'WON'");
        BigDecimal receita    = sumClosed("deals", "value", tenantId, s, e, "status = 'WON'");
        BigDecimal receitaRecebida = sumReceived(tenantId, s, e);
        BigDecimal ticketMedio= vendasFechadas > 0
                ? receita.divide(BigDecimal.valueOf(vendasFechadas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> comercial = ordered(
                "leadsRecebidos", leadsRecebidos, "leadsQualificados", leadsQualificados,
                "leadsDescartados", leadsDescartados, "visitas", visitas,
                "oportunidadesGeradas", oportunidades, "vendasFechadas", vendasFechadas,
                "ticketMedio", ticketMedio, "receitaGerada", receita, "receitaRecebida", receitaRecebida);

        // ---------- ROI ----------
        BigDecimal investimento = settings.getMonthlyInvestment()
                .multiply(BigDecimal.valueOf(monthsInPeriod(type)));
        BigDecimal roi = investimento.signum() > 0
                ? receita.subtract(investimento).multiply(BigDecimal.valueOf(100))
                    .divide(investimento, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Crescimento vs período anterior
        BigDecimal crescimento = null;
        Optional<ValueReport> anterior = reportRepository.findPreviousReport(tenantId, type.name(), start);
        if (anterior.isPresent() && anterior.get().getRevenue().signum() > 0) {
            crescimento = receita.subtract(anterior.get().getRevenue()).multiply(BigDecimal.valueOf(100))
                    .divide(anterior.get().getRevenue(), 2, RoundingMode.HALF_UP);
        }
        // ROI acumulado (histórico + atual)
        BigDecimal[] acc = {BigDecimal.ZERO, BigDecimal.ZERO}; // [receita, investimento]
        for (ValueReport r : reportRepository.findAllByTenant(tenantId)) {
            acc[0] = acc[0].add(r.getRevenue());
            acc[1] = acc[1].add(r.getInvestment());
        }
        acc[0] = acc[0].add(receita);
        acc[1] = acc[1].add(investimento);
        BigDecimal roiAcumulado = acc[1].signum() > 0
                ? acc[0].subtract(acc[1]).multiply(BigDecimal.valueOf(100)).divide(acc[1], 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> roiBlock = ordered(
                "investimento", investimento, "receita", receita, "roiPercentual", roi,
                "roiAcumuladoPercentual", roiAcumulado,
                "crescimentoVsAnterior", crescimento);

        Map<String, Object> metrics = ordered(
                "periodo", ordered("tipo", type.name(), "inicio", start.toString(), "fim", end.toString()),
                "automacao", automacao, "atendimento", atendimento,
                "comercial", comercial, "roi", roiBlock);

        String resumo = buildExecutiveSummary(type, start, end, automacao, comercial, investimento, receita, roi, horas);

        ValueReport report = ValueReport.builder()
                .periodType(type).periodStart(start).periodEnd(end)
                .status("READY").metrics(metrics).executiveSummary(resumo)
                .revenue(receita).investment(investimento).roiPercent(roi).hoursSaved(horas)
                .build();
        report.setTenantId(tenantId);
        return report;
    }

    /** Gera e persiste (upsert por período). */
    @Transactional
    public ValueReport generateAndSave(UUID tenantId, ReportPeriodType type, LocalDate start, LocalDate end) {
        ValueReport novo = generate(tenantId, type, start, end);
        reportRepository.findByTenantAndPeriod(tenantId, type.name(), start).ifPresent(existing -> {
            reportRepository.deleteById(existing.getId());
            reportRepository.flush();
        });
        return reportRepository.save(novo);
    }

    public ValueReport preview(UUID tenantId, ReportPeriodType type) {
        LocalDate[] range = computeRange(type, LocalDate.now());
        return generate(tenantId, type, range[0], range[1]);
    }

    public List<ValueReport> list(UUID tenantId) {
        return reportRepository.findAllByTenant(tenantId);
    }

    public ValueReport getById(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relatório não encontrado"));
    }

    // ===================== Resumo executivo (linguagem natural) =====================

    private String buildExecutiveSummary(ReportPeriodType type, LocalDate start, LocalDate end,
                                         Map<String, Object> automacao, Map<String, Object> comercial,
                                         BigDecimal investimento, BigDecimal receita, BigDecimal roi,
                                         BigDecimal horas) {
        String periodoLabel = switch (type) {
            case MONTHLY -> "o mês de " + mesPorExtenso(start);
            case QUARTERLY -> "o trimestre encerrado em " + mesPorExtenso(end);
            case ANNUAL -> "o ano de " + start.getYear();
        };
        return String.format(Locale.forLanguageTag("pt-BR"),
                "Durante %s, o sistema executou %s follow-ups automáticos, recuperando %s leads que estavam sem interação " +
                "e enviando %s mensagens automáticas. Foram registrados %s novos leads, resultando em %s visitas e %s vendas concluídas. " +
                "A automação economizou aproximadamente %s horas da equipe. O investimento de %s gerou uma receita estimada de %s, " +
                "representando um ROI de %s%%.",
                periodoLabel,
                automacao.get("followups"), automacao.get("leadsRecuperados"), automacao.get("mensagensEnviadas"),
                comercial.get("leadsRecebidos"), comercial.get("visitas"), comercial.get("vendasFechadas"),
                horas, BRL.format(investimento), BRL.format(receita), roi);
    }

    private String mesPorExtenso(LocalDate d) {
        String[] meses = {"Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        return meses[d.getMonthValue() - 1] + " de " + d.getYear();
    }

    // ===================== Helpers JDBC (tenant explícito) =====================

    private long count(String table, UUID tenantId, Date s, Date e, String extra) {
        String sql = "SELECT COUNT(*) FROM " + table +
                " WHERE tenant_id = ? AND created_at::date BETWEEN ? AND ?" +
                (extra != null ? " AND " + extra : "");
        Long v = jdbc.queryForObject(sql, Long.class, tenantId, s, e);
        return v != null ? v : 0;
    }

    private long countClosed(String table, UUID tenantId, Date s, Date e, String extra) {
        String sql = "SELECT COUNT(*) FROM " + table +
                " WHERE tenant_id = ? AND COALESCE(closed_at::date, created_at::date) BETWEEN ? AND ? AND " + extra;
        Long v = jdbc.queryForObject(sql, Long.class, tenantId, s, e);
        return v != null ? v : 0;
    }

    private BigDecimal sum(String table, String col, UUID tenantId, Date s, Date e, String extra) {
        String sql = "SELECT COALESCE(SUM(" + col + "),0) FROM " + table +
                " WHERE tenant_id = ? AND created_at::date BETWEEN ? AND ?" +
                (extra != null ? " AND " + extra : "");
        BigDecimal v = jdbc.queryForObject(sql, BigDecimal.class, tenantId, s, e);
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal sumClosed(String table, String col, UUID tenantId, Date s, Date e, String extra) {
        String sql = "SELECT COALESCE(SUM(" + col + "),0) FROM " + table +
                " WHERE tenant_id = ? AND COALESCE(closed_at::date, created_at::date) BETWEEN ? AND ? AND " + extra;
        BigDecimal v = jdbc.queryForObject(sql, BigDecimal.class, tenantId, s, e);
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal sumReceived(UUID tenantId, Date s, Date e) {
        String sql = "SELECT COALESCE(SUM(value),0) FROM payments " +
                "WHERE tenant_id = ? AND status IN ('RECEIVED','CONFIRMED') " +
                "AND COALESCE(payment_date, created_at::date) BETWEEN ? AND ?";
        BigDecimal v = jdbc.queryForObject(sql, BigDecimal.class, tenantId, s, e);
        return v != null ? v : BigDecimal.ZERO;
    }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }

    private Map<String, Object> ordered(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }
}
