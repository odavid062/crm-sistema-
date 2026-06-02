package com.crm.apitoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiTokenRepository extends JpaRepository<ApiToken, UUID> {
    Optional<ApiToken> findByTokenHashAndActiveTrue(String tokenHash);
    List<ApiToken> findByTenantId(UUID tenantId);
}
