package uk.ac.sheffield.Assessment_management_tool.dto.request;

import uk.ac.sheffield.Assessment_management_tool.domain.enums.AssessmentRole;

import java.util.UUID;

public class AssignRoleRequest {
    private UUID userId;
    private AssessmentRole role;

    public AssignRoleRequest() {
    }

    public AssignRoleRequest(UUID userId, AssessmentRole role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AssessmentRole getRole() {
        return role;
    }

    public void setRole(AssessmentRole role) {
        this.role = role;
    }
}
