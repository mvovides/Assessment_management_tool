package uk.ac.sheffield.Assessment_management_tool.dto.response;

import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentType;

import java.time.LocalDate;
import java.util.UUID;

public class AssessmentDto {
    
    private UUID id;
    private UUID moduleId;
    private String moduleCode;
    private String moduleTitle;
    private String title;
    private AssessmentType type;
    private AssessmentState currentState;
    private LocalDate examDate;
    private String description;
    private String fileName;
    private String fileUrl;
    private java.util.List<String> roles;  // User's roles on this assessment
    private java.util.List<uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState> allowedTargets;  // States user can transition to
    
    // Constructors
    public AssessmentDto() {}
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getModuleId() {
        return moduleId;
    }
    
    public void setModuleId(UUID moduleId) {
        this.moduleId = moduleId;
    }
    
    public String getModuleCode() {
        return moduleCode;
    }
    
    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }
    
    public String getModuleTitle() {
        return moduleTitle;
    }
    
    public void setModuleTitle(String moduleTitle) {
        this.moduleTitle = moduleTitle;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public AssessmentType getType() {
        return type;
    }
    
    public void setType(AssessmentType type) {
        this.type = type;
    }
    
    public AssessmentState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(AssessmentState currentState) {
        this.currentState = currentState;
    }
    
    public LocalDate getExamDate() {
        return examDate;
    }
    
    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public java.util.List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(java.util.List<String> roles) {
        this.roles = roles;
    }
    
    public java.util.List<uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState> getAllowedTargets() {
        return allowedTargets;
    }
    
    public void setAllowedTargets(java.util.List<uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState> allowedTargets) {
        this.allowedTargets = allowedTargets;
    }
}
