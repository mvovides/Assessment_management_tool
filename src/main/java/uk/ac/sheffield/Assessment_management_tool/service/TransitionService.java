package uk.ac.sheffield.Assessment_management_tool.service;

import org.springframework.stereotype.Service;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.*;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.*;
import uk.ac.sheffield.Assessment_management_tool.repository.*;

import java.util.*;

@Service
public class TransitionService {
    
    private final AssessmentRoleRepository assessmentRoleRepository;
    private final ModuleStaffRoleRepository moduleStaffRoleRepository;
    private final ExternalExaminerFeedbackRepository externalFeedbackRepository;
    private final SetterResponseRepository setterResponseRepository;
    private final ModuleExternalExaminerRepository moduleExternalExaminerRepository;
    
    public TransitionService(
            AssessmentRoleRepository assessmentRoleRepository,
            ModuleStaffRoleRepository moduleStaffRoleRepository,
            ExternalExaminerFeedbackRepository externalFeedbackRepository,
            SetterResponseRepository setterResponseRepository,
            ModuleExternalExaminerRepository moduleExternalExaminerRepository) {
        this.assessmentRoleRepository = assessmentRoleRepository;
        this.moduleStaffRoleRepository = moduleStaffRoleRepository;
        this.externalFeedbackRepository = externalFeedbackRepository;
        this.setterResponseRepository = setterResponseRepository;
        this.moduleExternalExaminerRepository = moduleExternalExaminerRepository;
    }
    
    /**
     * Check if a user can progress an assessment to a target state
     */
    public boolean canProgress(User user, Assessment assessment, AssessmentState target) {
        if (user == null || assessment == null || target == null) {
            return false;
        }
        
        // Admin and Exams Officer can override (handled separately)
        if (isAdminOrExamsOfficer(user)) {
            return true;
        }
        
        AssessmentState current = assessment.getCurrentState();
        AssessmentType type = assessment.getType();
        
        // Check if transition is valid for this assessment type
        if (!isValidTransition(current, target, type)) {
            return false;
        }
        
        // Check role-based permissions
        return hasPermissionForTransition(user, assessment, current, target);
    }
    
    /**
     * Get allowed target states for a user and assessment
     */
    public List<AssessmentState> allowedTargets(User user, Assessment assessment) {
        if (user == null || assessment == null) {
            return Collections.emptyList();
        }
        
        List<AssessmentState> targets = new ArrayList<>();
        AssessmentState current = assessment.getCurrentState();
        AssessmentType type = assessment.getType();
        
        // Get possible next states based on type
        List<AssessmentState> possibleStates = getPossibleNextStates(current, type);
        
        // Filter by permissions
        for (AssessmentState target : possibleStates) {
            if (canProgress(user, assessment, target)) {
                targets.add(target);
            }
        }
        
        return targets;
    }
    
    /**
     * Validate if user can be assigned as checker (independence check)
     */
    public boolean canBeChecker(User user, Assessment assessment) {
        // Must be an academic
        if (user.getBaseType() != UserBaseType.ACADEMIC) {
            return false;
        }
        
        // Cannot be module staff (lead or staff)
        List<ModuleStaffRole> staffRoles = moduleStaffRoleRepository.findByModuleAndUser(
            assessment.getModule(), user
        );
        
        for (ModuleStaffRole role : staffRoles) {
            if (role.getRole() == ModuleRole.MODULE_LEAD || role.getRole() == ModuleRole.STAFF) {
                return false;
            }
        }
        
        // Cannot be a setter on this assessment
        List<AssessmentRoleAssignment> assessmentRoles = assessmentRoleRepository.findByAssessmentAndUser(
            assessment, user
        );
        
        for (AssessmentRoleAssignment role : assessmentRoles) {
            if (role.getRole() == AssessmentRole.SETTER) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if external examiner feedback can be submitted
     */
    public boolean canSubmitExternalFeedback(User user, Assessment assessment) {
        // Must be external examiner
        if (user.getBaseType() != UserBaseType.EXTERNAL_EXAMINER) {
            return false;
        }
        
        // Must be assigned to this module
        boolean isAssigned = moduleExternalExaminerRepository.existsByModuleAndUser(
            assessment.getModule(), user
        );
        
        if (!isAssigned) {
            return false;
        }
        
        // Must be an exam
        if (assessment.getType() != AssessmentType.EXAM) {
            return false;
        }
        
        // Must be in EXTERNAL_FEEDBACK state
        if (assessment.getCurrentState() != AssessmentState.EXTERNAL_FEEDBACK) {
            return false;
        }
        
        // Cannot already have feedback
        return !externalFeedbackRepository.existsByAssessment(assessment);
    }
    
    /**
     * Check if setter response can be submitted
     */
    public boolean canSubmitSetterResponse(User user, Assessment assessment) {
        // Must be a setter
        List<AssessmentRoleAssignment> roles = assessmentRoleRepository.findByAssessmentAndUser(
            assessment, user
        );
        
        boolean isSetter = roles.stream()
            .anyMatch(r -> r.getRole() == AssessmentRole.SETTER);
        
        if (!isSetter) {
            return false;
        }
        
        // Must be exam
        if (assessment.getType() != AssessmentType.EXAM) {
            return false;
        }
        
        // Must be in SETTER_RESPONSE state
        if (assessment.getCurrentState() != AssessmentState.SETTER_RESPONSE) {
            return false;
        }
        
        // Must have external feedback
        if (!externalFeedbackRepository.existsByAssessment(assessment)) {
            return false;
        }
        
        // Cannot already have response
        return !setterResponseRepository.existsByAssessment(assessment);
    }
    
    // Private helper methods
    
    private boolean isAdminOrExamsOfficer(User user) {
        return user.getBaseType() == UserBaseType.TEACHING_SUPPORT || user.isExamsOfficer();
    }
    
    private boolean isValidTransition(AssessmentState from, AssessmentState to, AssessmentType type) {
        List<AssessmentState> possible = getPossibleNextStates(from, type);
        return possible.contains(to);
    }
    
    private List<AssessmentState> getPossibleNextStates(AssessmentState current, AssessmentType type) {
        List<AssessmentState> states = new ArrayList<>();
        
        switch (type) {
            case CW:
                states.addAll(getCourseworkNextStates(current));
                break;
            case TEST:
                states.addAll(getTestNextStates(current));
                break;
            case EXAM:
                states.addAll(getExamNextStates(current));
                break;
        }
        
        return states;
    }
    
    private List<AssessmentState> getCourseworkNextStates(AssessmentState current) {
        return switch (current) {
            case DRAFT -> List.of(AssessmentState.READY_FOR_CHECK);
            case READY_FOR_CHECK -> List.of(AssessmentState.CHANGES_REQUIRED, AssessmentState.RELEASED, AssessmentState.DRAFT);
            case CHANGES_REQUIRED -> List.of(AssessmentState.READY_FOR_CHECK);
            case RELEASED -> List.of(AssessmentState.DEADLINE_PASSED);
            case DEADLINE_PASSED -> List.of(AssessmentState.MARKING); // Standardisation happens during marking
            case MARKING -> List.of(AssessmentState.MODERATED);
            case MODERATED -> List.of(AssessmentState.FEEDBACK_RETURNED);
            case FEEDBACK_RETURNED -> List.of(AssessmentState.APPROVED);
            case APPROVED -> List.of(AssessmentState.PUBLISHED);
            default -> Collections.emptyList();
        };
    }
    
    private List<AssessmentState> getTestNextStates(AssessmentState current) {
        return switch (current) {
            case DRAFT -> List.of(AssessmentState.READY_FOR_CHECK);
            case READY_FOR_CHECK -> List.of(AssessmentState.CHANGES_REQUIRED, AssessmentState.TEST_TAKEN, AssessmentState.DRAFT);
            case CHANGES_REQUIRED -> List.of(AssessmentState.READY_FOR_CHECK);
            case TEST_TAKEN -> List.of(AssessmentState.MARKING); // Standardisation happens during marking
            case MARKING -> List.of(AssessmentState.MODERATED);
            case MODERATED -> List.of(AssessmentState.RESULTS_RETURNED);
            case RESULTS_RETURNED -> List.of(AssessmentState.APPROVED);
            case APPROVED -> List.of(AssessmentState.PUBLISHED);
            default -> Collections.emptyList();
        };
    }
    
    private List<AssessmentState> getExamNextStates(AssessmentState current) {
        return switch (current) {
            case DRAFT -> List.of(AssessmentState.READY_FOR_CHECK);
            case READY_FOR_CHECK -> List.of(AssessmentState.CHANGES_REQUIRED, AssessmentState.EXAM_OFFICER_CHECK, AssessmentState.DRAFT);
            case CHANGES_REQUIRED -> List.of(AssessmentState.READY_FOR_CHECK);
            case EXAM_OFFICER_CHECK -> List.of(AssessmentState.EXTERNAL_FEEDBACK, AssessmentState.EXAM_CHANGES_REQUIRED);
            case EXAM_CHANGES_REQUIRED -> List.of(AssessmentState.EXAM_OFFICER_CHECK);
            case EXTERNAL_FEEDBACK -> List.of(AssessmentState.SETTER_RESPONSE);
            case SETTER_RESPONSE -> List.of(AssessmentState.FINAL_CHECK);
            case FINAL_CHECK -> List.of(AssessmentState.SENT_TO_PRINTING, AssessmentState.EXAM_CHANGES_REQUIRED);
            case SENT_TO_PRINTING -> List.of(AssessmentState.EXAM_TAKEN);
            case EXAM_TAKEN -> List.of(AssessmentState.MARKING); // Standardisation happens during marking
            case MARKING -> List.of(AssessmentState.ADMIN_MARK_CHECK);
            case ADMIN_MARK_CHECK -> List.of(AssessmentState.MODERATED);
            case MODERATED -> List.of(AssessmentState.APPROVED);
            case APPROVED -> List.of(AssessmentState.PUBLISHED);
            default -> Collections.emptyList();
        };
    }
    
    private boolean hasPermissionForTransition(User user, Assessment assessment, 
                                               AssessmentState from, AssessmentState to) {
        // Get user roles on this assessment
        List<AssessmentRoleAssignment> roles = assessmentRoleRepository.findByAssessmentAndUser(
            assessment, user
        );
        
        boolean isSetter = roles.stream().anyMatch(r -> r.getRole() == AssessmentRole.SETTER);
        boolean isChecker = roles.stream().anyMatch(r -> r.getRole() == AssessmentRole.CHECKER);
        
        // Get module roles
        List<ModuleStaffRole> staffRoles = moduleStaffRoleRepository.findByModuleAndUser(
            assessment.getModule(), user
        );
        boolean isModerator = staffRoles.stream().anyMatch(r -> r.getRole() == ModuleRole.MODERATOR);
        boolean isModuleLead = staffRoles.stream().anyMatch(r -> r.getRole() == ModuleRole.MODULE_LEAD);
        
        // Setter permissions - Create and submit for checking
        if (from == AssessmentState.DRAFT && to == AssessmentState.READY_FOR_CHECK) {
            return isSetter;
        }
        
        if (from == AssessmentState.CHANGES_REQUIRED && to == AssessmentState.READY_FOR_CHECK) {
            return isSetter;
        }
        
        if (from == AssessmentState.EXAM_CHANGES_REQUIRED && to == AssessmentState.EXAM_OFFICER_CHECK) {
            return isSetter;
        }
        
        // Setter can revert from READY_FOR_CHECK back to DRAFT to edit content
        if (from == AssessmentState.READY_FOR_CHECK && to == AssessmentState.DRAFT) {
            return isSetter;
        }
        
        // Checker permissions - Approve or request changes
        if (from == AssessmentState.READY_FOR_CHECK) {
            if (to == AssessmentState.CHANGES_REQUIRED || to == AssessmentState.RELEASED ||
                to == AssessmentState.TEST_TAKEN || to == AssessmentState.EXAM_OFFICER_CHECK) {
                return isChecker;
            }
        }
        
        // Module Lead permissions - Release coursework, record test/exam completion
        if (isModuleLead) {
            if (to == AssessmentState.DEADLINE_PASSED || to == AssessmentState.TEST_TAKEN || 
                to == AssessmentState.EXAM_TAKEN) {
                return true;
            }
        }
        
        // Exams Officer permissions - Handle exam workflows
        if (user.isExamsOfficer() && assessment.getType() == AssessmentType.EXAM) {
            if (from == AssessmentState.EXAM_OFFICER_CHECK) {
                return to == AssessmentState.EXTERNAL_FEEDBACK || to == AssessmentState.EXAM_CHANGES_REQUIRED;
            }
            if (from == AssessmentState.FINAL_CHECK) {
                return to == AssessmentState.SENT_TO_PRINTING || to == AssessmentState.EXAM_CHANGES_REQUIRED;
            }
            if (from == AssessmentState.SENT_TO_PRINTING || from == AssessmentState.ADMIN_MARK_CHECK) {
                return true;
            }
        }
        
        // External Examiner feedback (requires submission)
        if (from == AssessmentState.EXTERNAL_FEEDBACK && to == AssessmentState.SETTER_RESPONSE) {
            return externalFeedbackRepository.existsByAssessment(assessment);
        }
        
        // Setter response to external feedback
        if (from == AssessmentState.SETTER_RESPONSE && to == AssessmentState.FINAL_CHECK) {
            return isSetter && setterResponseRepository.existsByAssessment(assessment);
        }
        
        // Marking team permissions - Progress marking
        if (from == AssessmentState.MARKING) {
            // Any staff member can progress marking for their assessments
            return !staffRoles.isEmpty() || isChecker || isSetter;
        }
        
        // Moderator permissions - Complete moderation
        if (from == AssessmentState.MODERATED) {
            return isModerator;
        }
        
        // Module Lead permissions - Return feedback/results
        if (from == AssessmentState.FEEDBACK_RETURNED || from == AssessmentState.RESULTS_RETURNED) {
            return isModuleLead;
        }
        
        // Admin/Teaching Support permissions - Formal approval
        if (from == AssessmentState.APPROVED && to == AssessmentState.PUBLISHED) {
            return user.getBaseType() == UserBaseType.TEACHING_SUPPORT;
        }
        
        return false;
    }
}
