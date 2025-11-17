package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "base_type", nullable = false)
    private UserBaseType baseType;
    
    @Column(name = "is_exams_officer", nullable = false)
    private boolean isExamsOfficer = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
    
    // Constructors
    public User() {}
    
    public User(String name, String email, String passwordHash, UserBaseType baseType) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.baseType = baseType;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public UserBaseType getBaseType() {
        return baseType;
    }
    
    public void setBaseType(UserBaseType baseType) {
        this.baseType = baseType;
    }
    
    public boolean isExamsOfficer() {
        return isExamsOfficer;
    }
    
    public void setExamsOfficer(boolean examsOfficer) {
        isExamsOfficer = examsOfficer;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
