package com.crm.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // Calendário: agendamentos que começam dentro do intervalo
    List<Appointment> findByStartAtBetweenOrderByStartAtAsc(LocalDateTime from, LocalDateTime to);

    // Kanban: todos os agendamentos não-finais, ordenados por data
    @Query("""
        SELECT a FROM Appointment a
        WHERE (:status IS NULL OR a.status = :status)
          AND (:assignedTo IS NULL OR a.assignedTo.id = :assignedTo)
        ORDER BY a.startAt ASC
        """)
    List<Appointment> search(@Param("status") AppointmentStatus status,
                             @Param("assignedTo") UUID assignedTo);
}
