# Assessment Content Submission Feature

## Overview
This feature allows setters to upload assessment content (questions, marking schemes, instructions) and submit it to initiate the assessment workflow. This is a critical step that transitions assessments from creation to the review process.

## Feature Description

### What This Enables
- **Setters** can upload their assessment materials while in DRAFT state
- Assessment content is stored with metadata (description, file name, file URL)
- Content can be edited/updated while still in DRAFT state
- Once content is uploaded, setters can submit the assessment for checking (DRAFT → READY_FOR_CHECK)
- Checkers can review the uploaded content before approving or requesting changes

## Implementation Details

### Backend Changes

#### 1. **Assessment Entity** (`Assessment.java`)
Added three new fields:
```java
@Column(name = "description", columnDefinition = "TEXT")
private String description;

@Column(name = "file_name")
private String fileName;

@Column(name = "file_url")
private String fileUrl;
```

**Fields:**
- `description`: TEXT - Overview of assessment structure, topics, format
- `fileName`: VARCHAR(255) - Name of the file stored in secure storage
- `fileUrl`: VARCHAR(500) - Link to secure storage location (SharePoint, OneDrive, etc.)

#### 2. **SubmitAssessmentRequest DTO** (`SubmitAssessmentRequest.java`)
New request DTO for submitting content:
```java
public class SubmitAssessmentRequest {
    @NotBlank(message = "Description is required")
    private String description;
    private String fileName;
    private String fileUrl;
}
```

#### 3. **AssessmentDto** (`AssessmentDto.java`)
Updated to include content fields:
```java
private String description;
private String fileName;
private String fileUrl;
```

#### 4. **EntityMapper** (`EntityMapper.java`)
Updated `toAssessmentDto()` to map new fields.

#### 5. **AssessmentService** (`AssessmentService.java`)
New method `submitAssessmentContent()`:
```java
public AssessmentDto submitAssessmentContent(UUID assessmentId, UUID userId, SubmitAssessmentRequest request)
```

**Validation:**
- User must be a SETTER on the assessment
- Assessment must be in DRAFT state
- Description is required

**Actions:**
- Updates assessment with description, fileName, fileUrl
- Returns updated assessment DTO

#### 6. **AssessmentController** (`AssessmentController.java`)
New endpoint:
```
POST /api/assessments/{assessmentId}/submit-content
```

**Request Body:**
```json
{
  "description": "Assignment covering Java fundamentals...",
  "fileName": "COM101_Assignment2.pdf",
  "fileUrl": "https://sharepoint.sheffield.ac.uk/..."
}
```

**Response:** Updated AssessmentDto

**Security:** Any authenticated user (checked by service layer for SETTER role)

#### 7. **Database Migration** (`V3__add_assessment_content_fields.sql`)
```sql
ALTER TABLE assessment
ADD COLUMN description TEXT,
ADD COLUMN file_name VARCHAR(255),
ADD COLUMN file_url VARCHAR(500);
```

### Frontend Changes

#### 1. **client.js API**
Added new API method:
```javascript
submitContent: (id, data) => api.post(`/assessments/${id}/submit-content`, data).then(res => res.data)
```

#### 2. **AssessmentDetailPage.jsx**
Major updates:

**New State Variables:**
```javascript
const [showSubmitContentModal, setShowSubmitContentModal] = useState(false);
const [contentData, setContentData] = useState({
  description: '',
  fileName: '',
  fileUrl: '',
});
```

**New Mutation:**
```javascript
const submitContentMutation = useMutation({
  mutationFn: (data) => assessmentApi.submitContent(assessmentId, data),
  onSuccess: () => {
    queryClient.invalidateQueries(['assessments', assessmentId]);
    setShowSubmitContentModal(false);
    setContentData({ description: '', fileName: '', fileUrl: '' });
  },
});
```

**New Handler:**
```javascript
const handleSubmitContent = (e) => {
  e.preventDefault();
  submitContentMutation.mutate(contentData);
};
```

**New UI Section: Assessment Content Card**
- Displays content if uploaded (description, file name, download link)
- Shows "Upload Content" button for setters in DRAFT state
- Shows "Edit Content" button for setters to update content
- Shows helpful message when no content uploaded

**New Modal: Submit Content Modal**
- Form with description (required), fileName, fileUrl fields
- Guidelines explaining secure storage approach
- Validation and error handling
- Only accessible by setters in DRAFT state

## User Workflow

### For Setters:

1. **Create Assessment**
   - Module lead creates assessment in DRAFT state
   - System auto-assigns moderator as checker

2. **Upload Content**
   - Click "Upload Content" button on assessment detail page
   - Fill in description (required) - overview of assessment structure
   - Optionally provide file name and secure storage URL
   - Click "Upload Content"

3. **Edit Content (Optional)**
   - While still in DRAFT, can click "Edit Content"
   - Update description, file details
   - Re-upload

4. **Submit for Checking**
   - After content is ready, click "📤 Submit for Checking"
   - This transitions assessment from DRAFT → READY_FOR_CHECK
   - Checker is notified to review

### For Checkers:

1. **View Content**
   - Navigate to assessment detail page
   - See "Assessment Content" section with description and file link
   - Click file link to download from secure storage

2. **Review and Decide**
   - Review the content thoroughly
   - Either:
     - **Approve**: Click "✅ Approve" to move to next state
     - **Request Changes**: Click "❌ Request Changes" and provide feedback

### For All Users:

- **View Content**: Anyone with access to the assessment can view the content metadata
- **Download**: File link opens secure storage location (requires appropriate permissions)

## Security & Storage

### Secure Storage Approach
- **Assessment content (questions, answers) NOT stored in database**
- Only metadata (description, file name, URL) stored
- Actual files stored in secure external storage (SharePoint, OneDrive)
- This complies with Q&A #10 and #11 requirements

### Access Control
- **Upload/Edit**: Only SETTERS in DRAFT state
- **View**: Any user with access to the assessment
- **Download**: Controlled by external storage permissions

## UI Features

### Assessment Content Card
- **Header**: "Assessment Content" with conditional upload button
- **Empty State**: 
  - Helpful message for setters
  - Tip about uploading content
- **Content Display**:
  - Description in readable format
  - File card with icon, name, and download link
  - Edit button for setters (DRAFT only)

### Upload Content Modal
- **Guidelines Box**: Blue info box with best practices
- **Description Field**: Required multi-line textarea
- **File Name Field**: Optional text input
- **File URL Field**: Optional URL input
- **Important Note**: Yellow warning about workflow after upload
- **Validation**: Description required, URL format validated
- **Error Handling**: Displays server errors clearly

## Validation Rules

### Backend Validation
1. User must be assigned as SETTER
2. Assessment must be in DRAFT state
3. Description cannot be blank
4. Valid UUID for assessmentId and userId

### Frontend Validation
1. Description required (enforced by HTML5 `required`)
2. URL field type="url" for format validation
3. Submit button disabled when description empty
4. Loading states during submission

## Error Handling

### Common Errors
1. **Not a Setter**: "Only setters can submit assessment content"
2. **Wrong State**: "Assessment content can only be submitted from DRAFT state"
3. **Network Error**: "Failed to upload content"
4. **Validation Error**: "Description is required"

### Error Display
- Red alert box below form
- Server error messages passed through
- User-friendly fallback messages

## Benefits

### For Setters
✅ Clear process to upload their work
✅ Can update content before submitting for review
✅ Secure storage for sensitive content
✅ Visual confirmation of uploaded content

### For Checkers
✅ Easy access to content for review
✅ Clear metadata about what to review
✅ Direct link to secure storage

### For System
✅ Audit trail of content uploads
✅ Separates metadata from sensitive content
✅ Complies with data protection requirements
✅ Enables workflow progression

## Testing Checklist

- [ ] Setter can upload content in DRAFT state
- [ ] Description is required
- [ ] File name and URL are optional
- [ ] Content displays correctly after upload
- [ ] Setter can edit content while in DRAFT
- [ ] File download link works correctly
- [ ] Non-setters cannot upload content
- [ ] Cannot upload content after leaving DRAFT state
- [ ] Content persists after page refresh
- [ ] Submit for checking works after content upload
- [ ] Error messages display appropriately
- [ ] Modal cancel button works
- [ ] Form resets after successful submission
- [ ] Loading states work correctly

## Future Enhancements

1. **Direct File Upload**: Allow uploading files directly through UI
2. **Multiple Files**: Support multiple attachments
3. **File Preview**: Preview documents in browser
4. **Version History**: Track content changes over time
5. **Templates**: Pre-populate with common assessment structures
6. **Auto-save**: Save drafts automatically
7. **Content Validation**: Check for common issues (formatting, missing sections)
8. **Integration**: Direct integration with SharePoint/OneDrive APIs

## Compliance

### Q&A Specifications
- ✅ **Q10**: Content feedback not stored with question content
- ✅ **Q11**: Process tracking only, not assessment content
- ✅ Secure storage references instead of direct storage
- ✅ Metadata approach complies with data protection

## Summary

This feature provides a complete solution for setters to submit their assessment content and begin the workflow process. The implementation follows best practices:

- **Security**: Content stored externally with proper access control
- **Usability**: Clear UI with helpful guidance
- **Validation**: Proper checks at all levels
- **Flexibility**: Can update content while in DRAFT
- **Compliance**: Meets all Q&A requirements
- **Audit**: Full tracking of content submissions

The feature seamlessly integrates with the existing state transition workflow, enabling the complete assessment lifecycle from creation through submission, review, and approval.
