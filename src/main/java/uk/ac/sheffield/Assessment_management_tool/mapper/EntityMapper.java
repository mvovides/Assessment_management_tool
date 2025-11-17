package uk.ac.sheffield.Assessment_management_tool.mapper;

import uk.ac.sheffield.Assessment_management_tool.domain.entity.*;
import uk.ac.sheffield.Assessment_management_tool.dto.response.*;

public class EntityMapper {
    
    public static UserDto toUserDto(User user) {
        if (user == null) return null;
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBaseType(user.getBaseType());
        dto.setExamsOfficer(user.isExamsOfficer());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
    
    public static ModuleDto toModuleDto(uk.ac.sheffield.Assessment_management_tool.domain.entity.Module module) {
        if (module == null) return null;
        
        ModuleDto dto = new ModuleDto();
        dto.setId(module.getId());
        dto.setCode(module.getCode());
        dto.setTitle(module.getTitle());
        dto.setAcademicYear(module.getAcademicYear());
        return dto;
    }
    
    public static AssessmentDto toAssessmentDto(Assessment assessment) {
        if (assessment == null) return null;
        
        AssessmentDto dto = new AssessmentDto();
        dto.setId(assessment.getId());
        dto.setModuleId(assessment.getModule().getId());
        dto.setModuleCode(assessment.getModule().getCode());
        dto.setModuleTitle(assessment.getModule().getTitle());
        dto.setTitle(assessment.getTitle());
        dto.setType(assessment.getType());
        dto.setCurrentState(assessment.getCurrentState());
        dto.setExamDate(assessment.getExamDate());
        dto.setDescription(assessment.getDescription());
        dto.setFileName(assessment.getFileName());
        dto.setFileUrl(assessment.getFileUrl());
        return dto;
    }
    
    public static TransitionDto toTransitionDto(AssessmentTransition transition) {
        if (transition == null) return null;
        
        TransitionDto dto = new TransitionDto();
        dto.setId(transition.getId());
        dto.setAssessmentId(transition.getAssessment().getId());
        dto.setFromState(transition.getFromState());
        dto.setToState(transition.getToState());
        dto.setAt(transition.getAt());
        dto.setByUserId(transition.getByUser() != null ? transition.getByUser().getId() : null);
        dto.setByDisplayName(transition.getByDisplayName());
        dto.setNote(transition.getNote());
        dto.setOverride(transition.isOverride());
        dto.setReversion(transition.isReversion());
        dto.setRevertedTransitionId(transition.getRevertedTransition() != null ? 
            transition.getRevertedTransition().getId() : null);
        return dto;
    }
    
    public static FeedbackDto toFeedbackDto(CheckerFeedback feedback) {
        if (feedback == null) return null;
        
        FeedbackDto dto = new FeedbackDto();
        dto.setId(feedback.getId());
        dto.setAssessmentId(feedback.getAssessment().getId());
        dto.setAuthorId(feedback.getAuthor().getId());
        dto.setAuthorName(feedback.getAuthor().getName());
        dto.setText(feedback.getText());
        dto.setSecureDocRef(feedback.getSecureDocRef());
        dto.setCreatedAt(feedback.getCreatedAt());
        return dto;
    }
    
    public static FeedbackDto toFeedbackDto(ExternalExaminerFeedback feedback) {
        if (feedback == null) return null;
        
        FeedbackDto dto = new FeedbackDto();
        dto.setId(feedback.getId());
        dto.setAssessmentId(feedback.getAssessment().getId());
        dto.setAuthorId(feedback.getExaminer().getId());
        dto.setAuthorName(feedback.getExaminer().getName());
        dto.setText(feedback.getFeedback());
        dto.setCreatedAt(feedback.getCreatedAt());
        return dto;
    }
    
    public static FeedbackDto toFeedbackDto(SetterResponse response) {
        if (response == null) return null;
        
        FeedbackDto dto = new FeedbackDto();
        dto.setId(response.getId());
        dto.setAssessmentId(response.getAssessment().getId());
        dto.setAuthorId(response.getAuthor().getId());
        dto.setAuthorName(response.getAuthor().getName());
        dto.setText(response.getResponseText());
        dto.setSecureDocRef(response.getSecureDocRef());
        dto.setCreatedAt(response.getCreatedAt());
        return dto;
    }
}
