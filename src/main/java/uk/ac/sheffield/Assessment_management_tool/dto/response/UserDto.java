package uk.ac.sheffield.Assessment_management_tool.dto.response;

import uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserDto {
    
    private UUID id;
    private String name;
    private String email;
    private UserBaseType baseType;
    private boolean isExamsOfficer;
    private OffsetDateTime createdAt;
    
    // Constructors
    public UserDto() {}
    
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
