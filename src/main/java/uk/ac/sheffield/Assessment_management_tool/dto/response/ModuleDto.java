package uk.ac.sheffield.Assessment_management_tool.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModuleDto {
    
    private UUID id;
    private String code;
    private String title;
    private String academicYear;
    private List<ModuleStaffDto> staff = new ArrayList<>();
    private List<UserDto> externalExaminers = new ArrayList<>();
    private int staffCount;
    private int assessmentCount;
    private String userRole; // Current user's role on this module (MODULE_LEAD, MODERATOR, STAFF)
    
    // Constructors
    public ModuleDto() {}
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
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
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public List<ModuleStaffDto> getStaff() {
        return staff;
    }
    
    public void setStaff(List<ModuleStaffDto> staff) {
        this.staff = staff;
    }
    
    public int getStaffCount() {
        return staffCount;
    }
    
    public void setStaffCount(int staffCount) {
        this.staffCount = staffCount;
    }
    
    public int getAssessmentCount() {
        return assessmentCount;
    }
    
    public void setAssessmentCount(int assessmentCount) {
        this.assessmentCount = assessmentCount;
    }
    
    public List<UserDto> getExternalExaminers() {
        return externalExaminers;
    }
    
    public void setExternalExaminers(List<UserDto> externalExaminers) {
        this.externalExaminers = externalExaminers;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
