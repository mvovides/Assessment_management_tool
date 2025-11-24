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
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        if (request.getType() == AssessmentType.EXAM && request.getExamDate() == null) {
            throw new IllegalArgumentException("Exam date required for exams");
        }
        
        Assessment assessment = new Assessment(module, request.getTitle(), request.getType());
        assessment.setExamDate(request.getExamDate());
        assessment = assessmentRepository.save(assessment);
        
        autoAssignModerator(assessment, module);
        return EntityMapper.toAssessmentDto(assessment);
    }
    
    public AssessmentDto getAssessmentById(UUID id) {
        return EntityMapper.toAssessmentDto(assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found")));
    }
    
    public AssessmentDto getAssessmentByIdWithUserContext(UUID id, UUID userId) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        AssessmentDto dto = EntityMapper.toAssessmentDto(assessment);
        dto.setRoles(getUserRolesForAssessment(assessment, user));
        dto.setAllowedTargets(transitionService.allowedTargets(user, assessment));
        
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
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getBaseType() == uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType.TEACHING_SUPPORT) {
            return assessmentRepository.findAll().stream()
                    .map(EntityMapper::toAssessmentDto)
                    .collect(Collectors.toList());
        }
        
        List<Assessment> assessments = getAssessmentsForNonAdmin(userId);
        return assessments.stream()
                .map(EntityMapper::toAssessmentDto)
                .collect(Collectors.toList());
    }
    
    public AssessmentDto progressAssessment(UUID assessmentId, UUID userId, TransitionRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!transitionService.canProgress(user, assessment, request.getTargetState())) {
            throw new IllegalStateException("Not permitted to make this transition");
        }
        
        createTransition(assessment, user, request, false);
        assessment.setCurrentState(request.getTargetState());
        return EntityMapper.toAssessmentDto(assessmentRepository.save(assessment));
    }
    
    public AssessmentDto overrideTransition(UUID assessmentId, UUID userId, TransitionRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        createTransition(assessment, user, request, true);
        assessment.setCurrentState(request.getTargetState());
        return EntityMapper.toAssessmentDto(assessmentRepository.save(assessment));
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
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (role == uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole.CHECKER) {
            if (!transitionService.canBeChecker(user, assessment)) {
                throw new IllegalArgumentException("User cannot be checker - not independent");
            }
        }
        
        if (assessmentRoleRepository.findByAssessmentAndUser(assessment, user).stream()
                .anyMatch(r -> r.getRole() == role)) {
            throw new IllegalArgumentException("User already has this role");
        }
        
        assessmentRoleRepository.save(new AssessmentRoleAssignment(assessment, user, role));
    }
    
    /**
     * Remove a role assignment from an assessment
     */
    public void removeRole(UUID assessmentId, UUID userId, uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole role) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        AssessmentRoleAssignment roleToRemove = assessmentRoleRepository.findByAssessmentAndUser(assessment, user).stream()
                .filter(r -> r.getRole() == role)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        
        assessmentRoleRepository.delete(roleToRemove);
    }
    
    /**
     * Get all users assigned to an assessment with their roles
     */
    public List<AssessmentRoleAssignment> getAssessmentRoles(UUID assessmentId) {
        return assessmentRoleRepository.findByAssessment(assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found")));
    }
    
    /**
     * Submit assessment content and metadata (setter uploads their work)
     */
    public AssessmentDto submitAssessmentContent(UUID assessmentId, UUID userId, uk.ac.sheffield.Assessment_management_tool.dto.request.SubmitAssessmentRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!assessmentRoleRepository.findByAssessmentAndUser(assessment, user).stream()
                .anyMatch(r -> r.getRole() == uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole.SETTER)) {
            throw new IllegalStateException("Only setters can submit content");
        }
        
        if (assessment.getCurrentState() != AssessmentState.DRAFT) {
            throw new IllegalStateException("Can only submit from DRAFT state");
        }
        
        assessment.setDescription(request.getDescription());
        assessment.setFileName(request.getFileName());
        assessment.setFileUrl(request.getFileUrl());
        
        return EntityMapper.toAssessmentDto(assessmentRepository.save(assessment));
    }
    
    private void autoAssignModerator(Assessment assessment, Module module) {
        List<ModuleStaffRole> moderators = moduleStaffRoleRepository.findByModuleAndRole(module, ModuleRole.MODERATOR);
        if (!moderators.isEmpty()) {
            assessmentRoleRepository.save(new AssessmentRoleAssignment(assessment, moderators.get(0).getUser(), 
                    uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole.CHECKER));
        }
    }
    
    private List<String> getUserRolesForAssessment(Assessment assessment, User user) {
        List<String> roles = assessmentRoleRepository.findByAssessmentAndUser(assessment, user).stream()
                .map(r -> r.getRole().name())
                .collect(Collectors.toList());
        
        if (roles.isEmpty() && user.getBaseType() == uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType.TEACHING_SUPPORT) {
            roles.add("ADMIN");
        }
        
        return roles;
    }
    
    private List<Assessment> getAssessmentsForNonAdmin(UUID userId) {
        List<UUID> userModuleIds = moduleStaffRoleRepository.findByUserId(userId).stream()
                .map(role -> role.getModule().getId())
                .collect(Collectors.toList());
        
        List<Assessment> moduleAssessments = userModuleIds.stream()
                .flatMap(moduleId -> assessmentRepository.findByModuleId(moduleId).stream())
                .collect(Collectors.toList());
        
        List<Assessment> roleAssessments = assessmentRoleRepository.findByUserId(userId).stream()
                .map(AssessmentRoleAssignment::getAssessment)
                .collect(Collectors.toList());
        
        List<Assessment> combined = new java.util.ArrayList<>(moduleAssessments);
        roleAssessments.forEach(a -> { if (!combined.contains(a)) combined.add(a); });
        return combined;
    }
    
    private void createTransition(Assessment assessment, User user, TransitionRequest request, boolean isOverride) {
        AssessmentTransition transition = new AssessmentTransition();
        transition.setAssessment(assessment);
        transition.setFromState(assessment.getCurrentState());
        transition.setToState(request.getTargetState());
        transition.setByUser(user);
        transition.setByDisplayName(user.getName());
        transition.setNote(request.getNote());
        transition.setOverride(isOverride);
        transition.setReversion(false);
        transitionRepository.save(transition);
    }
}
