package com.crm.deal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface DealRepository extends JpaRepository<Deal, UUID> {

    @Query("""
        SELECT d FROM Deal d
        WHERE (:pipelineId IS NULL OR d.pipeline.id = :pipelineId)
        AND (:stageId IS NULL OR d.stage.id = :stageId)
        AND (:status IS NULL OR d.status = :status)
        AND (:ownerId IS NULL OR d.owner.id = :ownerId)
        AND (:search IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
    """)
    Page<Deal> search(
            @Param("pipelineId") UUID pipelineId,
            @Param("stageId") UUID stageId,
            @Param("status") DealStatus status,
            @Param("ownerId") UUID ownerId,
            @Param("search") String search,
            Pageable pageable
    );

    List<Deal> findByPipelineIdAndStatus(UUID pipelineId, DealStatus status);

    @Query("SELECT SUM(d.value) FROM Deal d WHERE d.status = :status")
    BigDecimal sumValueByStatus(@Param("status") DealStatus status);

    @Query("SELECT COUNT(d) FROM Deal d WHERE d.status = :status")
    long countByStatus(@Param("status") DealStatus status);
}
