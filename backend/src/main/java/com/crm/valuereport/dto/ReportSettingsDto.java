package com.crm.valuereport.dto;

import java.math.BigDecimal;

public record ReportSettingsDto(
        BigDecimal monthlyInvestment,
        String currency,
        BigDecimal minutesPerMessage,
        BigDecimal minutesPerFollowup,
        BigDecimal minutesPerTask,
        BigDecimal hourlyCost
) {}
