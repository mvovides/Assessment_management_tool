package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Assessment;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    
    List<Assessment> findByModuleId(UUID moduleId);
    
    List<Assessment> findByCurrentState(AssessmentState state);
    
    List<Assessment> findByType(AssessmentType type);
    
    @Query("SELECT a FROM Assessment a WHERE a.type = :type AND a.examDate = :date")
    List<Assessment> findByTypeAndExamDate(@Param("type") AssessmentType type, @Param("date") LocalDate date);
    
    @Query("SELECT a FROM Assessment a WHERE a.type = 'EXAM' AND a.examDate <= :date AND a.currentState < :state")
    List<Assessment> findExamsToAutoProgress(@Param("date") LocalDate date, @Param("state") AssessmentState state);
}
