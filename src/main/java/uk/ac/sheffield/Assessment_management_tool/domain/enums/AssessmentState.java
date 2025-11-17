package uk.ac.sheffield.Assessment_management_tool.domain.enums;

public enum AssessmentState {
    // Common states (Steps 1-2)
    DRAFT,                    // Initial state - setter creates assessment
    READY_FOR_CHECK,          // Setter submits for checking
    CHANGES_REQUIRED,         // Checker requests changes
    
    // Coursework specific (Steps 3-9)
    RELEASED,                 // Released to students
    DEADLINE_PASSED,          // Submission deadline passed
    
    // Test specific (Steps 3-8)
    TEST_TAKEN,               // Test has taken place
    
    // Exam specific (Steps 3-12)
    EXAM_OFFICER_CHECK,       // Exams officer checking
    EXAM_CHANGES_REQUIRED,    // EO requests changes
    EXTERNAL_FEEDBACK,        // Awaiting external examiner feedback
    SETTER_RESPONSE,          // Setter responding to external feedback
    FINAL_CHECK,              // Exams officer final check
    SENT_TO_PRINTING,         // Sent to exams office for printing
    EXAM_TAKEN,               // Exam has taken place
    
    // Common post-completion states
    MARKING,                  // Marking in progress (includes standardisation)
    ADMIN_MARK_CHECK,         // Admin checking marks (exams only)
    MODERATED,                // Moderation complete
    
    // Final states
    FEEDBACK_RETURNED,        // Feedback returned to students (CW)
    RESULTS_RETURNED,         // Results returned to students (Test)
    PUBLISHED,                // Marks approved and published (all types)
    APPROVED                  // Final approval through formal process
}
