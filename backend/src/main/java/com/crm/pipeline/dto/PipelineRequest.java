package com.crm.pipeline.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PipelineRequest(
        @NotBlank String name,
        String description,
        List<StageRequest> stages
) {
    public record StageRequest(String name, String color, int position, double winProbability) {}
}
