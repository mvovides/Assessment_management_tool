package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "setter_response")
public class SetterResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;
    
    @NotBlank
    @Column(name = "response_text", columnDefinition = "TEXT", nullable = false)
    private String responseText;
    
    @Column(name = "secure_doc_ref")
    private String secureDocRef;
    
    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
    
    // Constructors
    public SetterResponse() {}
    
    public SetterResponse(Assessment assessment, User author, String responseText) {
        this.assessment = assessment;
        this.author = author;
        this.responseText = responseText;
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
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public String getResponseText() {
        return responseText;
    }
    
    public void setResponseText(String responseText) {
        this.responseText = responseText;
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
