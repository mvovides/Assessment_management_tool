package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole;

import java.util.UUID;

@Entity
@Table(
    name = "assessment_role",
    uniqueConstraints = @UniqueConstraint(columnNames = {"assessment_id", "user_id", "role"})
)
public class AssessmentRoleAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentRole role;
    
    // Constructors
    public AssessmentRoleAssignment() {}
    
    public AssessmentRoleAssignment(Assessment assessment, User user, AssessmentRole role) {
        this.assessment = assessment;
        this.user = user;
        this.role = role;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public AssessmentRole getRole() {
        return role;
    }
    
    public void setRole(AssessmentRole role) {
        this.role = role;
    }
}
