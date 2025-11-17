package uk.ac.sheffield.Assessment_management_tool.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class FeedbackDto {
    
    private UUID id;
    private UUID assessmentId;
    private UUID authorId;
    private String authorName;
    private String text;
    private String secureDocRef;
    private OffsetDateTime createdAt;
    
    // Constructors
    public FeedbackDto() {}
    
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
    
    public UUID getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getSecureDocRef() {
        return secureDocRef;
    }
    
    public void setSecureDocRef(String secureDocRef) {
        this.secureDocRef = secureDocRef;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
