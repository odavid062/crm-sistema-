package com.crm.ticket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;

    public List<Queue> findAll() {
        return queueRepository.findByActiveTrueOrderByOrderIndexAsc();
    }

    @Transactional
    public Queue create(Queue q) {
        return queueRepository.save(q);
    }

    @Transactional
    public Queue update(UUID id, Queue updated) {
        Queue q = queueRepository.findById(id).orElseThrow(() -> new RuntimeException("Fila não encontrada"));
        q.setName(updated.getName());
        q.setColor(updated.getColor());
        q.setOrderIndex(updated.getOrderIndex());
        q.setGreetingMessage(updated.getGreetingMessage());
        q.setActive(updated.isActive());
        return queueRepository.save(q);
    }

    @Transactional
    public void delete(UUID id) {
        queueRepository.findById(id).ifPresent(q -> { q.setActive(false); queueRepository.save(q); });
    }
}
