package uk.ac.sheffield.Assessment_management_tool.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SubmitAssessmentRequest {
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String fileName;
    private String fileUrl;
    
    // Constructors
    public SubmitAssessmentRequest() {}
    
    public SubmitAssessmentRequest(String description, String fileName, String fileUrl) {
        this.description = description;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
    
    // Getters and Setters
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
}
