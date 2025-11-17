package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.ImportJobStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "csv_import_job")
public class CsvImportJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportJobStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String errors;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (status == null) {
            status = ImportJobStatus.PENDING;
        }
    }
    
    // Constructors
    public CsvImportJob() {}
    
    public CsvImportJob(String fileName) {
        this.fileName = fileName;
        this.status = ImportJobStatus.PENDING;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public ImportJobStatus getStatus() {
        return status;
    }
    
    public void setStatus(ImportJobStatus status) {
        this.status = status;
    }
    
    public String getErrors() {
        return errors;
    }
    
    public void setErrors(String errors) {
        this.errors = errors;
    }
}
