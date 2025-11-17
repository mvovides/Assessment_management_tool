package uk.ac.sheffield.Assessment_management_tool.dto.response;

import java.util.List;

public class UserProfileDto {
    
    private UserDto user;
    private List<String> roles;
    
    // Constructors
    public UserProfileDto() {}
    
    public UserProfileDto(UserDto user, List<String> roles) {
        this.user = user;
        this.roles = roles;
    }
    
    // Getters and Setters
    public UserDto getUser() {
        return user;
    }
    
    public void setUser(UserDto user) {
        this.user = user;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
