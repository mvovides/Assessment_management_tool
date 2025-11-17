package uk.ac.sheffield.Assessment_management_tool.dto.request;

import jakarta.validation.constraints.NotNull;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState;

public class TransitionRequest {
    
    @NotNull(message = "Target state is required")
    private AssessmentState targetState;
    
    private String note;
    
    // Constructors
    public TransitionRequest() {}
    
    public TransitionRequest(AssessmentState targetState, String note) {
        this.targetState = targetState;
        this.note = note;
    }
    
    // Getters and Setters
    public AssessmentState getTargetState() {
        return targetState;
    }
    
    public void setTargetState(AssessmentState targetState) {
        this.targetState = targetState;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
}
