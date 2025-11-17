package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Assessment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.SetterResponse;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SetterResponseRepository extends JpaRepository<SetterResponse, UUID> {
    
    Optional<SetterResponse> findByAssessment(Assessment assessment);
    
    Optional<SetterResponse> findByAssessmentId(UUID assessmentId);
    
    boolean existsByAssessment(Assessment assessment);
    
    boolean existsByAssessmentId(UUID assessmentId);
}
