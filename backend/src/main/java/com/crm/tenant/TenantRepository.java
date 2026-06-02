package com.crm.tenant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("""
        SELECT t FROM Tenant t
        WHERE (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(t.slug) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:status IS NULL OR t.status = :status)
    """)
    Page<Tenant> search(@Param("search") String search, @Param("status") TenantStatus status, Pageable pageable);

    long countByStatus(TenantStatus status);
}
