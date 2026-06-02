package com.crm.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    Page<Activity> findByContactIdOrderByDueDateAsc(UUID contactId, Pageable pageable);
    Page<Activity> findByDealIdOrderByDueDateAsc(UUID dealId, Pageable pageable);
    Page<Activity> findByAssignedToIdAndStatusOrderByDueDateAsc(UUID userId, ActivityStatus status, Pageable pageable);
    List<Activity> findByStatusAndDueDateBefore(ActivityStatus status, LocalDateTime dateTime);

    /** Agenda: lista atividades com filtros opcionais (período, status, tipo, responsável). */
    @Query("""
        SELECT a FROM Activity a
        WHERE (:from IS NULL OR a.dueDate >= :from)
          AND (:to IS NULL OR a.dueDate <= :to)
          AND (:status IS NULL OR a.status = :status)
          AND (:type IS NULL OR a.type = :type)
          AND (:assignedTo IS NULL OR a.assignedTo.id = :assignedTo)
        ORDER BY a.dueDate ASC
        """)
    List<Activity> searchAgenda(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") ActivityStatus status,
            @Param("type") ActivityType type,
            @Param("assignedTo") UUID assignedTo
    );
}
