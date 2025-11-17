# Assessment Workflows

## Overview
The Assessment Management Tool implements three distinct assessment workflows with role-based permissions:
- **Coursework (CW)** - 9 steps
- **In-Semester Test (TEST)** - 8 steps  
- **Examination (EXAM)** - 12 steps

Each workflow follows a state machine pattern with specific transitions controlled by user roles.

## Assessment States

### Common States (All Types)
1. **DRAFT** - Initial state, assessment being created
2. **READY_FOR_CHECK** - Submitted for checking
3. **CHANGES_REQUIRED** - Checker requests modifications

### Coursework Specific States
4. **RELEASED** - Published to students
5. **DEADLINE_PASSED** - Submission deadline has passed
6. **MARKING** - Marking in progress (includes standardisation)
7. **MODERATED** - Moderation complete
8. **FEEDBACK_RETURNED** - Feedback returned to students
9. **APPROVED** - Formally approved
10. **PUBLISHED** - Marks published

### Test Specific States
4. **TEST_TAKEN** - Test has taken place
5. **MARKING** - Marking in progress (includes standardisation)
6. **MODERATED** - Moderation complete
7. **RESULTS_RETURNED** - Results returned to students
8. **APPROVED** - Formally approved
9. **PUBLISHED** - Marks published

### Exam Specific States
4. **EXAM_OFFICER_CHECK** - Exams Officer reviewing
5. **EXAM_CHANGES_REQUIRED** - EO requests modifications
6. **EXTERNAL_FEEDBACK** - Awaiting External Examiner feedback
7. **SETTER_RESPONSE** - Setter responding to external feedback
8. **FINAL_CHECK** - Exams Officer final review
9. **SENT_TO_PRINTING** - Sent to printing
10. **EXAM_TAKEN** - Exam has taken place
11. **MARKING** - Marking in progress (includes standardisation)
12. **ADMIN_MARK_CHECK** - Admin checking marks
13. **MODERATED** - Moderation complete
14. **APPROVED** - Formally approved
15. **PUBLISHED** - Marks published

---

## Workflow Details

### 1. COURSEWORK (CW) Workflow

#### State Flow
```
DRAFT → READY_FOR_CHECK → [CHANGES_REQUIRED ↔ READY_FOR_CHECK] → RELEASED → 
DEADLINE_PASSED → MARKING → MODERATED → FEEDBACK_RETURNED → APPROVED → PUBLISHED
```

#### Step-by-Step Process

**Step 1: Create (DRAFT)**
- **Who**: Setter (module staff member assigned as SETTER)
- **Action**: Create assessment details, questions, marking scheme
- **Next**: Submit for checking

**Step 2: Check (READY_FOR_CHECK)**
- **Who**: Checker (independent academic, auto-assigned from Module Moderator)
- **Action**: Review assessment for quality, clarity, and appropriateness
- **Next**: Approve (→ RELEASED) or Request Changes (→ CHANGES_REQUIRED)

**Step 2a: Modifications (CHANGES_REQUIRED)**
- **Who**: Setter
- **Action**: Address checker's feedback and resubmit
- **Next**: Re-submit for checking (→ READY_FOR_CHECK)
- **Note**: Can loop multiple times until checker approves

**Step 3: Release (RELEASED)**
- **Who**: Checker approves transition
- **Action**: Assessment published to students on VLE/system
- **Next**: Wait for deadline

**Step 4: Deadline (DEADLINE_PASSED)**
- **Who**: Module Lead (or system automatically)
- **Action**: Record that submission deadline has passed
- **Next**: Begin marking

**Step 5: Marking (MARKING)**
- **Who**: Marking team (module staff, may include setter)
- **Action**: Mark submissions, includes standardisation if team marking
- **Next**: Submit for moderation

**Step 6: Moderation (MODERATED)**
- **Who**: Module Moderator
- **Action**: Review sample of marked work, ensure consistency
- **Next**: Return feedback

**Step 7: Feedback (FEEDBACK_RETURNED)**
- **Who**: Module Lead
- **Action**: Release feedback and provisional marks to students
- **Next**: Submit for approval

**Step 8: Approval (APPROVED)**
- **Who**: Module Lead (school exam board approval)
- **Action**: Formal approval through exam board process
- **Next**: Publish final marks

**Step 9: Publish (PUBLISHED)**
- **Who**: Teaching Support / Admin
- **Action**: Final publication of marks to central system
- **Final State**: Workflow complete

---

### 2. IN-SEMESTER TEST (TEST) Workflow

#### State Flow
```
DRAFT → READY_FOR_CHECK → [CHANGES_REQUIRED ↔ READY_FOR_CHECK] → TEST_TAKEN → 
MARKING → MODERATED → RESULTS_RETURNED → APPROVED → PUBLISHED
```

#### Step-by-Step Process

**Step 1: Create (DRAFT)**
- **Who**: Setter
- **Action**: Create test questions and marking scheme
- **Next**: Submit for checking

**Step 2: Check (READY_FOR_CHECK)**
- **Who**: Checker (Module Moderator)
- **Action**: Review test quality and appropriateness
- **Next**: Approve (→ TEST_TAKEN) or Request Changes (→ CHANGES_REQUIRED)

**Step 2a: Modifications (CHANGES_REQUIRED)**
- **Who**: Setter
- **Action**: Address feedback and resubmit
- **Next**: Re-submit for checking

**Step 3: Test (TEST_TAKEN)**
- **Who**: Module Lead (or checker approves and test is scheduled)
- **Action**: Test takes place, scripts collected
- **Next**: Begin marking

**Step 4: Marking (MARKING)**
- **Who**: Marking team (includes standardisation if team)
- **Action**: Mark test scripts
- **Next**: Submit for moderation

**Step 5: Moderation (MODERATED)**
- **Who**: Module Moderator
- **Action**: Review marked scripts for consistency
- **Next**: Return results

**Step 6: Results (RESULTS_RETURNED)**
- **Who**: Module Lead
- **Action**: Release results to students
- **Next**: Submit for approval

**Step 7: Approval (APPROVED)**
- **Who**: Module Lead (school exam board)
- **Action**: Formal approval of marks
- **Next**: Publish

**Step 8: Publish (PUBLISHED)**
- **Who**: Teaching Support / Admin
- **Action**: Final publication of marks
- **Final State**: Workflow complete

---

### 3. EXAMINATION (EXAM) Workflow

#### State Flow
```
DRAFT → READY_FOR_CHECK → [CHANGES_REQUIRED ↔ READY_FOR_CHECK] → EXAM_OFFICER_CHECK → 
[EXAM_CHANGES_REQUIRED ↔ EXAM_OFFICER_CHECK] → EXTERNAL_FEEDBACK → SETTER_RESPONSE → 
FINAL_CHECK → [EXAM_CHANGES_REQUIRED or SENT_TO_PRINTING] → EXAM_TAKEN → MARKING → 
ADMIN_MARK_CHECK → MODERATED → APPROVED → PUBLISHED
```

#### Step-by-Step Process

**Step 1: Create (DRAFT)**
- **Who**: Setter
- **Action**: Create exam paper, marking scheme, rubrics
- **Next**: Submit for checking

**Step 2: Check (READY_FOR_CHECK)**
- **Who**: Checker (Module Moderator)
- **Action**: Internal check of exam quality
- **Next**: Approve (→ EXAM_OFFICER_CHECK) or Request Changes (→ CHANGES_REQUIRED)

**Step 2a: Modifications (CHANGES_REQUIRED)**
- **Who**: Setter
- **Action**: Address feedback
- **Next**: Re-submit for checking

**Step 3: EO Check (EXAM_OFFICER_CHECK)**
- **Who**: Exams Officer
- **Action**: Check formatting, rubrics, exam regulations compliance
- **Next**: Send to External (→ EXTERNAL_FEEDBACK) or Request Changes (→ EXAM_CHANGES_REQUIRED)

**Step 3a: EO Modifications (EXAM_CHANGES_REQUIRED)**
- **Who**: Setter
- **Action**: Address EO feedback
- **Next**: Resubmit to EO (→ EXAM_OFFICER_CHECK)

**Step 4: External Check (EXTERNAL_FEEDBACK)**
- **Who**: External Examiner (assigned to module)
- **Action**: Review exam paper, provide feedback
- **Next**: Automatically transitions to SETTER_RESPONSE once feedback submitted

**Step 5: Setter Response (SETTER_RESPONSE)**
- **Who**: Setter
- **Action**: Respond to external examiner's feedback, make adjustments
- **Next**: Submit for final check

**Step 6: Final Check (FINAL_CHECK)**
- **Who**: Exams Officer
- **Action**: Final review before printing
- **Next**: Send to Printing (→ SENT_TO_PRINTING) or Request More Changes (→ EXAM_CHANGES_REQUIRED)

**Step 7: Print (SENT_TO_PRINTING)**
- **Who**: Exams Officer
- **Action**: Send to exams office for secure printing
- **Next**: Wait for exam date

**Step 8: Exam (EXAM_TAKEN)**
- **Who**: Module Lead or Exams Officer
- **Action**: Exam takes place, scripts collected
- **Next**: Begin marking

**Step 9: Marking (MARKING)**
- **Who**: Marking team (includes standardisation)
- **Action**: Mark exam scripts
- **Next**: Admin check

**Step 10: Admin Check (ADMIN_MARK_CHECK)**
- **Who**: Exams Officer
- **Action**: Administrative check of marks (no calculation errors)
- **Next**: Submit for moderation

**Step 11: Moderation (MODERATED)**
- **Who**: Module Moderator
- **Action**: Review sample of marked exams
- **Next**: Submit for approval

**Step 12: Approval (APPROVED)**
- **Who**: Module Lead (school exam board, faculty, central approval)
- **Action**: Formal approval through exam boards
- **Next**: Publish

**Step 13: Publish (PUBLISHED)**
- **Who**: Teaching Support / Admin
- **Action**: Final publication of marks
- **Final State**: Workflow complete

---

## Role-Based Permissions

### Setter
- Create assessments (DRAFT)
- Submit for checking (DRAFT → READY_FOR_CHECK)
- Address changes (CHANGES_REQUIRED → READY_FOR_CHECK)
- Address EO changes (EXAM_CHANGES_REQUIRED → EXAM_OFFICER_CHECK)
- Respond to external feedback (SETTER_RESPONSE → FINAL_CHECK)
- Cannot be checker on own assessments

### Checker
- Approve or reject submissions (READY_FOR_CHECK → RELEASED/TEST_TAKEN/EXAM_OFFICER_CHECK or CHANGES_REQUIRED)
- Must be independent (not module lead, not module staff, not setter)
- Auto-assigned from Module Moderator by default

### Module Lead
- Record deadline passed (RELEASED → DEADLINE_PASSED)
- Record test taken (TEST_TAKEN state transition)
- Record exam taken (EXAM_TAKEN state transition)
- Return feedback (MODERATED → FEEDBACK_RETURNED)
- Return results (MODERATED → RESULTS_RETURNED)
- Approve for publication (FEEDBACK_RETURNED/RESULTS_RETURNED → APPROVED)

### Module Moderator
- Auto-assigned as default checker for all assessments
- Complete moderation (MARKING → MODERATED)
- Can be overridden as checker if needed

### Exams Officer (EO)
- Check exam papers (EXAM_OFFICER_CHECK → EXTERNAL_FEEDBACK or EXAM_CHANGES_REQUIRED)
- Final check before printing (FINAL_CHECK → SENT_TO_PRINTING or EXAM_CHANGES_REQUIRED)
- Send to printing (SENT_TO_PRINTING → EXAM_TAKEN)
- Admin mark check (MARKING → ADMIN_MARK_CHECK for exams)
- Can view all assessments in admin mode

### External Examiner
- Must be assigned to module
- Submit feedback on exams (during EXTERNAL_FEEDBACK state)
- Feedback triggers automatic transition to SETTER_RESPONSE
- Cannot modify assessment directly

### Teaching Support / Admin
- Override all restrictions
- Final publication (APPROVED → PUBLISHED)
- Can perform any action as system administrator
- View all assessments across all modules

---

## Validation Rules

### Checker Independence
- Cannot be module lead on the module
- Cannot be module staff on the module
- Cannot be setter on the assessment
- Must be ACADEMIC user type

### External Examiner Requirements
- Must be assigned to the module via ModuleExternalExaminerRepository
- Can only submit feedback on EXAM type assessments
- Assessment must be in EXTERNAL_FEEDBACK state
- Can only submit feedback once per assessment

### Setter Response Requirements
- Must be a setter on the assessment
- Assessment must be EXAM type
- Must be in SETTER_RESPONSE state
- External feedback must already exist
- Can only submit response once

### State Transition Validation
- Each transition is validated against assessment type
- User role and permissions checked before allowing transition
- Cannot skip states (must follow defined workflow)
- Some transitions can be repeated (e.g., CHANGES_REQUIRED ↔ READY_FOR_CHECK)

---

## Implementation Notes

### Backend
- **AssessmentState.java**: Enum defining all possible states
- **TransitionService.java**: Core workflow logic and permission checking
  - `canProgress()`: Validates if user can make specific transition
  - `allowedTargets()`: Returns valid next states for current user
  - `canBeChecker()`: Validates checker independence
  - `canSubmitExternalFeedback()`: Validates external examiner permissions
  - `canSubmitSetterResponse()`: Validates setter response permissions
  - State machine methods: `getCourseworkNextStates()`, `getTestNextStates()`, `getExamNextStates()`

### Frontend
- Assessment detail pages should display current state
- Show available transitions based on user role (call `allowedTargets()`)
- Provide forms for:
  - Checker feedback (approve/reject)
  - External examiner feedback submission
  - Setter response submission
- Progress buttons should be role-aware and state-aware

### Database
- AssessmentRoleAssignment: Links users to assessments with SETTER/CHECKER roles
- ModuleStaffRole: Links users to modules with MODULE_LEAD/MODERATOR/STAFF roles
- ModuleExternalExaminer: Links external examiners to modules
- ExternalExaminerFeedback: Stores feedback submissions
- SetterResponse: Stores setter responses to external feedback

---

## Testing Recommendations

### Unit Tests
- Test each state transition for all three types
- Test permission checking for each role
- Test checker independence validation
- Test external examiner assignment validation

### Integration Tests
- Complete workflow from DRAFT to PUBLISHED for each type
- Test modification loops (CHANGES_REQUIRED)
- Test exam-specific workflows (external feedback, setter response)
- Test role switching (EO view mode)

### Edge Cases
- Attempt invalid transitions
- Attempt to be checker on own assessment
- External examiner not assigned to module
- Missing required data (feedback, response)
- Multiple modification cycles

---

## Future Enhancements

### Potential Additions
1. **Notifications**: Email alerts for state changes
2. **Deadlines**: Automatic tracking and reminders
3. **Comments**: Allow comments at each transition
4. **History**: Full audit trail of state changes
5. **Bulk Operations**: Process multiple assessments together
6. **Delegation**: Allow role delegation (e.g., deputy module lead)
7. **Templates**: Assessment templates for quick creation
8. **Analytics**: Workflow timing and bottleneck identification

---

*Last Updated: 2024*
*Version: 1.0*
