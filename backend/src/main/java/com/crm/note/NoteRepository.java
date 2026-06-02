package com.crm.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByContactIdOrderByCreatedAtDesc(UUID contactId);
    List<Note> findByDealIdOrderByCreatedAtDesc(UUID dealId);
    List<Note> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
