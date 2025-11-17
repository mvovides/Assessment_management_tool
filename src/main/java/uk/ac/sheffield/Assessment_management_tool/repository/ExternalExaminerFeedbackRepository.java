package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Assessment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.ExternalExaminerFeedback;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalExaminerFeedbackRepository extends JpaRepository<ExternalExaminerFeedback, UUID> {
    
    Optional<ExternalExaminerFeedback> findByAssessment(Assessment assessment);
    
    Optional<ExternalExaminerFeedback> findByAssessmentId(UUID assessmentId);
    
    boolean existsByAssessment(Assessment assessment);
    
    boolean existsByAssessmentId(UUID assessmentId);
}
