package com.crm.valuereport.dto;

import com.crm.valuereport.ReportPeriodType;

import java.time.LocalDate;

/**
 * Geração manual de relatório. Se as datas forem nulas, o serviço calcula
 * o intervalo a partir do periodType (mês/trimestre/ano corrente).
 */
public record GenerateReportRequest(
        ReportPeriodType periodType,
        LocalDate periodStart,
        LocalDate periodEnd
) {}
