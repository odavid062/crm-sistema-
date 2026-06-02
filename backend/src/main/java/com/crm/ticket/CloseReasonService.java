package com.crm.ticket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloseReasonService {

    private final CloseReasonRepository repository;

    public List<CloseReason> findAll() {
        return repository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public CloseReason create(CloseReason r) {
        return repository.save(r);
    }

    @Transactional
    public void delete(UUID id) {
        repository.findById(id).ifPresent(r -> { r.setActive(false); repository.save(r); });
    }
}
