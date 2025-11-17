package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "external_examiner_feedback")
public class ExternalExaminerFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examiner_user_id", nullable = false)
    private User examiner;
    
    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String feedback;
    
    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
    
    // Constructors
    public ExternalExaminerFeedback() {}
    
    public ExternalExaminerFeedback(Assessment assessment, User examiner, String feedback) {
        this.assessment = assessment;
        this.examiner = examiner;
        this.feedback = feedback;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Assessment getAssessment() {
        return assessment;
    }
    
    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }
    
    public User getExaminer() {
        return examiner;
    }
    
    public void setExaminer(User examiner) {
        this.examiner = examiner;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
