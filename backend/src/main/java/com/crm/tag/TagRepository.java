package com.crm.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByNameContainingIgnoreCase(String name);
    boolean existsByName(String name);
}
