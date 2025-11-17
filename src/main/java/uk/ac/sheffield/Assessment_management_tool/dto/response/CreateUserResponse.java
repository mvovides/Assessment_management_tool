package uk.ac.sheffield.Assessment_management_tool.dto.response;

public class CreateUserResponse {
    
    private UserDto user;
    private String temporaryPassword;
    
    // Constructors
    public CreateUserResponse() {}
    
    public CreateUserResponse(UserDto user, String temporaryPassword) {
        this.user = user;
        this.temporaryPassword = temporaryPassword;
    }
    
    // Getters and Setters
    public UserDto getUser() {
        return user;
    }
    
    public void setUser(UserDto user) {
        this.user = user;
    }
    
    public String getTemporaryPassword() {
        return temporaryPassword;
    }
    
    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }
}
