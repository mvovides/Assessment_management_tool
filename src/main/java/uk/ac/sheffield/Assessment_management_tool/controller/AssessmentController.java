package uk.ac.sheffield.Assessment_management_tool.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uk.ac.sheffield.Assessment_management_tool.dto.request.CreateAssessmentRequest;
import uk.ac.sheffield.Assessment_management_tool.dto.request.TransitionRequest;
import uk.ac.sheffield.Assessment_management_tool.dto.response.AssessmentDto;
import uk.ac.sheffield.Assessment_management_tool.dto.response.TransitionDto;
import uk.ac.sheffield.Assessment_management_tool.security.CustomUserDetails;
import uk.ac.sheffield.Assessment_management_tool.service.AssessmentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AssessmentController {
    
    private final AssessmentService assessmentService;
    
    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }
    
    @GetMapping("/assessments")
    public ResponseEntity<List<AssessmentDto>> getAllAssessments() {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(assessmentService.getAllAssessmentsForUser(userId));
    }
    
    @GetMapping("/assessments/{id}")
    public ResponseEntity<AssessmentDto> getAssessmentById(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(assessmentService.getAssessmentByIdWithUserContext(id, userId));
    }
    
    @GetMapping("/modules/{moduleId}/assessments")
    public ResponseEntity<List<AssessmentDto>> getModuleAssessments(@PathVariable UUID moduleId) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByModule(moduleId));
    }
    
    @PostMapping("/admin/modules/{moduleId}/assessments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssessmentDto> createAssessment(
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateAssessmentRequest request) {
        AssessmentDto assessment = assessmentService.createAssessment(moduleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assessment);
    }
    
    @GetMapping("/assessments/{id}/transitions")
    public ResponseEntity<List<TransitionDto>> getTransitions(@PathVariable UUID id) {
        return ResponseEntity.ok(assessmentService.getAssessmentTransitions(id));
    }
    
    @PostMapping("/assessments/{id}/progress")
    public ResponseEntity<AssessmentDto> progressAssessment(
            @PathVariable UUID id,
            @Valid @RequestBody TransitionRequest request) {
        
        UUID userId = getCurrentUserId();
        AssessmentDto assessment = assessmentService.progressAssessment(id, userId, request);
        return ResponseEntity.ok(assessment);
    }
    
    @PostMapping("/admin/assessments/{id}/override")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXAMS_OFFICER')")
    public ResponseEntity<AssessmentDto> overrideTransition(
            @PathVariable UUID id,
            @Valid @RequestBody TransitionRequest request) {
        
        UUID userId = getCurrentUserId();
        AssessmentDto assessment = assessmentService.overrideTransition(id, userId, request);
        return ResponseEntity.ok(assessment);
    }
    
    /**
     * Assign a role (SETTER or CHECKER) to a user for an assessment
     * POST /api/assessments/{assessmentId}/roles
     * Body: { "userId": "uuid", "role": "SETTER" or "CHECKER" }
     */
    @PostMapping("/assessments/{assessmentId}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODULE_LEAD')")
    public ResponseEntity<String> assignRole(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody uk.ac.sheffield.Assessment_management_tool.dto.request.AssignRoleRequest request) {
        
        assessmentService.assignRole(assessmentId, request.getUserId(), request.getRole());
        return ResponseEntity.ok("Role assigned successfully");
    }
    
    /**
     * Remove a role assignment from an assessment
     * DELETE /api/assessments/{assessmentId}/roles/{userId}/{role}
     */
    @DeleteMapping("/assessments/{assessmentId}/roles/{userId}/{role}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODULE_LEAD')")
    public ResponseEntity<String> removeRole(
            @PathVariable UUID assessmentId,
            @PathVariable UUID userId,
            @PathVariable uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole role) {
        
        assessmentService.removeRole(assessmentId, userId, role);
        return ResponseEntity.ok("Role removed successfully");
    }
    
    /**
     * Get all users assigned to an assessment with their roles
     * GET /api/assessments/{assessmentId}/roles
     */
    @GetMapping("/assessments/{assessmentId}/roles")
    public ResponseEntity<List<uk.ac.sheffield.Assessment_management_tool.dto.response.AssessmentRoleDto>> getAssessmentRoles(@PathVariable UUID assessmentId) {
        var roles = assessmentService.getAssessmentRoles(assessmentId);
        List<uk.ac.sheffield.Assessment_management_tool.dto.response.AssessmentRoleDto> result = roles.stream()
                .map(r -> new uk.ac.sheffield.Assessment_management_tool.dto.response.AssessmentRoleDto(
                    r.getUser().getId(),
                    r.getUser().getName(),
                    r.getUser().getEmail(),
                    r.getUser().getBaseType().name(),
                    r.getRole()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
    
    /**
     * Submit assessment content (setter uploads assessment details)
     * POST /api/assessments/{assessmentId}/submit-content
     * Body: { "description": "...", "fileName": "...", "fileUrl": "..." }
     */
    @PostMapping("/assessments/{assessmentId}/submit-content")
    public ResponseEntity<AssessmentDto> submitAssessmentContent(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody uk.ac.sheffield.Assessment_management_tool.dto.request.SubmitAssessmentRequest request) {
        
        UUID userId = getCurrentUserId();
        AssessmentDto assessment = assessmentService.submitAssessmentContent(assessmentId, userId, request);
        return ResponseEntity.ok(assessment);
    }
    
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }
}
