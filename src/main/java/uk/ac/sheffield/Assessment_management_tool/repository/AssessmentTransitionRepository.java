package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Assessment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.AssessmentTransition;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentTransitionRepository extends JpaRepository<AssessmentTransition, UUID> {
    
    List<AssessmentTransition> findByAssessmentOrderByAtDesc(Assessment assessment);
    
    List<AssessmentTransition> findByAssessmentIdOrderByAtDesc(UUID assessmentId);
    
    List<AssessmentTransition> findByByUserIdOrderByAtDesc(UUID userId);
    
    @Query("SELECT t FROM AssessmentTransition t WHERE " +
           "(:fromDate IS NULL OR t.at >= :fromDate) AND " +
           "(:toDate IS NULL OR t.at <= :toDate) AND " +
           "(:userId IS NULL OR t.byUser.id = :userId) AND " +
           "(:moduleId IS NULL OR t.assessment.module.id = :moduleId) " +
           "ORDER BY t.at DESC")
    List<AssessmentTransition> findTransitionsWithFilters(
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate,
        @Param("userId") UUID userId,
        @Param("moduleId") UUID moduleId
    );
}
