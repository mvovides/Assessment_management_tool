package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentState;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentType;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "assessment")
public class Assessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    
    @NotBlank
    @Column(nullable = false)
    private String title;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentType type;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false)
    private AssessmentState currentState;
    
    @Column(name = "exam_date")
    private LocalDate examDate;
    
    @Column(name = "description", columnDefinition = "CLOB")
    private String description;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_url")
    private String fileUrl;
    
    @Version
    private Long version;
    
    // Constructors
    public Assessment() {
        this.currentState = AssessmentState.DRAFT;
    }
    
    public Assessment(Module module, String title, AssessmentType type) {
        this.module = module;
        this.title = title;
        this.type = type;
        this.currentState = AssessmentState.DRAFT;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Module getModule() {
        return module;
    }
    
    public void setModule(Module module) {
        this.module = module;
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
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
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
}
