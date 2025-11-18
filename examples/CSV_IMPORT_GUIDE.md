# CSV Import Guide

## Module & Assessment Import Format

This guide explains how to import modules with their assessments using CSV files.

### File Format

**Important:** The CSV file should have **no header row**. Each row represents one module with its assessments.

### Column Structure

| Column | Name | Required | Description |
|--------|------|----------|-------------|
| A | Module Code | ✓ Yes | Unique module code (e.g., COM1001) |
| B | Module Title | ✓ Yes | Full module name |
| C | Module Lead | ✓ Yes | Name of the module lead (must exist in system). Gets MODULE_LEAD role |
| D | Moderators | ✗ No | Comma-separated list of moderator names (can be blank). Get MODERATOR role |
| E+ | Assessments | ✗ No | Pairs of (type, title) - can have multiple assessments |

### Column D - Moderators (Optional)

As per client requirements, **moderators are optional**. This allows you to:
- Set up module details early and assign moderators later
- Leave column D blank if no moderator is assigned yet
- Add multiple moderators as a comma-separated list
- Moderators will automatically be assigned as checkers for all assessments in the module

**Examples:**
- Single moderator: `James Mapp`
- Multiple moderators: `Kirill Bogdanov, Tahsin Khan, Donghwan Shin` (can use quotes)
- No moderator: `` (leave blank)

### Assessment Pairs (Column E onward)

Assessments are specified as repeating pairs of **type** and **title**:

**Assessment Types:**
- `exam` - Examination assessment
- `cw` - Coursework assessment  
- `test` - Test (treated as coursework)

**Format:** `type1,title1,type2,title2,type3,title3,...`

Empty pairs at the end are ignored (just leave blank cells).

### Examples

#### Example 1: Module with moderators and multiple assessments
```csv
COM1001,Introduction to Software Engineering,Phil McMinn,"Kirill Bogdanov, Tahsin Khan, Donghwan Shin",cw,Programming Assignment,cw,Requirements Specification,cw,Team Project
```

#### Example 2: Module without moderator (assigned later)
```csv
COM4507,Software and Hardware Verification,Georg Struth,,exam,Final Exam
```

#### Example 3: Module with single moderator
```csv
COM107,Systems and Networks,Prosanta Gope,James Mapp,cw,Lab Assessment,cw,Test,exam,Final Exam
```

#### Example 4: Module with moderators, partial assessments
```csv
COM1002,Foundations of Computer Science,Maksim Zhukovskii,"Delvin Ce Zhang, Georgios Moulantzikos, Parinya Chalermsook",exam,Autumn Exam,exam,Spring Exam,,
```
(Note the empty cells at the end are ignored)

### Complete CSV File Example

See `modules_import_example.csv` for a complete working example.

### Import Process

1. Go to **Admin Panel** → **CSV Import** tab
2. Select the academic year (e.g., "2024/25")
3. Click "Choose CSV File to Import"
4. Select your CSV file
5. Review the import results

### Import Behavior

- **Duplicate modules:** If a module with the same code and academic year exists, only the title is updated
- **Missing users:** If a moderator or module lead is not found, an error is logged and that row is skipped
- **Partial moderators:** If some moderators in the list are found and others aren't, the found ones are assigned with a warning
- **Role assignments:** 
  - Module lead gets MODULE_LEAD role
  - Moderators get MODERATOR role
  - Moderators are automatically assigned as CHECKERs for all assessments
- **Assessment states:** All imported assessments start in DRAFT state

### Validation Rules

1. Module code and title are required
2. Module lead is required and must exist in the system
3. Moderators are optional - column D can be blank
4. Assessment type must be one of: `exam`, `cw`, or `test`
5. Assessment title is required if type is provided
6. Empty assessment pairs are ignored

### Troubleshooting

**Error: "Module lead 'Name' not found"**
- The specified user doesn't exist. Create the user first via the Users tab.

**Error: "Moderator 'Name' not found"**  
- Warning only - import continues with available moderators. Create missing users if needed.

**Error: "Invalid type 'X'. Must be EXAM, CW, or TEST"**
- Check assessment type spelling. Valid values: `exam`, `cw`, `test` (case-insensitive)

**Success: "Imported 0 modules and X assessments"**
- Modules already existed, only assessments were added (this is normal)

### Tips

- Keep your CSV file in UTF-8 encoding
- Use double quotes around comma-separated moderator lists
- Test with a small file first (1-2 modules)
- Keep the example CSV file as a template
- Moderators can be assigned to modules later through the module management interface
