package com.crm.pipeline;

import com.crm.pipeline.dto.PipelineRequest;
import com.crm.pipeline.dto.PipelineResponse;
import com.crm.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PipelineService {

    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository stageRepository;

    public List<PipelineResponse> findAll() {
        return pipelineRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    public PipelineResponse findById(UUID id) {
        return toResponse(pipelineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pipeline não encontrado")));
    }

    @Transactional
    public PipelineResponse create(PipelineRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pipeline pipeline = Pipeline.builder()
                .name(request.name())
                .description(request.description())
                .createdBy(currentUser)
                .build();
        pipeline = pipelineRepository.save(pipeline);
        if (request.stages() != null) {
            for (PipelineRequest.StageRequest sr : request.stages()) {
                PipelineStage stage = PipelineStage.builder()
                        .pipeline(pipeline)
                        .name(sr.name())
                        .color(sr.color())
                        .position(sr.position())
                        .winProbability(sr.winProbability())
                        .build();
                pipeline.getStages().add(stageRepository.save(stage));
            }
        }
        return toResponse(pipeline);
    }

    @Transactional
    public PipelineResponse addStage(UUID pipelineId, PipelineRequest.StageRequest request) {
        Pipeline pipeline = pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new RuntimeException("Pipeline não encontrado"));
        PipelineStage stage = PipelineStage.builder()
                .pipeline(pipeline)
                .name(request.name())
                .color(request.color())
                .position(request.position())
                .winProbability(request.winProbability())
                .build();
        pipeline.getStages().add(stageRepository.save(stage));
        return toResponse(pipeline);
    }

    @Transactional
    public void delete(UUID id) {
        Pipeline pipeline = pipelineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pipeline não encontrado"));
        pipeline.setActive(false);
        pipelineRepository.save(pipeline);
    }

    public PipelineResponse toResponse(Pipeline p) {
        List<PipelineResponse.StageResponse> stages = p.getStages().stream()
                .map(s -> new PipelineResponse.StageResponse(s.getId(), s.getName(), s.getColor(), s.getPosition(), s.getWinProbability()))
                .toList();
        return new PipelineResponse(p.getId(), p.getName(), p.getDescription(), p.isActive(), stages, p.getCreatedAt());
    }
}
