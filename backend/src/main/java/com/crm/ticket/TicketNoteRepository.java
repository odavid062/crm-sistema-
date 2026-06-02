package com.crm.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketNoteRepository extends JpaRepository<TicketNote, UUID> {
    List<TicketNote> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
