package uk.ac.sheffield.Assessment_management_tool.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType;

public class CreateUserRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotNull(message = "User type is required")
    private UserBaseType baseType;
    
    private boolean isExamsOfficer = false;
    
    // Constructors
    public CreateUserRequest() {}
    
    public CreateUserRequest(String name, String email, UserBaseType baseType, boolean isExamsOfficer) {
        this.name = name;
        this.email = email;
        this.baseType = baseType;
        this.isExamsOfficer = isExamsOfficer;
    }
    
    // Getters and Setters
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
}
