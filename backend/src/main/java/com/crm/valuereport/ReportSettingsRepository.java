package com.crm.valuereport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportSettingsRepository extends JpaRepository<ReportSettings, UUID> {

    // Explícito por tenant: funciona tanto em request quanto em job agendado.
    @Query(value = "SELECT * FROM report_settings WHERE tenant_id = :tenantId", nativeQuery = true)
    Optional<ReportSettings> findByTenant(@Param("tenantId") UUID tenantId);
}
