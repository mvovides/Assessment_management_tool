package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assessment_transition")
public class AssessmentTransition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", nullable = false)
    private AssessmentState fromState;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false)
    private AssessmentState toState;
    
    @NotNull
    @Column(nullable = false, updatable = false)
    private OffsetDateTime at;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "by_user_id")
    private User byUser;
    
    @NotBlank
    @Column(name = "by_display_name", nullable = false)
    private String byDisplayName;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "is_override", nullable = false)
    private boolean isOverride = false;
    
    @Column(name = "is_reversion", nullable = false)
    private boolean isReversion = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reverted_transition_id")
    private AssessmentTransition revertedTransition;
    
    @PrePersist
    protected void onCreate() {
        at = OffsetDateTime.now();
    }
    
    // Constructors
    public AssessmentTransition() {}
    
    public AssessmentTransition(Assessment assessment, AssessmentState fromState, 
                                AssessmentState toState, User byUser, String byDisplayName) {
        this.assessment = assessment;
        this.fromState = fromState;
        this.toState = toState;
        this.byUser = byUser;
        this.byDisplayName = byDisplayName;
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
    
    public User getByUser() {
        return byUser;
    }
    
    public void setByUser(User byUser) {
        this.byUser = byUser;
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
    
    public AssessmentTransition getRevertedTransition() {
        return revertedTransition;
    }
    
    public void setRevertedTransition(AssessmentTransition revertedTransition) {
        this.revertedTransition = revertedTransition;
    }
}
