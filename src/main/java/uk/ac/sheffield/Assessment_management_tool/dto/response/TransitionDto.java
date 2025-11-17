package uk.ac.sheffield.Assessment_management_tool.dto.response;

import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TransitionDto {
    
    private UUID id;
    private UUID assessmentId;
    private AssessmentState fromState;
    private AssessmentState toState;
    private OffsetDateTime at;
    private UUID byUserId;
    private String byDisplayName;
    private String note;
    private boolean isOverride;
    private boolean isReversion;
    private UUID revertedTransitionId;
    
    // Constructors
    public TransitionDto() {}
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAssessmentId() {
        return assessmentId;
    }
    
    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }
    
    public AssessmentState getFromState() {
        return fromState;
    }
    
    public void setFromState(AssessmentState fromState) {
        this.fromState = fromState;
    }
    
    public AssessmentState getToState() {
        return toState;
    }
    
    public void setToState(AssessmentState toState) {
        this.toState = toState;
    }
    
    public OffsetDateTime getAt() {
        return at;
    }
    
    public void setAt(OffsetDateTime at) {
        this.at = at;
    }
    
    public UUID getByUserId() {
        return byUserId;
    }
    
    public void setByUserId(UUID byUserId) {
        this.byUserId = byUserId;
    }
    
    public String getByDisplayName() {
        return byDisplayName;
    }
    
    public void setByDisplayName(String byDisplayName) {
        this.byDisplayName = byDisplayName;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public boolean isOverride() {
        return isOverride;
    }
    
    public void setOverride(boolean override) {
        isOverride = override;
    }
    
    public boolean isReversion() {
        return isReversion;
    }
    
    public void setReversion(boolean reversion) {
        isReversion = reversion;
    }
    
    public UUID getRevertedTransitionId() {
        return revertedTransitionId;
    }
    
    public void setRevertedTransitionId(UUID revertedTransitionId) {
        this.revertedTransitionId = revertedTransitionId;
    }
}
