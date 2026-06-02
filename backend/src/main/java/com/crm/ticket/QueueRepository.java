package com.crm.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QueueRepository extends JpaRepository<Queue, UUID> {
    List<Queue> findByActiveTrueOrderByOrderIndexAsc();
}
