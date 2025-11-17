package uk.ac.sheffield.Assessment_management_tool.dto.request;

import jakarta.validation.constraints.NotBlank;

public class FeedbackRequest {
    
    @NotBlank(message = "Feedback text is required")
    private String text;
    
    private String secureDocRef;
    
    // Constructors
    public FeedbackRequest() {}
    
    public FeedbackRequest(String text, String secureDocRef) {
        this.text = text;
        this.secureDocRef = secureDocRef;
    }
    
    // Getters and Setters
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getSecureDocRef() {
        return secureDocRef;
    }
    
    public void setSecureDocRef(String secureDocRef) {
        this.secureDocRef = secureDocRef;
    }
}
