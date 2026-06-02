package com.crm.pipeline.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PipelineResponse(
        UUID id,
        String name,
        String description,
        boolean active,
        List<StageResponse> stages,
        LocalDateTime createdAt
) {
    public record StageResponse(UUID id, String name, String color, int position, double winProbability) {}
}
