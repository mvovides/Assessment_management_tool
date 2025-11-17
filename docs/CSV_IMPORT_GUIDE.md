# CSV Import Guide

## Overview

The Assessment Management Tool supports bulk data import via CSV files for users, modules, and assessments. This feature is available to users with ADMIN or EXAMS_OFFICER roles.

## Access

1. Log in as an administrator or exams officer
2. Navigate to **Admin** page
3. Click on **CSV Import** tab

## CSV Formats

### 1. Users Import

**File Format:** `users.csv`

**Required Columns:**
- `name` - Full name of the user
- `email` - Email address (must be unique)
- `baseType` - User type: `ACADEMIC`, `TEACHING_SUPPORT`, or `EXTERNAL_EXAMINER`
- `isExamsOfficer` - Boolean: `true` or `false`

**Example:**
```csv
name,email,baseType,isExamsOfficer
John Doe,john.doe@sheffield.ac.uk,ACADEMIC,false
Jane Smith,jane.smith@sheffield.ac.uk,TEACHING_SUPPORT,true
Bob Johnson,bob.johnson@example.com,EXTERNAL_EXAMINER,false
```

**Notes:**
- Passwords are automatically generated (12 characters, random, secure)
- Generated passwords are logged to the console (in production, send via email)
- Duplicate emails will be skipped with an error message
- All users are created with `active=true` status

---

### 2. Modules Import

**File Format:** `modules.csv`

**Required Columns:**
- `code` - Module code (e.g., COM1001)
- `title` - Full module title
- `academicYear` - Academic year in format `YYYY/YY` (e.g., 2024/25)

**Example:**
```csv
code,title,academicYear
COM1001,Introduction to Programming,2024/25
COM2008,Software Engineering,2024/25
COM3001,Research Project,2024/25
```

**Notes:**
- Module codes are automatically converted to uppercase
- Duplicate combinations of code+academicYear will be skipped
- Modules are created without staff assignments (add manually later)

---

### 3. Assessments Import

**File Format:** `assessments.csv`

**Required Columns:**
- `moduleCode` - Existing module code
- `academicYear` - Must match an existing module
- `title` - Assessment title
- `type` - Assessment type: `EXAM` or `CW` (coursework)
- `examDate` - Date in format `yyyy-MM-dd` (required for EXAM, optional for CW)

**Example:**
```csv
moduleCode,academicYear,title,type,examDate
COM1001,2024/25,Final Exam,EXAM,2025-05-15
COM1001,2024/25,Programming Assignment,CW,
COM2008,2024/25,Group Project,CW,
COM3001,2024/25,Research Presentation,EXAM,2025-06-10
```

**Notes:**
- Module must exist before importing assessments
- Exam date format must be `yyyy-MM-dd`
- All assessments are created in `DRAFT` state
- Assessment roles (setter, checker, etc.) must be assigned manually after import

---

## Import Process

### Step 1: Prepare CSV File
1. Create a CSV file using Excel, Google Sheets, or a text editor
2. Ensure the first row contains column headers (case-insensitive)
3. Validate data format matches requirements

### Step 2: Upload File
1. Click the **Choose File** button for the appropriate import type
2. Select your CSV file
3. Upload begins automatically

### Step 3: Review Results
- Success: Green message showing number of records imported
- Partial Success: Yellow message showing errors for specific lines
- Failure: Red message with error details

**Example Success Message:**
```
Successfully imported 15 users
```

**Example Error Message:**
```
Imported 12 users. Errors:
Line 3: User with email john.doe@sheffield.ac.uk already exists
Line 7: Invalid base type 'STAFF'. Must be ACADEMIC, TEACHING_SUPPORT, or EXTERNAL_EXAMINER
Line 10: Email is required
```

---

## Common Errors and Solutions

### Users Import

| Error | Solution |
|-------|----------|
| "User with email X already exists" | Check existing users or use a different email |
| "Invalid base type" | Use only: ACADEMIC, TEACHING_SUPPORT, or EXTERNAL_EXAMINER |
| "Email is required" | Ensure all rows have an email value |
| "Name is required" | Ensure all rows have a name value |

### Modules Import

| Error | Solution |
|-------|----------|
| "Module X for Y already exists" | Module code + academic year combination must be unique |
| "Module code is required" | Ensure all rows have a code value |
| "Academic year is required" | Ensure all rows have an academic year value |

### Assessments Import

| Error | Solution |
|-------|----------|
| "Module X for Y not found" | Import modules before assessments |
| "Invalid assessment type" | Use only: EXAM or CW |
| "Exam date is required for EXAM assessments" | Provide a date in yyyy-MM-dd format |
| "Invalid date format" | Use yyyy-MM-dd format (e.g., 2025-05-15) |

---

## API Endpoints

### Upload Users
```http
POST /api/admin/import/users
Content-Type: multipart/form-data
Authorization: Required (ADMIN or EXAMS_OFFICER role)

Form Data:
  file: users.csv
```

### Upload Modules
```http
POST /api/admin/import/modules
Content-Type: multipart/form-data
Authorization: Required (ADMIN or EXAMS_OFFICER role)

Form Data:
  file: modules.csv
```

### Upload Assessments
```http
POST /api/admin/import/assessments
Content-Type: multipart/form-data
Authorization: Required (ADMIN or EXAMS_OFFICER role)

Form Data:
  file: assessments.csv
```

### Get Import Jobs History
```http
GET /api/admin/import/jobs
Authorization: Required (ADMIN or EXAMS_OFFICER role)

Response:
[
  {
    "id": "uuid",
    "fileName": "users.csv",
    "status": "COMPLETED",
    "createdAt": "2024-01-15T10:30:00Z",
    "errors": "Successfully imported 15 users"
  }
]
```

---

## Best Practices

1. **Test with Small Files First**
   - Start with 5-10 records to validate format
   - Check for errors before importing full dataset

2. **Import Order**
   - Import users first (if they don't exist)
   - Import modules second
   - Import assessments last (requires existing modules)

3. **Backup Data**
   - Export existing data before bulk imports
   - Keep original CSV files for reference

4. **Validate Data**
   - Check for duplicate emails/module codes
   - Verify date formats (yyyy-MM-dd)
   - Ensure all required fields have values

5. **Character Encoding**
   - Use UTF-8 encoding for CSV files
   - Avoid special characters in module codes

6. **Post-Import Tasks**
   - Verify imported records in the UI
   - Assign assessment roles (setter, checker)
   - Assign staff to modules
   - Send login credentials to new users (manual in current version)

---

## Limitations

1. **No Update Support**: Import only creates new records, does not update existing ones
2. **No Relationships**: Cannot import staff assignments or assessment roles via CSV
3. **Password Distribution**: Generated passwords are logged to console, not emailed
4. **No Validation Preview**: Errors are only shown after upload attempt
5. **File Size**: Large files (>1000 rows) may timeout

---

## Example Files

Sample CSV files are available in the `examples/` directory:

- `examples/users.csv` - Sample user data
- `examples/modules.csv` - Sample module data
- `examples/assessments.csv` - Sample assessment data

---

## Troubleshooting

### File Upload Fails
- Check file extension is `.csv`
- Verify file is not empty
- Ensure file size is < 10MB

### All Rows Skipped
- Verify CSV header row matches expected format exactly
- Check for extra spaces in column names
- Ensure file encoding is UTF-8

### Partial Import Success
- Review error messages for specific line numbers
- Fix errors in CSV file
- Re-upload only failed records

---

## Support

For issues or questions:
1. Check error message for specific line numbers
2. Review CSV format requirements above
3. Verify data in H2 console: http://localhost:8080/h2-console
4. Check application logs for detailed error messages
