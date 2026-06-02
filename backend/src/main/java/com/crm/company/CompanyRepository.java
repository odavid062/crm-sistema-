package com.crm.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    @Query("""
        SELECT c FROM Company c
        WHERE (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:industry IS NULL OR c.industry = :industry)
    """)
    Page<Company> search(@Param("search") String search, @Param("industry") String industry, Pageable pageable);
}
