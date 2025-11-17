package uk.ac.sheffield.Assessment_management_tool.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentType;

import java.time.LocalDate;

public class CreateAssessmentRequest {
    
    @NotBlank(message = "Assessment title is required")
    private String title;
    
    @NotNull(message = "Assessment type is required")
    private AssessmentType type;
    
    private LocalDate examDate;
    
    // Constructors
    public CreateAssessmentRequest() {}
    
    public CreateAssessmentRequest(String title, AssessmentType type, LocalDate examDate) {
        this.title = title;
        this.type = type;
        this.examDate = examDate;
    }
    
    // Getters and Setters
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
    
    public LocalDate getExamDate() {
        return examDate;
    }
    
    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }
}
