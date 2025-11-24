package uk.ac.sheffield.Assessment_management_tool.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateModuleRequest {
    
    @NotBlank(message = "Module code is required")
    private String code;
    
    @NotBlank(message = "Module title is required")
    private String title;
    
    @NotNull(message = "Module lead is required")
    private UUID moduleLeadId;
    
    @NotNull(message = "Module moderator is required")
    private UUID moduleModeratorId;
    
    private List<UUID> staffIds = new ArrayList<>();
    
    // Constructors
    public CreateModuleRequest() {}
    
    public CreateModuleRequest(String code, String title) {
        this.code = code;
        this.title = title;
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public UUID getModuleLeadId() {
        return moduleLeadId;
    }
    
    public void setModuleLeadId(UUID moduleLeadId) {
        this.moduleLeadId = moduleLeadId;
    }
    
    public UUID getModuleModeratorId() {
        return moduleModeratorId;
    }
    
    public void setModuleModeratorId(UUID moduleModeratorId) {
        this.moduleModeratorId = moduleModeratorId;
    }
    
    public List<UUID> getStaffIds() {
        return staffIds;
    }
    
    public void setStaffIds(List<UUID> staffIds) {
        this.staffIds = staffIds;
    }
}
