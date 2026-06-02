package com.crm.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByAsaasId(String asaasId);
    Page<Payment> findByContactIdOrderByCreatedAtDesc(UUID contactId, Pageable pageable);
    Page<Payment> findByStatusOrderByDueDateAsc(String status, Pageable pageable);

    @Query("SELECT SUM(p.value) FROM Payment p WHERE p.status = :status")
    BigDecimal sumByStatus(@Param("status") String status);
}
