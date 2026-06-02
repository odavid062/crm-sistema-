package com.crm.pipeline;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, UUID> {
    List<PipelineStage> findByPipelineIdOrderByPositionAsc(UUID pipelineId);
}
