package com.crm.valuereport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValueReportRepository extends JpaRepository<ValueReport, UUID> {

    @Query(value = """
        SELECT * FROM value_reports
        WHERE tenant_id = :tenantId
        ORDER BY period_start DESC
        """, nativeQuery = true)
    List<ValueReport> findAllByTenant(@Param("tenantId") UUID tenantId);

    @Query(value = """
        SELECT * FROM value_reports
        WHERE tenant_id = :tenantId AND period_type = :periodType AND period_start = :periodStart
        LIMIT 1
        """, nativeQuery = true)
    Optional<ValueReport> findByTenantAndPeriod(@Param("tenantId") UUID tenantId,
                                                @Param("periodType") String periodType,
                                                @Param("periodStart") LocalDate periodStart);

    @Query(value = """
        SELECT * FROM value_reports
        WHERE tenant_id = :tenantId AND period_type = :periodType AND period_start < :periodStart
        ORDER BY period_start DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<ValueReport> findPreviousReport(@Param("tenantId") UUID tenantId,
                                             @Param("periodType") String periodType,
                                             @Param("periodStart") LocalDate periodStart);
}
