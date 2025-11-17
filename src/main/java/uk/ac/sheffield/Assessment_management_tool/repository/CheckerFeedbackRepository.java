package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Assessment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.CheckerFeedback;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckerFeedbackRepository extends JpaRepository<CheckerFeedback, UUID> {
    
    List<CheckerFeedback> findByAssessmentOrderByCreatedAtDesc(Assessment assessment);
    
    List<CheckerFeedback> findByAssessmentIdOrderByCreatedAtDesc(UUID assessmentId);
    
    List<CheckerFeedback> findByAuthorId(UUID authorId);
}
