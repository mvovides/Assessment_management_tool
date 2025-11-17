package uk.ac.sheffield.Assessment_management_tool.dto.response;

import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole;

import java.util.UUID;

public class AssessmentRoleDto {
    private UUID id;
    private String name;
    private String email;
    private String baseType;
    private AssessmentRole role;

    public AssessmentRoleDto() {
    }

    public AssessmentRoleDto(UUID id, String name, String email, String baseType, AssessmentRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.baseType = baseType;
        this.role = role;
    }

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

    public String getBaseType() {
        return baseType;
    }

    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }

    public AssessmentRole getRole() {
        return role;
    }

    public void setRole(AssessmentRole role) {
        this.role = role;
    }
}
