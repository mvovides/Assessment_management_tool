package uk.ac.sheffield.Assessment_management_tool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Assessment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.AssessmentRoleAssignment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.AssessmentTransition;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Module;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.ModuleStaffRole;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.User;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentType;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.ModuleRole;
import uk.ac.sheffield.Assessment_management_tool.dto.request.CreateAssessmentRequest;
import uk.ac.sheffield.Assessment_management_tool.dto.request.TransitionRequest;
import uk.ac.sheffield.Assessment_management_tool.dto.response.AssessmentDto;
import uk.ac.sheffield.Assessment_management_tool.dto.response.TransitionDto;
import uk.ac.sheffield.Assessment_management_tool.mapper.EntityMapper;
import uk.ac.sheffield.Assessment_management_tool.repository.AssessmentRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.AssessmentRoleRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.AssessmentTransitionRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.ModuleRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.ModuleStaffRoleRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssessmentService {
    
    private final AssessmentRepository assessmentRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final AssessmentTransitionRepository transitionRepository;
    private final AssessmentRoleRepository assessmentRoleRepository;
    private final ModuleStaffRoleRepository moduleStaffRoleRepository;
    private final TransitionService transitionService;
    
    public AssessmentService(
            AssessmentRepository assessmentRepository,
            ModuleRepository moduleRepository,
            UserRepository userRepository,
            AssessmentTransitionRepository transitionRepository,
            AssessmentRoleRepository assessmentRoleRepository,
            ModuleStaffRoleRepository moduleStaffRoleRepository,
            TransitionService transitionService) {
        this.assessmentRepository = assessmentRepository;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.transitionRepository = transitionRepository;
        this.assessmentRoleRepository = assessmentRoleRepository;
        this.moduleStaffRoleRepository = moduleStaffRoleRepository;
        this.transitionService = transitionService;
    }
    
    public AssessmentDto createAssessment(UUID moduleId, CreateAssessmentRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));
        
        // Validate exam date for exam type
        if (request.getType() == AssessmentType.EXAM && request.getExamDate() == null) {
            throw new IllegalArgumentException("Exam date is required for exam assessments");
        }
        
        Assessment assessment = new Assessment();
        assessment.setModule(module);
        assessment.setTitle(request.getTitle());
        assessment.setType(request.getType());
        assessment.setExamDate(request.getExamDate());
        // currentState is set to DRAFT in constructor
        
        assessment = assessmentRepository.save(assessment);
        
        // Automatically assign module moderator as checker (if module has a moderator)
        List<ModuleStaffRole> moderators = moduleStaffRoleRepository.findByModuleAndRole(module, ModuleRole.MODERATOR);
        if (!moderators.isEmpty()) {
            User moderator = moderators.get(0).getUser(); // Take the first moderator
            AssessmentRoleAssignment checkerRole = new AssessmentRoleAssignment(assessment, moderator, uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole.CHECKER);
            assessmentRoleRepository.save(checkerRole);
        }
        
        return EntityMapper.toAssessmentDto(assessment);
    }
    
    public AssessmentDto getAssessmentById(UUID id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + id));
        return EntityMapper.toAssessmentDto(assessment);
    }
    
    public AssessmentDto getAssessmentByIdWithUserContext(UUID id, UUID userId) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + id));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        AssessmentDto dto = EntityMapper.toAssessmentDto(assessment);
        
        // Get user's roles on this assessment
        List<AssessmentRoleAssignment> userRoles = assessmentRoleRepository.findByAssessmentAndUser(assessment, user);
        List<String> roles = userRoles.stream()
                .map(r -> r.getRole().name())
                .collect(Collectors.toList());
        
        // Add ADMIN role if user is teaching support AND doesn't have explicit assessment roles
        // This ensures checkers/setters who are also admins show their actual assignment role
        if (user.getBaseType() == uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType.TEACHING_SUPPORT) {
            if (roles.isEmpty()) {
                roles.add("ADMIN");
            }
        }
        
        dto.setRoles(roles);
        
        // Get allowed target states for this user
        List<AssessmentState> allowedTargets = transitionService.allowedTargets(user, assessment);
        dto.setAllowedTargets(allowedTargets);
        
        return dto;
    }
    
    public List<AssessmentDto> getAssessmentsByModule(UUID moduleId) {
        return assessmentRepository.findByModuleId(moduleId).stream()
                .map(EntityMapper::toAssessmentDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all assessments that the user can see:
     * - Assessments in modules where user is staff
     * - Assessments where user is assigned as SETTER or CHECKER
     * - All assessments if user is admin
     */
    public List<AssessmentDto> getAllAssessmentsForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // If admin, return all assessments
        if (user.getBaseType() == uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType.TEACHING_SUPPORT) {
            return assessmentRepository.findAll().stream()
                    .map(EntityMapper::toAssessmentDto)
                    .collect(Collectors.toList());
        }
        
        // Get modules where user is staff
        List<ModuleStaffRole> staffRoles = moduleStaffRoleRepository.findByUserId(userId);
        List<UUID> userModuleIds = staffRoles.stream()
                .map(role -> role.getModule().getId())
                .collect(Collectors.toList());
        
        // Get assessments from user's modules
        List<Assessment> moduleAssessments = userModuleIds.stream()
                .flatMap(moduleId -> assessmentRepository.findByModuleId(moduleId).stream())
                .collect(Collectors.toList());
        
        // Get assessments where user has a role assignment (SETTER or CHECKER)
        List<AssessmentRoleAssignment> roleAssignments = assessmentRoleRepository.findByUserId(userId);
        List<Assessment> roleAssessments = roleAssignments.stream()
                .map(AssessmentRoleAssignment::getAssessment)
                .collect(Collectors.toList());
        
        // Combine and deduplicate
        List<Assessment> allAssessments = new java.util.ArrayList<>(moduleAssessments);
        for (Assessment assessment : roleAssessments) {
            if (!allAssessments.contains(assessment)) {
                allAssessments.add(assessment);
            }
        }
        
        return allAssessments.stream()
                .map(EntityMapper::toAssessmentDto)
                .collect(Collectors.toList());
    }
    
    public AssessmentDto progressAssessment(UUID assessmentId, UUID userId, TransitionRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Check permission
        if (!transitionService.canProgress(user, assessment, request.getTargetState())) {
            throw new IllegalStateException("User not permitted to make this transition");
        }
        
        // Create transition record
        AssessmentState fromState = assessment.getCurrentState();
        AssessmentTransition transition = new AssessmentTransition();
        transition.setAssessment(assessment);
        transition.setFromState(fromState);
        transition.setToState(request.getTargetState());
        transition.setByUser(user);
        transition.setByDisplayName(user.getName());
        transition.setNote(request.getNote());
        transition.setOverride(false);
        transition.setReversion(false);
        
        transitionRepository.save(transition);
        
        // Update assessment state
        assessment.setCurrentState(request.getTargetState());
        assessment = assessmentRepository.save(assessment);
        
        return EntityMapper.toAssessmentDto(assessment);
    }
    
    public AssessmentDto overrideTransition(UUID assessmentId, UUID userId, TransitionRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Create override transition
        AssessmentState fromState = assessment.getCurrentState();
        AssessmentTransition transition = new AssessmentTransition();
        transition.setAssessment(assessment);
        transition.setFromState(fromState);
        transition.setToState(request.getTargetState());
        transition.setByUser(user);
        transition.setByDisplayName(user.getName());
        transition.setNote(request.getNote());
        transition.setOverride(true);
        transition.setReversion(false);
        
        transitionRepository.save(transition);
        
        // Update assessment state
        assessment.setCurrentState(request.getTargetState());
        assessment = assessmentRepository.save(assessment);
        
        return EntityMapper.toAssessmentDto(assessment);
    }
    
    public List<TransitionDto> getAssessmentTransitions(UUID assessmentId) {
        return transitionRepository.findByAssessmentIdOrderByAtDesc(assessmentId).stream()
                .map(EntityMapper::toTransitionDto)
                .collect(Collectors.toList());
    }
    
    public List<AssessmentDto> getExamsToAutoProgress(LocalDate date, AssessmentState beforeState) {
        return assessmentRepository.findExamsToAutoProgress(date, beforeState).stream()
                .map(EntityMapper::toAssessmentDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Assign a role (SETTER or CHECKER) to a user for an assessment
     */
    public void assignRole(UUID assessmentId, UUID userId, uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole role) {
        System.out.println("DEBUG: Assigning role " + role + " to user " + userId + " on assessment " + assessmentId);
        
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Additional validation for CHECKER role
        if (role == uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole.CHECKER) {
            if (!transitionService.canBeChecker(user, assessment)) {
                throw new IllegalArgumentException(
                    "User cannot be checker. Checker must be independent " +
                    "(not module lead, not module staff, not setter on this assessment)."
                );
            }
        }
        
        // Check if role already exists
        List<AssessmentRoleAssignment> existingRoles = assessmentRoleRepository
                .findByAssessmentAndUser(assessment, user);
        
        boolean roleExists = existingRoles.stream()
                .anyMatch(r -> r.getRole() == role);
        
        if (roleExists) {
            throw new IllegalArgumentException("User already has this role on this assessment");
        }
        
        // Create and save new role assignment
        AssessmentRoleAssignment roleAssignment = new AssessmentRoleAssignment(assessment, user, role);
        assessmentRoleRepository.save(roleAssignment);
        System.out.println("DEBUG: Successfully assigned role " + role + " to user " + user.getName() + " (" + user.getEmail() + ")");
    }
    
    /**
     * Remove a role assignment from an assessment
     */
    public void removeRole(UUID assessmentId, UUID userId, uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole role) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Find the role assignment
        List<AssessmentRoleAssignment> roles = assessmentRoleRepository.findByAssessmentAndUser(assessment, user);
        
        AssessmentRoleAssignment roleToRemove = roles.stream()
                .filter(r -> r.getRole() == role)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role assignment not found"));
        
        assessmentRoleRepository.delete(roleToRemove);
    }
    
    /**
     * Get all users assigned to an assessment with their roles
     */
    public List<AssessmentRoleAssignment> getAssessmentRoles(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));
        
        return assessmentRoleRepository.findByAssessment(assessment);
    }
    
    /**
     * Submit assessment content and metadata (setter uploads their work)
     */
    public AssessmentDto submitAssessmentContent(UUID assessmentId, UUID userId, uk.ac.sheffield.Assessment_management_tool.dto.request.SubmitAssessmentRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Verify user is a setter
        List<AssessmentRoleAssignment> roles = assessmentRoleRepository.findByAssessmentAndUser(assessment, user);
        boolean isSetter = roles.stream()
                .anyMatch(r -> r.getRole() == uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole.SETTER);
        
        if (!isSetter) {
            throw new IllegalStateException("Only setters can submit assessment content");
        }
        
        // Must be in DRAFT state to submit
        if (assessment.getCurrentState() != AssessmentState.DRAFT) {
            throw new IllegalStateException("Assessment content can only be submitted from DRAFT state. Current state: " + assessment.getCurrentState());
        }
        
        // Update assessment with content details
        assessment.setDescription(request.getDescription());
        assessment.setFileName(request.getFileName());
        assessment.setFileUrl(request.getFileUrl());
        
        assessment = assessmentRepository.save(assessment);
        
        return EntityMapper.toAssessmentDto(assessment);
    }
}
