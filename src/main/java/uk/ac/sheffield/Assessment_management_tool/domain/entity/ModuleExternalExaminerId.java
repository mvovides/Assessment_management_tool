package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ModuleExternalExaminerId implements Serializable {
    
    private UUID moduleId;
    private UUID userId;
    
    // Constructors
    public ModuleExternalExaminerId() {}
    
    public ModuleExternalExaminerId(UUID moduleId, UUID userId) {
        this.moduleId = moduleId;
        this.userId = userId;
    }
    
    // Getters and Setters
    public UUID getModuleId() {
        return moduleId;
    }
    
    public void setModuleId(UUID moduleId) {
        this.moduleId = moduleId;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleExternalExaminerId that = (ModuleExternalExaminerId) o;
        return Objects.equals(moduleId, that.moduleId) && Objects.equals(userId, that.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(moduleId, userId);
    }
}
