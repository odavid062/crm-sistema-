package com.crm.contact;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    @Query("""
        SELECT c FROM Contact c
        WHERE (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR c.phone LIKE CONCAT('%', CAST(:search AS string), '%'))
        AND (:status IS NULL OR c.status = :status)
        AND (:ownerId IS NULL OR c.owner.id = :ownerId)
    """)
    Page<Contact> search(
            @Param("search") String search,
            @Param("status") ContactStatus status,
            @Param("ownerId") UUID ownerId,
            Pageable pageable
    );

    long countByStatus(ContactStatus status);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
