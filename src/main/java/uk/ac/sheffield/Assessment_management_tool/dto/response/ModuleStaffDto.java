package uk.ac.sheffield.Assessment_management_tool.dto.response;

import uk.ac.sheffield.Assessment_management_tool.domain.enums.ModuleRole;

import java.util.UUID;

public class ModuleStaffDto {
    
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private ModuleRole role;
    
    // Constructors
    public ModuleStaffDto() {}
    
    public ModuleStaffDto(UUID id, UUID userId, String name, String email, ModuleRole role) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
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
    
    public ModuleRole getRole() {
        return role;
    }
    
    public void setRole(ModuleRole role) {
        this.role = role;
    }
}
