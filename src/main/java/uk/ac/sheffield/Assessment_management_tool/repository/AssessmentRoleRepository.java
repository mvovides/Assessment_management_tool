package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Assessment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.AssessmentRoleAssignment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.User;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentRoleRepository extends JpaRepository<AssessmentRoleAssignment, UUID> {
    
    List<AssessmentRoleAssignment> findByAssessment(Assessment assessment);
    
    List<AssessmentRoleAssignment> findByAssessmentId(UUID assessmentId);
    
    List<AssessmentRoleAssignment> findByUser(User user);
    
    List<AssessmentRoleAssignment> findByUserId(UUID userId);
    
    List<AssessmentRoleAssignment> findByAssessmentAndRole(Assessment assessment, AssessmentRole role);
    
    List<AssessmentRoleAssignment> findByAssessmentAndUser(Assessment assessment, User user);
    
    Optional<AssessmentRoleAssignment> findByAssessmentAndUserAndRole(Assessment assessment, User user, AssessmentRole role);
    
    boolean existsByAssessmentAndUserAndRole(Assessment assessment, User user, AssessmentRole role);
    
    void deleteByAssessmentAndUserAndRole(Assessment assessment, User user, AssessmentRole role);
}
