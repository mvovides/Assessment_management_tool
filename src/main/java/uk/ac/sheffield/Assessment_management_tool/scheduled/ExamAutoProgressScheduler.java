package uk.ac.sheffield.Assessment_management_tool.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Assessment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.AssessmentTransition;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentType;
import uk.ac.sheffield.Assessment_management_tool.repository.AssessmentRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.AssessmentTransitionRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
public class ExamAutoProgressScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamAutoProgressScheduler.class);
    
    private final AssessmentRepository assessmentRepository;
    private final AssessmentTransitionRepository transitionRepository;
    
    public ExamAutoProgressScheduler(
            AssessmentRepository assessmentRepository,
            AssessmentTransitionRepository transitionRepository) {
        this.assessmentRepository = assessmentRepository;
        this.transitionRepository = transitionRepository;
    }
    
    /**
     * Auto-progress exams to EXAM_TAKEN state
     * Runs daily at 02:00 Europe/London
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/London")
    @Transactional
    public void autoProgressExams() {
        logger.info("Running exam auto-progress job");
        
        LocalDate today = LocalDate.now();
        LocalDate checkDate = getLastWorkingDay(today);
        
        // Find exams that should be progressed
        List<Assessment> examsToProgress = assessmentRepository.findByTypeAndExamDate(
            AssessmentType.EXAM, checkDate
        );
        
        int progressedCount = 0;
        
        for (Assessment assessment : examsToProgress) {
            if (assessment.getCurrentState() == AssessmentState.SENT_TO_PRINTING) {
                // Create transition
                AssessmentTransition transition = new AssessmentTransition();
                transition.setAssessment(assessment);
                transition.setFromState(assessment.getCurrentState());
                transition.setToState(AssessmentState.EXAM_TAKEN);
                transition.setByUser(null); // System action
                transition.setByDisplayName("System (Auto-progress)");
                transition.setNote("Automatically progressed after exam date");
                transition.setOverride(false);
                transition.setReversion(false);
                
                transitionRepository.save(transition);
                
                // Update assessment
                assessment.setCurrentState(AssessmentState.EXAM_TAKEN);
                assessmentRepository.save(assessment);
                
                progressedCount++;
                logger.info("Auto-progressed exam assessment {} to EXAM_TAKEN", assessment.getId());
            }
        }
        
        logger.info("Exam auto-progress job completed. Progressed {} assessments", progressedCount);
    }
    
    /**
     * Get the last working day (previous day, skipping weekends)
     */
    private LocalDate getLastWorkingDay(LocalDate date) {
        LocalDate yesterday = date.minusDays(1);
        
        // If yesterday is Saturday, go back to Friday
        if (yesterday.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return yesterday.minusDays(1);
        }
        
        // If yesterday is Sunday, go back to Friday
        if (yesterday.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return yesterday.minusDays(2);
        }
        
        return yesterday;
    }
}
