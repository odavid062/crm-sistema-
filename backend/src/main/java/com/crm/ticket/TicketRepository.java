package com.crm.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query(value = "SELECT nextval('ticket_protocol_seq')", nativeQuery = true)
    Long nextProtocol();

    @Query("""
        SELECT t FROM Ticket t
        WHERE (:status IS NULL OR t.status = :status)
          AND (:queueId IS NULL OR t.queue.id = :queueId)
          AND (:assignedTo IS NULL OR t.assignedTo.id = :assignedTo)
          AND (:channel IS NULL OR t.channel = :channel)
        ORDER BY t.lastMessageAt DESC NULLS LAST, t.createdAt DESC
        """)
    java.util.List<Ticket> search(
            @Param("status") TicketStatus status,
            @Param("queueId") UUID queueId,
            @Param("assignedTo") UUID assignedTo,
            @Param("channel") String channel
    );

    Optional<Ticket> findByConversationIdAndStatusNot(UUID conversationId, TicketStatus status);

    long countByStatus(TicketStatus status);
}
